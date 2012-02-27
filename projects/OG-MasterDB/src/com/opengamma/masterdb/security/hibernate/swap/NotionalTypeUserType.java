/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the NotionalType enum
 */
public class NotionalTypeUserType extends EnumUserType<NotionalType> {

  private static final String COMMODITY = "Commodity";
  private static final String INTEREST_RATE = "Interest rate";
  private static final String SECURITY = "Security";
  private static final String VARIANCE = "Variance";

  public NotionalTypeUserType() {
    super(NotionalType.class, NotionalType.values());
  }

  @Override
  protected String enumToStringNoCache(NotionalType value) {
    return value.accept(new NotionalVisitor<String>() {

      @Override
      public String visitCommodityNotional(CommodityNotional notional) {
        return COMMODITY;
      }

      @Override
      public String visitInterestRateNotional(InterestRateNotional notional) {
        return INTEREST_RATE;
      }

      @Override
      public String visitSecurityNotional(SecurityNotional notional) {
        return SECURITY;
      }

      @Override
      public String visitVarianceSwapNotional(VarianceSwapNotional notional) {
        return VARIANCE;
      }

    });
  }

}
