/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Allows a {@link CurrencyPair} to be stored as a string in a Fudge message.
 */
public class CurrencyPairFudgeSecondaryType extends SecondaryFieldType<CurrencyPair, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final CurrencyPairFudgeSecondaryType INSTANCE = new CurrencyPairFudgeSecondaryType();

  protected CurrencyPairFudgeSecondaryType() {
    super(FudgeWireType.STRING, CurrencyPair.class);
  }

  /**
   * @param currencyPair The currency pair
   * @return The currency pair as a string in the form AAA/BBB
   */
  @Override
  public String secondaryToPrimary(CurrencyPair currencyPair) {
    return currencyPair.getName();
  }

  /**
   * @param currencyPairString The currency pair in the form AAA/BBB
   * @return The currency pair
   */
  @Override
  public CurrencyPair primaryToSecondary(String currencyPairString) {
    return CurrencyPair.of(currencyPairString);
  }
}
