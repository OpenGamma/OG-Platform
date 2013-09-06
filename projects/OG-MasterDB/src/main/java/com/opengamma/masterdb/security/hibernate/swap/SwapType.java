/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;

/**
 * 
 */
public enum SwapType {
  /**
   * A vanilla swap type
   */
  SWAP,
  /**
   * Forward starting swap type
   */
  FORWARD,
  /**
   * Zero coupon inflation swap type
   */
  ZERO_COUPON_INFLATION,
  /**
   * Year on Year inflation swap type
   */
  YEAR_ON_YEAR_INFLATION;

  public static SwapType identify(final SwapSecurity object) {
    return object.accept(new FinancialSecurityVisitorAdapter<SwapType>() {

      @Override
      public SwapType visitForwardSwapSecurity(ForwardSwapSecurity security) {
        return FORWARD;
      }

      @Override
      public SwapType visitSwapSecurity(SwapSecurity security) {
        return SWAP;
      }

      @Override
      public SwapType visitZeroCouponInflationSwapSecurity(ZeroCouponInflationSwapSecurity security) {
        return ZERO_COUPON_INFLATION;
      }
      
      @Override
      public SwapType visitYearOnYearInflationSwapSecurity(YearOnYearInflationSwapSecurity security) {
        return YEAR_ON_YEAR_INFLATION;
      }
    });
  }

  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    switch (this) {
      case SWAP:
        return visitor.visitSwapSecurity(null);
      case FORWARD:
        return visitor.visitForwardSwapSecurity(null);
      case ZERO_COUPON_INFLATION:
        return visitor.visitZeroCouponInflationSwapSecurity(null);
      case YEAR_ON_YEAR_INFLATION:
        return visitor.visitYearOnYearInflationSwapSecurity(null);
      default:
        throw new OpenGammaRuntimeException("unexpected SwapType: " + this);
    }
  }

}
