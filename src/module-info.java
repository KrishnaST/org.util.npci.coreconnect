/**
 * @author krishna.telgave
 */
module org.util.npci.coreconnect {

	requires transitive java.sql;
	requires transitive org.util.datautil;
	requires transitive org.util.npci.api;
	requires transitive org.util.iso8583;
	requires transitive org.util.nanolog;
	requires transitive org.util.iso8583.npci;
	requires com.zaxxer.hikari;
	
	exports org.util.npci.coreconnect;
	exports org.util.npci.coreconnect.acquirer;
	exports org.util.npci.coreconnect.issuer;
	
	uses org.util.npci.coreconnect.issuer.IssuerDispatcher;
	
	provides org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder with org.util.npci.coreconnect.issuer.LogonDispatcherBuilder;
}