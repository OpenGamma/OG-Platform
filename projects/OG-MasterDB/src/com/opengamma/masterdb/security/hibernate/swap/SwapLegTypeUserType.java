/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the SwapLegType enum
 */
public class SwapLegTypeUserType extends EnumUserType<SwapLegType> {

  private static final String FIXED_INTEREST = "Fixed interest";
  private static final String FLOATING_INTEREST = "Floating interest";

  public SwapLegTypeUserType() {
    super(SwapLegType.class, SwapLegType.values());
  }

  @Override
  protected String enumToStringNoCache(SwapLegType value) {
    return value.accept(new SwapLegVisitor<String>() {

      @Override
      public String visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
        return FIXED_INTEREST;
      }

      @Override
      public String visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
        return FLOATING_INTEREST;
      }
    });
  }

}
