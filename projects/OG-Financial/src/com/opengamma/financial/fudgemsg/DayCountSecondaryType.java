/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Converts DayCount instances to/from a Fudge string type.
 */
public final class DayCountSecondaryType extends SecondaryFieldType<DayCount, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final DayCountSecondaryType INSTANCE = new DayCountSecondaryType();

  private DayCountSecondaryType() {
    super(StringFieldType.INSTANCE, DayCount.class);
  }

  @Override
  public String secondaryToPrimary(DayCount object) {
    return object.getConventionName();
  }

  @Override
  public DayCount primaryToSecondary(final String string) {
    return DayCountFactory.INSTANCE.getDayCount(string);
  }

}
