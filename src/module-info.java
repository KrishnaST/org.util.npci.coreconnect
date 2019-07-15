/**
 * @author krishna.telgave
 */
module org.util.npci.coreconnect {

	exports org.util.npci.coreconnect;
	exports org.util.npci.coreconnect.acquirer;
	exports org.util.npci.coreconnect.issuer;
	exports org.util.npci.coreconnect.logon;
	exports org.util.npci.coreconnect.util;
	
	requires transitive java.sql;
	requires transitive com.zaxxer.hikari;
	requires transitive org.util.datautil;
	requires transitive org.util.npci.api;
	requires transitive org.util.iso8583;
	requires transitive org.util.nanolog;
	requires transitive org.util.iso8583.npci;
	
	requires transitive okhttp3;
	requires transitive okio;
	requires transitive retrofit2;
	requires transitive retrofit2.converter.jackson;
	
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.annotation;

	uses org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder;
	uses org.util.npci.coreconnect.acquirer.AcquirerServerBuilder;

	provides org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder with org.util.npci.coreconnect.issuer.LogonDispatcherBuilder;
}