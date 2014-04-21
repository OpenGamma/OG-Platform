/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Fudge secondary type for {@code Tenor} converting to a string.
 */
public final class TenorFudgeSecondaryType extends SecondaryFieldType<Tenor, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final TenorFudgeSecondaryType INSTANCE = new TenorFudgeSecondaryType();

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Restricted constructor.
   */
  private TenorFudgeSecondaryType() {
    super(FudgeWireType.STRING, Tenor.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(Tenor object) {
    return object.toFormattedString();
  }

  @Override
  public Tenor primaryToSecondary(final String string) {
    return Tenor.parse(string);
  }

}
