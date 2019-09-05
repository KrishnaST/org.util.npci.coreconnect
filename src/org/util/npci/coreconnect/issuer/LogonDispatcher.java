package org.util.npci.coreconnect.issuer;

import java.util.List;

import org.util.iso8583.ISO8583Message;
import org.util.iso8583.npci.LogonType;
import org.util.iso8583.npci.MTI;
import org.util.npci.coreconnect.CoreConfig;

public class LogonDispatcher extends IssuerDispatcher {

	private final List<String> logonTypes = List.of(LogonType.LOGON, LogonType.ECHO_LOGON, LogonType.LOGOFF);

	public LogonDispatcher(final CoreConfig config) {
		super(config);

	}

	@Override
	public final String getName() {
		return "LOGON";
	}

	@Override
	public boolean dispatch(final ISO8583Message request) {
		if (MTI.NET_MGMT_REQUEST.equals(request.get(0)) && logonTypes.contains(request.get(70))) {
			return config.schedular.execute(new IssuerLogon(request, this));
		}
		return false;
	}

	@Override
	public boolean shutdown() {
		return true;
	}

}