package org.util.npci.coreconnect.logon;

import java.util.Date;
import java.util.Random;

import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.npci.LogonType;
import org.util.iso8583.npci.MTI;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.acquirer.AcquirerServer;
import org.util.npci.coreconnect.acquirer.AcquirerTransaction;

public class Logon extends AcquirerTransaction<AcquirerServer> {

	private final Random random = new Random();
	
	public Logon() {
		super(null, null);
	}

	@Override
	protected void execute(Logger logger) {
		try {
			request = new ISO8583Message();
			request.put(0, MTI.NET_MGMT_REQUEST);
			request.put(11, String.format("%06d", random.nextInt(999999)));
			request.put(7, de7Formatter.format(new Date()));
			request.put(70, LogonType.LOGON);
			logger.info("logon request  sent : "+EncoderDecoder.log(request));
			byte[] bytes = EncoderDecoder.encode(npciFormat, request);
			npres = Dispatcher.sendAcquirerRequest(request.get(0), request.getUniqueKey(), bytes, logger);
			logger.info("logon response rcvd : "+EncoderDecoder.log(npres));
			if(npres != null && npres.get(39) != null && npres.get(39).equals("00")) {
				if(Status.NEW == NPCISocket.getStatus() || Status.ONUSLOGOFF == NPCISocket.getStatus()) NPCISocket.setStatus(Status.LOGGEDON);
			}
			else {
				Thread.sleep(2000);
				new Logon().start();
			}
		} catch (Exception e) {logger.info(e);}
	}


}
