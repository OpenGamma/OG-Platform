/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;

/**
 * Based on the exchange code, determines whether a future option security is margined or not.
 */
public class FutureOptionMarginResolver {

  /**
   * US exchange codes - should all be margined
   */
  private static final Map<String, Boolean> US_EXCHANGE_CODE_MARGIN_MAPPING = ImmutableMap.of(
      "US", true,
      "CBOE", true,
      "CBOT", true,
      "CME", true);

  /**
   * European exchange codes - should all be margined
   */
  private static final Map<String, Boolean> EU_EXCHANGE_CODE_MARGIN_MAPPING = ImmutableMap.of(
      "LIF", true,
      "EUX", true);

  /**
   * Combined mapping of US & EU exchange codes. (Note we can't use ImmutableMap.of(...) directly
   * for all entries as it is limited to 5 mappings.)
   */
  private static final Map<String, Boolean> EXCHANGE_CODE_MARGIN_MAPPING = ImmutableMap.<String, Boolean>builder()
      .putAll(US_EXCHANGE_CODE_MARGIN_MAPPING)
      .putAll(EU_EXCHANGE_CODE_MARGIN_MAPPING)
      .build();

  /**
   * Indicates if a security with the specified exchange code should be flagged
   * as margined or not. If the exchange code is not one of the recognised codes
   * then an OpenGammaRuntimeException will be thrown.
   *
   * @param exchangeCode the exchange code to derive the margin flag for
   * @return true if the security should be margined, false if not, and an exception
   * if the code is not recognied.
   */
  public boolean isMargined(String exchangeCode) {
    if (EXCHANGE_CODE_MARGIN_MAPPING.containsKey(exchangeCode)) {
      return EXCHANGE_CODE_MARGIN_MAPPING.get(exchangeCode);
    } else {
      throw new OpenGammaRuntimeException("Cannot calculate margined flag from exchangeCode: " + exchangeCode);
    }
  }

}
