package org.util.npci.coreconnect.logon;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.npci.LogonType;
import org.util.iso8583.npci.MTI;

import com.sil.npci.acquirer.iso8583.ISO8583AcquirerTransaction;
import com.sil.npci.dispatcher.NPCISocket;

import sun.rmi.server.Dispatcher;

public class ScheduledLogon2 extends ISO8583AcquirerTransaction {

	private Date lastKeyXchangeDt = new Date(new Date().getTime()-3600000);
	
	public ScheduledLogon2() {
		super(null, null);
	}

	private final Random random = new Random();

	@Override
	protected void execute() {
		try {
			if(NPCISocket.getStatus() == Status.SHUTDOWN || !NPCISocket.isConnected()) return;
			npreq.put(0, MTI.NET_MGMT_REQUEST);
			npreq.put(11, String.format("%06d", random.nextInt(999999)));
			npreq.put(7, de7Formatter.format(new Date()));
			if (Status.NEW == NPCISocket.getStatus()) 				npreq.put(70, LogonType.LOGON);
			else if (Status.LOGGEDON == NPCISocket.getStatus()) 	npreq.put(70, LogonType.ECHO_LOGON);
			if(Status.LOGGEDON == NPCISocket.getStatus() && isExchangeTime(lastKeyXchangeDt)) {
				npreq.put(70, LogonType.ZPK_REQUEST);
				lastKeyXchangeDt = new Date();
			}
			byte[] bytes = EncoderDecoder.encode(npciFormat, npreq);
			logger.info("bytes : "+ByteHexUtil.byteToHex(bytes));
			logger.info(""+EncoderDecoder.log(npreq));
			npres = Dispatcher.sendAcquirerRequest(npreq.get(0), npreq.getUniqueKey(), bytes, logger);
			logger.info(""+EncoderDecoder.log(npres));
			
			if (npres == null) {
				logger.info("no logon response");
				if(npreq.get(70).equals(LogonType.LOGON)) {
					execute();
				}
				return;
			}
			if(npres.get(39).equals("00")) {
				if (npres.get(70).equals(LogonType.LOGON)) {
					if(Status.NEW == NPCISocket.getStatus()) NPCISocket.setStatus(Status.LOGGEDON);
				}
				else if (npres.get(70).equals(LogonType.ECHO_LOGON)) {
					if (npres.get(39).equals("00")) logger.info("echo logon success");
				}
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}
	
	public static final boolean isExchangeTime(final Date lastxchangeDate) {
		Date currentDate = new Date();
		if((currentDate.getTime()-lastxchangeDate.getTime()) > 10*60*1000 && Math.abs(currentDate.getTime() - roundDate().getTime())/60000 < 10) {
			return true;
		}
		return false;
	}
	
	public static final Date roundDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		if(calendar.get(Calendar.HOUR_OF_DAY) > 12) calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
	}
	
}