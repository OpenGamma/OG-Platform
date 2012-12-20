/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;

/**
 * 
 */
public enum NotionalType {
  /**
   * 
   */
  COMMODITY,
  /**
   * 
   */
  INTEREST_RATE,
  /**
   * 
   */
  SECURITY,
  /**
   *
   */
  VARIANCE;

  public static NotionalType identify(final Notional object) {
    return object.accept(new NotionalVisitor<NotionalType>() {

      @Override
      public NotionalType visitCommodityNotional(CommodityNotional notional) {
        return COMMODITY;
      }

      @Override
      public NotionalType visitInterestRateNotional(InterestRateNotional notional) {
        return INTEREST_RATE;
      }

      @Override
      public NotionalType visitSecurityNotional(SecurityNotional notional) {
        return SECURITY;
      }

      @Override
      public NotionalType visitVarianceSwapNotional(VarianceSwapNotional notional) {
        return VARIANCE;
      }

    });
  }

  public <T> T accept(final NotionalVisitor<T> visitor) {
    switch (this) {
      case COMMODITY:
        return visitor.visitCommodityNotional(null);
      case INTEREST_RATE:
        return visitor.visitInterestRateNotional(null);
      case SECURITY:
        return visitor.visitSecurityNotional(null);
      case VARIANCE:
        return visitor.visitVarianceSwapNotional(null);
      default:
        throw new OpenGammaRuntimeException("unexpected SwapLegType: " + this);
    }
  }

}
