package org.util.npci.coreconnect.logon;

import java.util.Date;
import java.util.Random;

import org.util.iso8583.EncoderDecoder;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.ext.ISO8583DateField;
import org.util.iso8583.npci.LogonType;
import org.util.iso8583.npci.MTI;
import org.util.nanolog.Logger;
import org.util.npci.api.Status;
import org.util.npci.coreconnect.CoreConfig;
import org.util.npci.coreconnect.acquirer.NoLogAcquirerTransaction;

//@formatter:off
public final class ScheduledLogon extends NoLogAcquirerTransaction  {

	public ScheduledLogon(CoreConfig config) {
		super(config);
	}

	private final Random random = new Random();

	@Override
	protected final void execute(final Logger logger) {
		final String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("scheduled-echo");
		try {
			final Date date = new Date();
			final ISO8583Message request = new ISO8583Message();
			request.put(0, MTI.NET_MGMT_REQUEST);
			request.put(7, ISO8583DateField.getISODate(ISO8583DateField.TRANSMISSION, date));
			request.put(11, String.format("%06d", random.nextInt(999999)));
			request.put(70, LogonType.LOGON);
			logger.info("logon request  sent : " + EncoderDecoder.log(request));
			final ISO8583Message response = config.coreconnect.sendRequestToNPCI(request, logger, 15000);
			logger.info("logon response rcvd : " + EncoderDecoder.log(response));
			if (response != null && "00".equals(response.get(39)) && Status.SHUTDOWN != config.coreconnect.getStatus()) config.coreconnect.setStatus(Status.LOGGEDON);
			else config.coreconnect.setStatus(Status.NEW);
		} catch (Exception e) {logger.info(e);} 
		finally {Thread.currentThread().setName(oldName == null ? "" : oldName);}
	}

}
