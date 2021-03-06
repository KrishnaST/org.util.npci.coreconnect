package org.util.npci.coreconnect.issuer;

import org.util.iso8583.ISO8583Message;
import org.util.iso8583.npci.LogonType;
import org.util.iso8583.npci.MTI;
import org.util.nanolog.Logger;
import org.util.npci.api.Status;
import org.util.npci.coreconnect.CoreConnect;

public final class IssuerLogon extends NoLogIssuerTransaction<IssuerDispatcher> {

	public IssuerLogon(ISO8583Message request, IssuerDispatcher dispatcher) {
		super(request, dispatcher);
	}

	@Override
	protected void execute(Logger logger) {
		try {
			final CoreConnect coreconnect = config.coreconnect;
			if (LogonType.LOGON.equals(request.get(70))) {
				if (Status.SHUTDOWN != coreconnect.getStatus()) {
					if (Status.LOGOFF == coreconnect.getStatus()) dispatcher.config.controller.action("enable-logon");
					coreconnect.setStatus(Status.LOGGEDON);
				}
			} else if (LogonType.LOGOFF.equals(request.get(70))) {
				if (Status.SHUTDOWN != coreconnect.getStatus()) {
					dispatcher.config.controller.action("disable-logon");
					coreconnect.setStatus(Status.LOGOFF);
				}
			} else if (LogonType.ECHO_LOGON.equals(request.get(70))) {
				if (Status.SHUTDOWN != coreconnect.getStatus()) coreconnect.setStatus(Status.LOGGEDON);
			} else if (LogonType.CUTOVER.equals(request.get(70))) { if (Status.SHUTDOWN != coreconnect.getStatus()) { logger.info("cutover message."); } }
			request.put(0, MTI.NET_MGMT_RESPONSE);
			request.put(39, "00");
			final boolean isSent = sendResponseToNPCI(request, null, logger);
			logger.info("isSent : " + isSent);
		} catch (Exception e) {
			logger.info(e);
		}
	}

}
