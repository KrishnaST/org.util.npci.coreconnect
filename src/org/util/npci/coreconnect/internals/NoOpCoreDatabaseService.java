package org.util.npci.coreconnect.internals;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreDatabaseService;

public final class NoOpCoreDatabaseService implements CoreDatabaseService {

	@Override
	public final long registerTransaction(ISO8583Message message, String type, Logger logger) {
		return 0;
	}

	@Override
	public final ISO8583Message getTransaction(long id, Logger logger) {
		return null;
	}

	@Override
	public final ISO8583Message getTransaction(String key, Logger logger) {
		return null;
	}

	@Override
	public final boolean registerResponse(long id, ISO8583Message response, Logger logger) {
		return false;
	}

	@Override
	public final boolean registerResponse(String key, ISO8583Message response, Logger logger) {
		return false;
	}

	@Override
	public final boolean isDuplicateTransaction(String key, Logger logger) {
		return false;
	}

}
