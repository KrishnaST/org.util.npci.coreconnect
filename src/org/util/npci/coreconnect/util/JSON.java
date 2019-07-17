package org.util.npci.coreconnect.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public final class JSON {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
	
	public static final String toJson(final Object o) {
		try {
			return OBJECT_WRITER.writeValueAsString(o);
		} catch (JsonProcessingException e) {}
		return null;
	}
	
	public static final <T> T fromJsom(final String json, final Class<T> classz) {
		try {
			return OBJECT_MAPPER.readValue(json, classz);
		} catch (IOException e) {}
		return null;
	}
}
