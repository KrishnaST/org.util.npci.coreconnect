package org.util.npci.coreconnect;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;

public interface CoreDatabaseService {

	public abstract long registerTransaction(final ISO8583Message message, final String type, final Logger logger);

	public abstract ISO8583Message getTransaction(final long id, final Logger logger);
	
	public abstract ISO8583Message getTransaction(final String key, final Logger logger);
	
	public abstract boolean registerResponse(final long id, final ISO8583Message response, final Logger logger);
	
	public abstract boolean registerResponse(final String key, final ISO8583Message response, final Logger logger);
	
	public abstract boolean isDuplicateTransaction(final String key, final Logger logger);
}
