/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange;

import com.opengamma.id.IdentificationScheme;

/**
 * Utilities and constants for exchanges.
 */
public class ExchangeUtils {

  /**
   * Identification scheme for the MIC exchange code ISO standard.
   */
  public static final IdentificationScheme ISO_MIC = new IdentificationScheme("ISO_MIC");
  /**
   * Identification scheme for the Copp-Clark holiday data provider 'name' field.
   */
  public static final IdentificationScheme COPP_CLARK_NAME = new IdentificationScheme("COPP_CLARK_NAME");
  /**
   * Identification scheme for the Copp-Clark holiday data provider 'centre id' field.
   */
  public static final IdentificationScheme COPP_CLARK_CENTER_ID = new IdentificationScheme("COPP_CLARK_CENTER_ID");

  /**
   * Restricted constructor.
   */
  protected ExchangeUtils() {
  }

}
