/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.core.common.Currency;
import com.opengamma.id.UniqueIdentifier;

/**
 * Converts Currency instances to/from a Fudge string type.
 */
public final class CurrencySecondaryType extends SecondaryFieldType<Currency, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final CurrencySecondaryType INSTANCE = new CurrencySecondaryType();

  private CurrencySecondaryType() {
    super(StringFieldType.INSTANCE, Currency.class);
  }

  @Override
  public String secondaryToPrimary(Currency object) {
    return object.getISOCode();
  }

  @Override
  public Currency primaryToSecondary(final String isoCodeOrUniqueIdentifier) {
    if (isoCodeOrUniqueIdentifier.length() == 3) {
      // 3 letters means ISO code
      return Currency.getInstance(isoCodeOrUniqueIdentifier);
    } else {
      // Otherwise, try as a UID
      final UniqueIdentifier uid = UniqueIdentifier.parse(isoCodeOrUniqueIdentifier);
      if (Currency.IDENTIFICATION_DOMAIN.equals(uid.getScheme())) {
        return Currency.getInstance(uid.getValue());
      } else {
        throw new IllegalArgumentException("Not a unique identifier or currency ISO code - '"
            + isoCodeOrUniqueIdentifier + "'");
      }
    }
  }

}
