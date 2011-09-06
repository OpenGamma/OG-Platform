/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.Period;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Converts Period instances to/from a Fudge string type.
 */
public final class PeriodFudgeSecondaryType extends SecondaryFieldType<Period, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final PeriodFudgeSecondaryType INSTANCE = new PeriodFudgeSecondaryType();

  private PeriodFudgeSecondaryType() {
    super(FudgeWireType.STRING, Period.class);
  }

  @Override
  public String secondaryToPrimary(Period object) {
    return object.toString();
  }

  @Override
  public Period primaryToSecondary(final String string) {
    return Period.parse(string);
  }

}
