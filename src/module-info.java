/**
 * @author krishna.telgave
 */
module org.util.npci.coreconnect {

	requires transitive java.sql;
	requires transitive com.zaxxer.hikari;
	requires transitive org.util.datautil;
	requires transitive org.util.npci.api;
	requires transitive org.util.iso8583;
	requires transitive org.util.nanolog;
	requires transitive org.util.iso8583.npci;

	exports org.util.npci.coreconnect;
	exports org.util.npci.coreconnect.acquirer;
	exports org.util.npci.coreconnect.issuer;
	exports org.util.npci.coreconnect.logon;

	uses org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder;
	uses org.util.npci.coreconnect.acquirer.AcquirerServerBuilder;

	provides org.util.npci.coreconnect.issuer.IssuerDispatcherBuilder with org.util.npci.coreconnect.issuer.LogonDispatcherBuilder;
}