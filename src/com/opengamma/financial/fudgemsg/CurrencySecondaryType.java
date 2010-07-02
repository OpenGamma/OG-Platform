/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.financial.Currency;

/**
 * Converts Currency instances to/from a Fudge string type.
 */
public final class CurrencySecondaryType extends SecondaryFieldType<Currency, String> {

  /**
   * Singleton instance of the type.
   */
  public static final CurrencySecondaryType INSTANCE = new CurrencySecondaryType();

  private CurrencySecondaryType() {
    super(StringFieldType.INSTANCE, Currency.class);
  }

  @Override
  public String secondaryToPrimary(Currency object) {
    return object.getISOCode();
  }

  @Override
  public Currency primaryToSecondary(final String isoCode) {
    return Currency.getInstance(isoCode);
  }

}
