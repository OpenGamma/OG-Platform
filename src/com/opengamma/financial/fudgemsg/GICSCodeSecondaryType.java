/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.financial.GICSCode;

/**
 * Converts GICSCode instances to/from a Fudge string type.
 */
public final class GICSCodeSecondaryType extends SecondaryFieldType<GICSCode, String> {

  /**
   * Singleton instance of the type.
   */
  public static final GICSCodeSecondaryType INSTANCE = new GICSCodeSecondaryType();

  private GICSCodeSecondaryType() {
    super(StringFieldType.INSTANCE, GICSCode.class);
  }

  @Override
  public String secondaryToPrimary(GICSCode object) {
    return object.toString();
  }

  @Override
  public GICSCode primaryToSecondary(final String code) {
    return GICSCode.getInstance(code);
  }

}
