/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.fudgemsg.types.SecondaryFieldType;

import com.opengamma.master.security.financial.GICSCode;

/**
 * Converts GICSCode instances to/from a Fudge integer type.
 */
public final class GICSCodeSecondaryType extends SecondaryFieldType<GICSCode, Integer> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final GICSCodeSecondaryType INSTANCE = new GICSCodeSecondaryType();

  private GICSCodeSecondaryType() {
    super(PrimitiveFieldTypes.INT_TYPE, GICSCode.class);
  }

  @Override
  public Integer secondaryToPrimary(GICSCode object) {
    return object.getCode();
  }

  @Override
  public GICSCode primaryToSecondary(final Integer code) {
    return GICSCode.getInstance(code);
  }

  @Override
  public boolean canConvertPrimary(Class<? extends Integer> clazz) {
    return Integer.class.isAssignableFrom(clazz) || Integer.TYPE.isAssignableFrom(clazz);
  }

}
