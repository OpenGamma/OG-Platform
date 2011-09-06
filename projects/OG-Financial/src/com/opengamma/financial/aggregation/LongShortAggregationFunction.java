/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Function to classify positions by Currency.
 *
 */
public class LongShortAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Long/Short";
  private static final String NOT_LONG_SHORT = "N/A"; 
  private static final String LONG = "Long";
  private static final String SHORT = "Short";
  
  @Override
  public String classifyPosition(final Position position) {
    
    FinancialSecurityVisitor<String> visitor = new FinancialSecurityVisitor<String>() {

      @Override
      public String visitBondSecurity(BondSecurity security) {
        return position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitCashSecurity(CashSecurity security) {
        return security.getAmount() * position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitEquitySecurity(EquitySecurity security) {
        return position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitFRASecurity(FRASecurity security) {
        return security.getAmount() * position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitFutureSecurity(FutureSecurity security) {
        return position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitSwapSecurity(SwapSecurity security) {
        return NOT_LONG_SHORT;
      }

      @Override
      public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
        return position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitEquityOptionSecurity(EquityOptionSecurity security) {
        return position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitFXOptionSecurity(FXOptionSecurity security) {
        return security.getIsLong() ? LONG : SHORT;
      }

      @Override
      public String visitSwaptionSecurity(SwaptionSecurity security) {
        return security.getIsLong() ? LONG : SHORT;
      }

      @Override
      public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
        return position.getQuantity().longValue() < 0 ? SHORT : LONG;
      }

      @Override
      public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
        return security.getIsLong() ? LONG : SHORT;
      }

      @Override
      public String visitFXSecurity(FXSecurity security) {
        return NOT_LONG_SHORT;
      }

      @Override
      public String visitFXForwardSecurity(FXForwardSecurity security) {
        return NOT_LONG_SHORT;
      }

      @Override
      public String visitCapFloorSecurity(CapFloorSecurity security) {
        return NOT_LONG_SHORT;
      }

      @Override
      public String visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
        return NOT_LONG_SHORT;
      }

      @Override
      public String visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
        return NOT_LONG_SHORT;
      }
      
    };
    if (position.getSecurity() instanceof FinancialSecurity) {
      FinancialSecurity finSec = (FinancialSecurity) position.getSecurity();
      return finSec.accept(visitor);
    }
    return NOT_LONG_SHORT;
  }

  public String getName() {
    return NAME;
  }
}
