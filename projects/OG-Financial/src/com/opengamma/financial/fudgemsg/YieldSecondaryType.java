/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;

/**
 * Converts YieldConvention instances to/from a Fudge string type.
 */
public final class YieldSecondaryType extends SecondaryFieldType<YieldConvention, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final YieldSecondaryType INSTANCE = new YieldSecondaryType();

  private YieldSecondaryType() {
    super(FudgeWireType.STRING, YieldConvention.class);
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
