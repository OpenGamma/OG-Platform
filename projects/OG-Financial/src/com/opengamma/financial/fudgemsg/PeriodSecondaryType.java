/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.Period;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * Converts DayCount instances to/from a Fudge string type.
 */
public final class PeriodSecondaryType extends SecondaryFieldType<Period, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final PeriodSecondaryType INSTANCE = new PeriodSecondaryType();

  private PeriodSecondaryType() {
    super(StringFieldType.INSTANCE, Period.class);
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
