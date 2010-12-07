/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;

/**
 * Converts YieldConvention instances to/from a Fudge string type.
 */
public final class YieldSecondaryType extends SecondaryFieldType<YieldConvention, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final YieldSecondaryType INSTANCE = new YieldSecondaryType();

  private YieldSecondaryType() {
    super(StringFieldType.INSTANCE, YieldConvention.class);
  }

  @Override
  public String secondaryToPrimary(YieldConvention object) {
    return object.getConventionName();
  }

  @Override
  public YieldConvention primaryToSecondary(final String string) {
    return YieldConventionFactory.INSTANCE.getYieldConvention(string);
  }

}
