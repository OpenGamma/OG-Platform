/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Converts DayCount instances to/from a Fudge string type.
 */
public final class DayCountFudgeSecondaryType extends SecondaryFieldType<DayCount, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final DayCountFudgeSecondaryType INSTANCE = new DayCountFudgeSecondaryType();

  private DayCountFudgeSecondaryType() {
    super(FudgeWireType.STRING, DayCount.class);
  }

  @Override
  public String secondaryToPrimary(DayCount object) {
    return object.getName();
  }

  @Override
  public DayCount primaryToSecondary(final String string) {
    return DayCountFactory.of(string);
  }

}
