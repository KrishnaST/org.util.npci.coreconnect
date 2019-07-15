package org.util.npci.coreconnect;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.util.datautil.ByteHexUtil;
import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.format.ISOFormat;
import org.util.iso8583.format.NPCIFormat;
import org.util.iso8583.npci.MTI;
import org.util.iso8583.npci.ResponseCode;
import org.util.nanolog.Logger;
import org.util.npci.api.ShutDownable;
import org.util.npci.api.Status;
import org.util.npci.api.model.Locker;

public final class CoreConnect extends Thread implements ShutDownable {

	private static final ISOFormat npciFormat = NPCIFormat.getInstance();

	private final ConcurrentHashMap<String, Locker<ISO8583Message>> tranmap      = new ConcurrentHashMap<>(20);
	private final AtomicBoolean                                     socketStatus = new AtomicBoolean(false);
	private final AtomicReference<Status>                           status       = new AtomicReference<Status>(Status.NEW);

	private final InetSocketAddress npciAddress;
	private Socket                  socket;
	private InputStream             is;
	private OutputStream            os;

	public final CoreConfig config;
	public final Logger     logger;

	public CoreConnect(CoreConfig config) {
		this.config = config;
		logger      = config.corelogger;
		npciAddress = new InetSocketAddress(config.npciIp, config.npciPort);
		setName(config.bankId + "-coreconnect");
	}

	public final void run() {
		setStatus(Status.NEW);
		while (Status.SHUTDOWN != status.get()) {
			try {
				boolean isNPCISocketConnected = socketStatus.get() ? socketStatus.get() : initSocket();
				if (!isNPCISocketConnected) logger.info("unable to connect to npci : " + config.bankId + " status : " + status.get());
				if (!isNPCISocketConnected) { continue; }
				logger.info("reading from npci : ");
				byte[] bytes = receive();
				logger.trace("read from npci : " + ByteHexUtil.byteToHex(bytes));
				if (bytes == null || bytes.length < 10) continue;
				try {
					final ISO8583Message message = EncoderDecoder.decode(npciFormat, bytes);
					if (message == null || message.get(0) == null) continue;
					if (message.isRequest()) config.dispatcher.dispatch(message);
					else sendResponseToAcquirer(message);
				} catch (Exception e) {
					logger.info("message parsing error");
					logger.info(e);
				}
			} catch (Exception e) {
				logger.info(e);
			}
		}
		setStatus(Status.SHUTDOWN);
		logger.info("shutting down npci connector : " + config.bankId);
	}

	private final boolean send(byte[] bytes, Logger logger) {
		try {
			if (socketStatus.get()) {
				logger.trace("writing : " + ByteHexUtil.byteToHex(bytes));
				os.write(bytes);
				os.flush();
				return true;
			}
		} catch (Exception e) {
			logger.error("error sending message to npci" + e.getMessage());
			socketStatus.set(false);
			if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
		}
		return false;
	}

	private final byte[] receive() {
		if (Status.SHUTDOWN != status.get()) {
			byte[] bytes = null;
			try {
				int b1 = is.read();
				logger.trace("b1 : " + b1);
				int b2 = is.read();
				logger.trace("b2 : " + b2);
				if (b1 < 0 || b2 < 0) {
					logger.debug("unexpected socket break");
					socketStatus.set(false);
					if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
					return null;
				} else {
					logger.trace("len : " + (b1 * 256 + b2));
					bytes = new byte[b1 * 256 + b2];
					is.read(bytes);
					return bytes;
				}
			} catch (Exception e) {
				logger.debug("error receiving message from npci socketexception : " + e.getMessage());
				socketStatus.set(false);
				if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
			}
		}
		return null;
	}

	private final synchronized boolean initSocket() {
		if (Status.SHUTDOWN != status.get()) {
			socketStatus.set(false);
			closeSocket();
			try {
				logger.info("connecting to : " + npciAddress);
				socket = new Socket();
				socket.connect(npciAddress, 5000);
				socket.setKeepAlive(true);
				socket.setSoTimeout(0);
				//socket.setTcpNoDelay(true);
				//socket.setTrafficClass(0x04);
				is = socket.getInputStream();
				os = socket.getOutputStream();
				socketStatus.set(true);
				setStatus(Status.NEW);
				return true;
			} catch (Exception e) {
				logger.debug("connect to npci failed.");
				logger.trace(e);
				return false;
			}
		}
		return false;
	}

	private final void closeSocket() {
		try {
			closeQuietly(socket);
			closeQuietly(os);
			closeQuietly(is);
			socket = null;
			os     = null;
			is     = null;
		} catch (Exception e) {
			logger.info(e);
		}
	}

	private static final void closeQuietly(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {}
	}

	public final boolean shutdown() {
		if (Status.SHUTDOWN == status.get()) return true;
		try {
			setStatus(Status.SHUTDOWN);
			closeSocket();
			return true;
		} catch (Exception e) {
			logger.info(e);
		}
		return false;
	}

	public final Status getStatus() {
		return status.get();
	}

	public final void setStatus(Status status) {
		this.status.set(status);
		config.statusReceiver.notify(status);
	}

	public final boolean isSocketConnected() {
		if (socket != null && socket.isConnected()) return true;
		return false;
	}

	public final ISO8583Message sendRequestToNPCI(final ISO8583Message request, final Logger logger, final int timeoutInMs) {
		try {
			final String requestKey = request.getUniqueKey();
			logger.trace("request key : " + requestKey);
			final Locker<ISO8583Message> locker = new Locker<>(request);
			tranmap.put(requestKey, locker);
			final byte[]  bytes  = EncoderDecoder.encode(npciFormat, request);
			final boolean isSent = send(bytes, logger);
			if (isSent) {
				logger.info("waiting started for : " + timeoutInMs);
				synchronized (locker) {
					try {
						locker.wait(timeoutInMs);
					} catch (InterruptedException e) {
						logger.info(e);
					}
				}
				logger.trace("waiting finished");
			}
			tranmap.remove(requestKey);
			if (locker.response == null) {
				locker.response = request.copy();
				locker.response.put(0, MTI.getCounterMTI(locker.response.get(0)));
				locker.response.put(39, ResponseCode.ISSUER_INOPERATIVE);
			}
			return locker.response;
		} catch (Exception e) {
			logger.error(e);
		}
		final ISO8583Message response = request.copy();
		response.put(0, MTI.getCounterMTI(response.get(0)));
		response.put(39, ResponseCode.SYSTEM_MALFUNCTION);
		return response;
	}

	public final boolean sendResponseToAcquirer(final ISO8583Message response) {
		try {
			logger.trace("response_key : " + response.getUniqueKey());
			Locker<ISO8583Message> locker = tranmap.get(response.getUniqueKey());
			if (locker == null) {
				logger.info("delayed response for transaction : " + response.get(39));
				return false;
			}
			locker.response = response;
			synchronized (locker) {
				locker.notify();
			}
			return true;
		} catch (Exception e) {
			logger.error(e);
		}
		return false;
	}

	public final boolean sendResponseToNPCI(final ISO8583Message response, final Logger logger) {
		try {
			byte[]        bytes  = EncoderDecoder.encode(npciFormat, response);
			final boolean isSent = send(bytes, logger);
			return isSent;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return false;
	}
}
