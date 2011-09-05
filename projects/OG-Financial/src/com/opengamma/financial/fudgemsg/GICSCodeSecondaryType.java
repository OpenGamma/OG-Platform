/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.security.equity.GICSCode;

/**
 * Converts GICSCode instances to/from a Fudge integer type.
 */
public final class GICSCodeSecondaryType extends SecondaryFieldType<GICSCode, Integer> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final GICSCodeSecondaryType INSTANCE = new GICSCodeSecondaryType();

  private GICSCodeSecondaryType() {
    super(FudgeWireType.INT, GICSCode.class);
  }

  @Override
  public Integer secondaryToPrimary(GICSCode object) {
    return object.getCodeInt();
  }

  @Override
  public GICSCode primaryToSecondary(final Integer code) {
    return GICSCode.of(code);
  }

  @Override
  public boolean canConvertPrimary(Class<? extends Integer> clazz) {
    return Integer.class.isAssignableFrom(clazz) || Integer.TYPE.isAssignableFrom(clazz);
  }

}
