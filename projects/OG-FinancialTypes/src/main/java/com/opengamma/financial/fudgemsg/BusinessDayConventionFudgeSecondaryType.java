/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;

/**
 * Converts BusinessDayConvention instances to/from a Fudge string type.
 */
public final class BusinessDayConventionFudgeSecondaryType extends SecondaryFieldType<BusinessDayConvention, String> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final BusinessDayConventionFudgeSecondaryType INSTANCE = new BusinessDayConventionFudgeSecondaryType();

  private BusinessDayConventionFudgeSecondaryType() {
    super(FudgeWireType.STRING, BusinessDayConvention.class);
  }

  @Override
  public String secondaryToPrimary(BusinessDayConvention object) {
    return object.getName();
  }

  @Override
  public BusinessDayConvention primaryToSecondary(final String string) {
    return BusinessDayConventionFactory.of(string);
  }

}
