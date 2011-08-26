/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Utilities and constants for {@code Exchange}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class ExchangeUtils {

  /**
   * Identification scheme for the MIC exchange code ISO standard.
   */
  public static final ExternalScheme ISO_MIC = ExternalScheme.of("ISO_MIC");

  /**
   * Restricted constructor.
   */
  protected ExchangeUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an ISO MIC code.
   * <p>
   * Examples might be {@code XLON} or {@code XNYS}.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static ExternalId isoMicExchangeId(String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z0-9]{4}([-][A-Z0-9]{3})?") == false) {
      throw new IllegalArgumentException("ISO MIC code is invalid: " + code);
    }
    return ExternalId.of(ISO_MIC, code);
  }

}
