/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudge;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Fudge secondary type for {@code Currency} converting to a string.
 */
public final class CurrencySecondaryType extends SecondaryFieldType<Currency, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final CurrencySecondaryType INSTANCE = new CurrencySecondaryType();

  /** Serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Restricted constructor.
   */
  private CurrencySecondaryType() {
    super(StringFieldType.INSTANCE, Currency.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(Currency object) {
    return object.getCode();
  }

  @Override
  public Currency primaryToSecondary(final String isoCodeOrUniqueIdentifier) {
    if (isoCodeOrUniqueIdentifier.length() == 3) {
      // 3 letters means ISO code
      return Currency.of(isoCodeOrUniqueIdentifier);
    } else {
      // Otherwise, try as a UID
      final UniqueIdentifier uid = UniqueIdentifier.parse(isoCodeOrUniqueIdentifier);
      if (Currency.OBJECT_IDENTIFIER_SCHEME.equals(uid.getScheme())) {
        return Currency.of(uid.getValue());
      } else {
        throw new IllegalArgumentException("Not a unique identifier or currency ISO code - '"
            + isoCodeOrUniqueIdentifier + "'");
      }
    }
  }

}
