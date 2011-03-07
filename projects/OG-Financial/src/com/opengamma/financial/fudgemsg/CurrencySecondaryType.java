/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.id.UniqueIdentifier;

/**
 * Converts Currency instances to/from a Fudge string type.
 */
public final class CurrencySecondaryType extends SecondaryFieldType<CurrencyUnit, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final CurrencySecondaryType INSTANCE = new CurrencySecondaryType();

  private CurrencySecondaryType() {
    super(StringFieldType.INSTANCE, CurrencyUnit.class);
  }

  @Override
  public String secondaryToPrimary(CurrencyUnit object) {
    return object.getCode();
  }

  @Override
  public CurrencyUnit primaryToSecondary(final String isoCodeOrUniqueIdentifier) {
    if (isoCodeOrUniqueIdentifier.length() == 3) {
      // 3 letters means ISO code
      return CurrencyUnit.of(isoCodeOrUniqueIdentifier);
    } else {
      // Otherwise, try as a UID
      final UniqueIdentifier uid = UniqueIdentifier.parse(isoCodeOrUniqueIdentifier);
      if (CurrencyUnit.OBJECT_IDENTIFIER_SCHEME.equals(uid.getScheme())) {
        return CurrencyUnit.of(uid.getValue());
      } else {
        throw new IllegalArgumentException("Not a unique identifier or currency ISO code - '"
            + isoCodeOrUniqueIdentifier + "'");
      }
    }
  }

}
