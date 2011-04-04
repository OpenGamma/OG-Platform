/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import javax.time.calendar.Period;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.time.Tenor;

/**
 * Fudge secondary type for {@code Tenor} converting to a string.
 */
public final class TenorSecondaryType extends SecondaryFieldType<Tenor, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final TenorSecondaryType INSTANCE = new TenorSecondaryType();

  /** Serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Restricted constructor.
   */
  private TenorSecondaryType() {
    super(FudgeWireType.STRING, Tenor.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(Tenor object) {
    return object.getPeriod().toString();
  }

  @Override
  public Tenor primaryToSecondary(final String string) {
    return new Tenor(Period.parse(string));
  }

}
