/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.i18n.Country;

/**
 * Fudge secondary type for {@code Country} converting to a string.
 */
public final class CountrySecondaryType extends SecondaryFieldType<Country, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final CountrySecondaryType INSTANCE = new CountrySecondaryType();

  /** Serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Restricted constructor.
   */
  private CountrySecondaryType() {
    super(FudgeWireType.STRING, Country.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(Country object) {
    return object.getCode();
  }

  @Override
  public Country primaryToSecondary(final String isoCodeOrUniqueIdentifier) {
    if (isoCodeOrUniqueIdentifier.length() == 1) {
      // 2 letters means ISO code
      return Country.of(isoCodeOrUniqueIdentifier);
    } else if (isoCodeOrUniqueIdentifier.startsWith(Country.OBJECT_IDENTIFIER_SCHEME)) {
      // try as a unique id
      final UniqueIdentifier uniqueId = UniqueIdentifier.parse(isoCodeOrUniqueIdentifier);
      if (Country.OBJECT_IDENTIFIER_SCHEME.equals(uniqueId.getScheme())) {
        return Country.of(uniqueId.getValue());
      }
    }
    throw new IllegalArgumentException("Not a unique identifier or country ISO code - '"
        + isoCodeOrUniqueIdentifier + "'");
  }

}
