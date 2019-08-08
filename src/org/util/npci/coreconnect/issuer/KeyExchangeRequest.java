package org.util.npci.coreconnect.issuer;

import org.util.hsm.api.HSMConfig;
import org.util.hsm.api.constants.KeyScheme;
import org.util.hsm.api.constants.KeyType;
import org.util.hsm.api.model.GenKeyResponse;
import org.util.iso8583.ISO8583Message;
import org.util.nanolog.Logger;
import org.util.npci.api.model.DefaultHSMConfig;

public abstract class KeyExchangeRequest<T extends IssuerDispatcher> extends IssuerTransaction<T>  {

	public KeyExchangeRequest(final ISO8583Message request, final T dispatcher) {
		super(request, dispatcher);
	}

	@Override
	protected final void execute(Logger logger) {
		try {
			logger.info("key change request from npci.");
			final String zpk_zmk 	= request.get(48).substring(0, 32);
			final String kcv 		= request.get(48).substring(32);
			logger.info("received kcv : "+kcv);
			final String zmk		= getZMK();
			final DefaultHSMConfig defaultHSMConfig = dispatcher.config.defaultHSMConfig;
			final HSMConfig hsmConfig = HSMConfig.newBuilder(defaultHSMConfig.host, defaultHSMConfig.port)
										.withDecimalizationTable(defaultHSMConfig.decTab)
										.withLengthOfPinLMK(defaultHSMConfig.lengthOfPinLMK)
										.withMaximumPinLength(defaultHSMConfig.maximumPinLength)
										.withMinimumPinLength(defaultHSMConfig.minimumPinLength).build();
			final GenKeyResponse response = dispatcher.config.hsmService.key().importKey(hsmConfig, KeyType.ZPK, KeyScheme.VARIANT.DOUBLE_LEN, zpk_zmk, zmk, logger);
			if(response == GenKeyResponse.IO) logger.error("hsm error.");
			else if(response.isSuccess) {
				logger.info("key import successfully. kcv match : "+kcv.equalsIgnoreCase(response.kcv));
				processKey(response.keyUnderLMK);
			}
			else {
				logger.info("key import failure : "+response.responseCode);
			}
		} catch (Exception e) {logger.error(e);}
	}
	
	public abstract String getZMK();
	
	public abstract void processKey(final String zpk);

}
