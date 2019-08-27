package org.util.npci.coreconnect;

import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;

public class CoreDatabaseService {

	public long registerTransaction(final ISO8583Message message, final String type, final Logger logger) {
		return 0;
	}
	
	public ISO8583Message getTransactionById(final long id, final Logger logger) {
		return null;
	}
	
	public ISO8583Message getTransactionByKey(final String key, final Logger logger) {
		return null;
	}
	
	public final boolean registerResponseById(final int id, final ISO8583Message response, final Logger logger) {
		return false;
	}
	
	public final boolean registerResponseByKey(final String key, final ISO8583Message response, final Logger logger) {
		return false;
	}
	
	/*
	  
	  

	  
	  
	 */
	
}
