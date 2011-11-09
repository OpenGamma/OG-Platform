/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
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
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
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
 * 
 */
public class MarketDataNormalizationUtils {
  @SuppressWarnings("synthetic-access")
  private static final FinancialSecurityVisitor<Double> VISITOR = new NormalizationSecurityVisitor();
 
  
  public static double normalizeRateForFixedIncomeStrip(final StripInstrumentType strip, final double rate) {
    switch (strip) {
      case BASIS_SWAP:
      case TENOR_SWAP:
        return rate / 10000.;
      default:
        return rate / 100.;
    }
  }
  
  public static double normalizeRateForSecurity(final FinancialSecurity security, final double rate) {
    return rate * security.accept(VISITOR);
  }
  
  private static class NormalizationSecurityVisitor implements FinancialSecurityVisitor<Double> {

    @Override
    public Double visitBondSecurity(final BondSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize BondSecurity data");
    }

    @Override
    public Double visitCashSecurity(final CashSecurity security) {
      return 0.01;
    }

    @Override
    public Double visitEquitySecurity(final EquitySecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize EquitySecurity data");
    }

    @Override
    public Double visitFRASecurity(final FRASecurity security) {
      return 0.01;
    }

    @Override
    public Double visitFutureSecurity(final FutureSecurity security) {
      if (security instanceof InterestRateFutureSecurity) {
        return 0.01;
      }
      throw new UnsupportedOperationException("Do not need to normalize non-IRFutureSecurity data");
    }

    @Override
    public Double visitSwapSecurity(final SwapSecurity security) {
      InterestRateInstrumentType type = SwapSecurityUtils.getSwapType(security);
      if (type == InterestRateInstrumentType.SWAP_IBOR_IBOR) {
        return 0.0001;
      }
      return 0.01;
    }

    @Override
    public Double visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize EquityIndexOptionSecurity data");
    }

    @Override
    public Double visitEquityOptionSecurity(final EquityOptionSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize EquityOptionSecurity data");
    }

    @Override
    public Double visitFXOptionSecurity(final FXOptionSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize FXOptionSecurity data");
    }

    @Override
    public Double visitSwaptionSecurity(final SwaptionSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize SwaptionSecurity data");
    }

    @Override
    public Double visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize IRFutureOptionSecurity data");
    }

    @Override
    public Double visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize FXBarrierOptionSecurity data");
    }

    @Override
    public Double visitFXSecurity(final FXSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize FXSecurity data");
    }

    @Override
    public Double visitFXForwardSecurity(final FXForwardSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize FXForwardSecurity data");
    }

    @Override
    public Double visitCapFloorSecurity(final CapFloorSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize CapFloorSecurity data");
    }

    @Override
    public Double visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize CapFloorCMSSpreadSecurity data");
    }

    @Override
    public Double visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
      throw new UnsupportedOperationException("Do not need to normalize BondSecurity data");
    }
    
  }
}
