package org.util.npci.coreconnect.logon;

import java.util.Date;
import java.util.Random;

import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.npci.LogonType;
import org.util.iso8583.npci.MTI;


public class EchoLogon extends ISO8583AcquirerTransaction {

	private final Random random = new Random();
	
	public EchoLogon() {
		super(null, null);
	}

	@Override
	protected void execute() {
		try {
			npreq.put(0, MTI.NET_MGMT_REQUEST);
			npreq.put(11, String.format("%06d", random.nextInt(999999)));
			npreq.put(7, de7Formatter.format(new Date()));
			npreq.put(70, LogonType.ECHO_LOGON);
			logger.info("logon request  sent : "+EncoderDecoder.log(npreq));
			byte[] bytes = EncoderDecoder.encode(npciFormat, npreq);
			npres = Dispatcher.sendAcquirerRequest(npreq.get(0), npreq.getUniqueKey(), bytes, logger);
			logger.info("logon response rcvd : "+EncoderDecoder.log(npres));
		} catch (Exception e) {logger.info(e);}
	}

}
