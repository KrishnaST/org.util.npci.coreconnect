package org.util.npci.coreconnect.util;

import org.util.iso8583.ISO8583Message;

public final class NPCIISOUtil {

	public static final boolean removeNotRequiredElements(final ISO8583Message message) {
		message.remove(14);
		message.remove(18);
		message.remove(22);
		message.remove(25);
		message.remove(35);
		message.remove(40);
		message.remove(42);
		message.remove(43);
		message.remove(52);
		message.remove(61);
		return false;
	}
}
