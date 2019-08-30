package org.util.npci.coreconnect;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.util.datautil.ByteHexUtil;
import org.util.datautil.TLV;
import org.util.hsm.api.model.MACResponse;
import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.ISO8583LogSupplier;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.format.ISOFormat;
import org.util.iso8583.format.NPCIFormat;
import org.util.iso8583.npci.MTI;
import org.util.iso8583.npci.ResponseCode;
import org.util.nanolog.Logger;
import org.util.npci.api.ShutDownable;
import org.util.npci.api.Status;
import org.util.npci.coreconnect.issuer.UnIdentifiedTransaction;
import org.util.npci.coreconnect.logon.Logon;
import org.util.npci.coreconnect.util.Locker;
import org.util.npci.coreconnect.util.MACUtil;

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

	public CoreConnect(final CoreConfig config) {
		this.config         = config;
		this.logger         = config.corelogger;
		this.npciAddress    = new InetSocketAddress(config.npciIp, config.npciPort);
		setName(config.bankId + "-coreconnect");
	}

	//@formatter:off
	public final void run() {
		setStatus(Status.NEW);
		while (Status.SHUTDOWN != status.get()) {
			try {
				boolean isNPCISocketConnected = socketStatus.get() ? true : initSocket();
				if (!isNPCISocketConnected) logger.info("unable to connect to npci : " + config.bankId + " status : " + status.get());
				if (!isNPCISocketConnected) { continue; }
				logger.info(config.bankId, "connected and reading from npci : ");
				final byte[] bytes = receive();
				logger.trace(config.bankId, "read from npci : " + ByteHexUtil.byteToHex(bytes));
				if (bytes == null || bytes.length < 10) continue;
				try {
					final ISO8583Message message = EncoderDecoder.decode(npciFormat, bytes);
					if (message == null || message.get(0) == null) continue;
					if (message.isRequest()) {
						message.putAdditional(PropertyName.NPCI_RAW_REQUEST, bytes);
						final boolean dispatched = config.dispatcher.dispatch(message);
						if(!dispatched) config.schedular.execute(new UnIdentifiedTransaction(message, config.dispatcher));
					}
					else {
						message.putAdditional(PropertyName.NPCI_RAW_RESPONSE, bytes);
						sendResponseToAcquirer(message);
					}
				} catch (Exception e) {
					logger.info(config.bankId, "message parsing error");
					logger.error(e);
				}
			} catch (Exception e) {logger.info(e);}
		}
		setStatus(Status.SHUTDOWN);
		logger.info("shutting down npci connector : " + config.bankId);
	}

	private final boolean send(byte[] bytes, Logger logger) {
		try {
			final boolean connected = socketStatus.get() ?  true : false;//initSocket();
			if (connected) {
				this.logger.trace(config.bankId, "write to npci : " + ByteHexUtil.byteToHex(bytes));
				os.write(bytes);
				os.flush();
				return true;
			}
			return false;
		} catch (Exception e) {
			this.logger.error(config.bankId, "error sending message to npci" + e.getMessage());
			socketStatus.set(false);
			if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
		}
		return false;
	}

	private final byte[] receive() {
		if (Status.SHUTDOWN != status.get()) {
			try {
				final int b1 = is.read();
				this.logger.trace(config.bankId, "b1 : " + b1);
				final int b2 = is.read();
				this.logger.trace(config.bankId, "b2 : " + b2);
				if (b1 < 0 || b2 < 0) {
					this.logger.debug(config.bankId, "unexpected socket break");
					socketStatus.set(false);
					if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
					return null;
				} else {
					this.logger.trace("len : " + (b1 * 256 + b2));
					final byte[] bytes = new byte[b1 * 256 + b2];
					is.read(bytes);
					return bytes;
				}
			} catch (Exception e) {
				this.logger.debug(config.bankId, "error receiving message from npci socketexception : " + e.getMessage());
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
				logger.info(config.bankId, "connecting to : " + npciAddress);
				socket = new Socket();
				socket.connect(npciAddress, 5000);
				socket.setKeepAlive(true);
				socket.setSoTimeout(0);
				socket.setTcpNoDelay(true);
				is = socket.getInputStream();
				os = socket.getOutputStream();
				socketStatus.set(true);
				setStatus(Status.NEW);
				config.schedular.execute(new Logon(config));
				return true;
			} catch (Exception e) {
				logger.info(config.bankId, e.getMessage());
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
	
	public final void setSocketStatus(boolean status) {
		this.socketStatus.set(status);
	}
	
	public final boolean resetSocket() {
		this.socketStatus.set(false);
		try{
			socket.close();
			initSocket();
			return true;
		} catch (Exception e) {logger.error(e);}
		return false;
	}

	public final boolean isSocketConnected() {
		if (socket != null && socket.isConnected()) return true;
		return false;
	}

	public final ISO8583Message sendRequestToNPCI(final ISO8583Message request, final Logger logger) {
		return sendRequestToNPCI(request, logger, config.acquirerTimeout*1000);
	}
	
	public final ISO8583Message sendRequestToNPCI(final ISO8583Message request, final Logger logger, final int timeoutInMs) {
		try {
			config.acquirerInterceptor.applyToRequest(request);
			if (config.hasMAC && MTI.isMACable(request.get(0), request.get(3))) {
				final MACResponse macResponse = MACUtil.calculateMAC(config, request.getMAB(), logger);
				if (macResponse != null && macResponse.isSuccess) request.put(48, TLV.parse(request.get(48)).put("099", macResponse.mac).build());
			}
			final String requestKey = request.getUniqueKey();
			logger.trace(config.bankId, "request key : " + requestKey);
			final Locker<ISO8583Message> locker = new Locker<>(request);
			tranmap.put(requestKey, locker);
			final byte[]  bytes  = EncoderDecoder.encode(npciFormat, request);
			request.putAdditional(PropertyName.NPCI_RAW_REQUEST, bytes);
			final boolean isSent = send(bytes, logger);
			logger.info("issent",Boolean.toString(isSent));
			logger.trace("request bytes", ByteHexUtil.byteToHex(bytes));
			logger.trace("request ", new ISO8583LogSupplier(request));
			if (isSent) {
				logger.info(config.bankId, "waiting started for : " + timeoutInMs);
				synchronized (locker) {
					try {
						locker.wait(timeoutInMs);
					} catch (InterruptedException e) {
						logger.info(e);
					}
				}
				logger.trace(config.bankId, "waiting finished");
			} else logger.info("npci connectity down while transaction ", request.get(37));
			tranmap.remove(requestKey);
			if (locker.response == null) {
				logger.info("transaction timedout. generating timeout response.");
				locker.response = request.copy();
				locker.response.put(0, MTI.getResponseMTI(locker.response.get(0)));
				locker.response.put(39, ResponseCode.ISSUER_INOPERATIVE);
				locker.response.putAdditional(PropertyName.IS_STATIC_RESPONSE, true);
				logger.trace("response received", Boolean.toString(false));
				logger.info("response ", new ISO8583LogSupplier(locker.response));
			}
			else if(config.hasMAC && MTI.isMACable(locker.response.get(0), locker.response.get(3))) {
				logger.trace("response received", Boolean.toString(true));
				logger.info("response ", new ISO8583LogSupplier(locker.response));
				TLV tlv = TLV.parse(locker.response.get(48));
				MACResponse macResponse = MACUtil.validateMAC(config, locker.response.getMAB(), tlv.get("099"), logger);
				if(macResponse == null || !macResponse.isSuccess) {
					logger.error("mac verification failure.changing response code to : "+ResponseCode.MAC_FAILURE_ACQUIRER);
					locker.response.put(39, ResponseCode.MAC_FAILURE_ACQUIRER);
				}
			} else {
				logger.trace("response received", Boolean.toString(true));
				logger.info("response ", new ISO8583LogSupplier(locker.response));
			}
			config.acquirerInterceptor.applyToResponse(request);
			return locker.response;
		} catch (Exception e) {logger.error(e);}
		final ISO8583Message response = request.copy();
		response.put(0, MTI.getResponseMTI(response.get(0)));
		response.put(39, ResponseCode.ISSUER_INOPERATIVE);
		return response;
	}

	public final boolean sendResponseToAcquirer(final ISO8583Message response) {
		try {
			logger.trace(config.bankId, "response_key : " + response.getUniqueKey());
			final Locker<ISO8583Message> locker = tranmap.get(response.getUniqueKey());
			if (locker == null) {
				logger.error(config.bankId, "delayed response for rrn : " + response.get(37)+" response code : "+response.get(39));
				logger.trace("delayed response ", ByteHexUtil.byteToHex((byte[]) response.getAdditional(PropertyName.NPCI_RAW_RESPONSE)));
				return false;
			}
			locker.response = response;
			synchronized (locker) {locker.notify();}
			return true;
		} catch (Exception e) {logger.error(e);}
		return false;
	}

	public final boolean sendResponseToNPCI(final ISO8583Message response, final Logger logger) {
		response.put(0, MTI.getResponseMTI(response.get(0)));
		try {
			config.issuerInterceptor.applyToResponse(response);
			if (config.hasMAC && MTI.isMACable(response.get(0), response.get(3))) {
				final MACResponse macResponse = MACUtil.calculateMAC(config, response.getMAB(), logger);
				logger.info("mac response", macResponse.toString());
				if (macResponse != null && macResponse.isSuccess) response.put(48, TLV.parse(response.get(48)).put("099", macResponse.mac).build());
				else logger.error("mac calculation failed.");
			}
			final byte[] bytes  = EncoderDecoder.encode(npciFormat, response);
			final boolean isSent = send(bytes, logger);
			logger.trace("response bytes issent : "+Boolean.toString(isSent), ByteHexUtil.byteToHex(bytes));
			logger.trace(new ISO8583LogSupplier(response));
			return isSent;
		} catch (Exception e) {logger.error(e);}
		return false;
	}

	@Override
	public final String toString() {
		return "coreconnect-" + config.bankId;
	}

}
