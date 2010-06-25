/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * Registers custom builders for OG-Financial objects
 */
public class FinancialFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * The singleton Fudge context
   */
  public static final FinancialFudgeContextConfiguration INSTANCE = new FinancialFudgeContextConfiguration();

  @Override
  public void configureFudgeObjectDictionary(FudgeObjectDictionary dictionary) {
    // none at the moment
  }

}
