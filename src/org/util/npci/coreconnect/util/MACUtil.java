package org.util.npci.coreconnect.util;

import org.util.hsm.api.constants.InputFormat;
import org.util.hsm.api.constants.MACAlgorithm;
import org.util.hsm.api.constants.MACKeyType;
import org.util.hsm.api.constants.MACMode;
import org.util.hsm.api.constants.MACPadding;
import org.util.hsm.api.constants.MACSize;
import org.util.hsm.api.model.MACResponse;
import org.util.nanolog.Logger;
import org.util.npci.coreconnect.CoreConfig;

public class MACUtil {

	public static final MACResponse calculateMAC(final CoreConfig config, final String MAB, final Logger logger) {
		return config.hsmService.mac().calculateMAC(config.hsmConfig, MACMode.ONLY_BLOCK_OF_SINGLE_BLOCK_MESSAGE, InputFormat.BINARY,
				MACSize.SIXTEEN_HEX_DIGITS, MACAlgorithm.ISO9797MAC_ALG3, MACPadding.ISO9797_PADDING1, MACKeyType.ZAC, config.defaultHSMConfig.macKey, null,
				MAB.getBytes(), logger);
	}

	public static final MACResponse validateMAC(final CoreConfig config, final String MAB, final String MAC, final Logger logger) {
		return config.hsmService.mac().validateMAC(config.hsmConfig, MACMode.ONLY_BLOCK_OF_SINGLE_BLOCK_MESSAGE, InputFormat.BINARY, MACSize.SIXTEEN_HEX_DIGITS,
				MACAlgorithm.ISO9797MAC_ALG3, MACPadding.ISO9797_PADDING1, MACKeyType.ZAC, config.defaultHSMConfig.macKey, null, MAB.getBytes(), MAC, logger);
	}
}
