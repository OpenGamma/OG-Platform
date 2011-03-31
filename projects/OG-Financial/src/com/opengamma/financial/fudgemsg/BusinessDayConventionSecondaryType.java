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
public final class BusinessDayConventionSecondaryType extends SecondaryFieldType<BusinessDayConvention, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final BusinessDayConventionSecondaryType INSTANCE = new BusinessDayConventionSecondaryType();

  private BusinessDayConventionSecondaryType() {
    super(FudgeWireType.STRING, BusinessDayConvention.class);
  }

  @Override
  public String secondaryToPrimary(BusinessDayConvention object) {
    return object.getConventionName();
  }

  @Override
  public BusinessDayConvention primaryToSecondary(final String string) {
    return BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(string);
  }

}
