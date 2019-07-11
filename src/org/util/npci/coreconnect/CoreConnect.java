package org.util.npci.coreconnect;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.util.datautil.ByteHexUtil;
import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.format.ISOFormat;
import org.util.iso8583.format.NPCIFormat;
import org.util.nanolog.Logger;
import org.util.npci.api.ShutDownable;
import org.util.npci.api.Status;
import org.util.npci.coreconnect.logon.Logon;

public final class CoreConnect extends Thread implements ShutDownable{

	private static final ISOFormat npciFormat = NPCIFormat.getInstance();

	private final AtomicBoolean           socketStatus = new AtomicBoolean(false);
	private final AtomicReference<Status> status       = new AtomicReference<Status>(Status.NEW);

	private Socket       socket;
	private InputStream  is;
	private OutputStream os;
	
	public final CoreConfig config;
	public final Logger logger;
	
	public CoreConnect(CoreConfig config) {
		this.config = config;
		logger = config.coreLogger;
	}

	public void run() {
		setStatus(Status.NEW);
		while (Status.SHUTDOWN != status.get()) {
			try {
				boolean isNPCISocketConnected = socketStatus.get() ? socketStatus.get() : initSocket();
				if (!isNPCISocketConnected) logger.info("unable to connect to npci : " + config.bankId+ " status : " + status.get());
				if (!isNPCISocketConnected) {
					Thread.sleep(1000);
					continue;
				}
				logger.info("reading from npci : ");
				byte[] bytes = receive();
				logger.info("read from npci : " + ByteHexUtil.byteToHex(bytes));
				if (bytes == null || bytes.length < 10) continue;
				try {
					ISO8583Message message = EncoderDecoder.decode(npciFormat, bytes);
					if (message == null || message.get(0) == null) continue;
					config.dispatcher.dispatch(message);
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

	public final synchronized Status send(byte[] bytes, Logger logger) {
		try {
			if ((Status.NEW == status.get() || Status.LOGGEDON == status.get()) && socketStatus.get()) {
				logger.info("writing : " + ByteHexUtil.byteToHex(bytes));
				os.write(bytes);
				os.flush();
			}
			return status.get();
		} catch (Exception e) {
			logger.info("error sending message to npci" + e.getMessage());
			socketStatus.set(false);
			if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
			return status.get();
		}
	}

	public final synchronized Status sendUnchecked(byte[] bytes, Logger logger) {
		try {
			logger.trace("writing : " + ByteHexUtil.byteToHex(bytes));
			os.write(bytes);
			os.flush();
			return status.get();
		} catch (Exception e) {
			logger.info("error sending message to npci" + e.getMessage());
			socketStatus.set(false);
			if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
			return status.get();
		}
	}

	public final byte[] receive() {
		if (Status.SHUTDOWN != status.get()) {
			byte[] bytes = null;
			try {
				int b1 = is.read();
				// logger.info("b1 : "+b1);
				int b2 = is.read();
				// logger.info("b2 : "+b2);
				if (b1 < 0 || b2 < 0) {
					logger.info("unexpected socket break");
					socketStatus.set(false);
					if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
					return null;
				} else {
					// logger.info("len : "+(b1 * 256 + b2));
					bytes = new byte[b1 * 256 + b2];
					is.read(bytes);
					return bytes;
				}
			} catch (Exception e) {
				logger.info("error receiving message from npci socketexception : " + e.getMessage());
				socketStatus.set(false);
				if (Status.LOGGEDON == status.get()) setStatus(Status.NEW);
			}
		}
		return null;
	}

	public final synchronized boolean initSocket() {
		if (Status.SHUTDOWN != status.get()) {
			socketStatus.set(false);
			closeSocket();
			try {
				logger.info("connecting to " + config.npciIp + ":" + config.npciPort);
				socket = new Socket(config.npciIp, config.npciPort);
				socket.setKeepAlive(true);
				is = socket.getInputStream();
				os = socket.getOutputStream();
				socketStatus.set(true);
				/* if(Status.LOGGEDON == status.get()) */
				setStatus(Status.NEW);
				new Logon().start();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public final void closeSocket() {
		try {
			if (socket != null) socket.close();
			socket = null;
			os     = null;
			is     = null;
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public final boolean shutdown() {
		if (Status.SHUTDOWN == status.get()) return status.get();
		try {
			DataSource.shutdown();
			setStatus(Status.SHUTDOWN);
			logger.info("canceling logon task : " + Schedular.cancelLogonTasks());
			Schedular.getSchedular().shutdownNow();
			closeSocket();
			URLClassLoader classLoader = (URLClassLoader) NPCISocket.class.getClassLoader();
			classLoader.close();
			Runtime.getRuntime().gc();
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public final Status getStatus() {
		return status.get();
	}

	public final void setStatus(Status status) {
		this.status.set(status);
		config.statusReceiver.notify(status);
	}

	public final boolean isConnected() {
		if (socket != null && socket.isConnected()) return true;
		return false;
	}

}
