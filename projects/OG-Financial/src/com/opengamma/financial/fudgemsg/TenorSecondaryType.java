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

import com.opengamma.util.time.Tenor;

/**
 * Converts DayCount instances to/from a Fudge string type.
 */
public final class TenorSecondaryType extends SecondaryFieldType<Tenor, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final TenorSecondaryType INSTANCE = new TenorSecondaryType();

  private TenorSecondaryType() {
    super(StringFieldType.INSTANCE, Tenor.class);
  }

  @Override
  public String secondaryToPrimary(Tenor object) {
    return object.getPeriod().toString();
  }

  @Override
  public Tenor primaryToSecondary(final String string) {
    return new Tenor(Period.parse(string));
  }

}
