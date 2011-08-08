/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.UniqueId;
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
  public Country primaryToSecondary(final String isoCodeOrUniqueId) {
    if (isoCodeOrUniqueId.length() == 1) {
      // 2 letters means ISO code
      return Country.of(isoCodeOrUniqueId);
    } else if (isoCodeOrUniqueId.startsWith(Country.OBJECT_SCHEME)) {
      // try as a unique id
      final UniqueId uniqueId = UniqueId.parse(isoCodeOrUniqueId);
      if (Country.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
        return Country.of(uniqueId.getValue());
      }
    }
    throw new IllegalArgumentException("Not a unique identifier or country ISO code - '"
        + isoCodeOrUniqueId + "'");
  }

}
