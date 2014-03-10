/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.impl.MockConvention;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.ExchangeTradedFutureAndOptionConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.InterestRateSwapLegConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;

/**
 * Pseudo-visitor for conventions.
 * @param <P> the type of the parameter to pass
 * @param <R> the return type
 */
public abstract class ConventionFollower<P, R> {

  public R followConvention(Convention c, P parameter) {
    if (c instanceof CMSLegConvention) {
      CMSLegConvention cmsLegConvention = (CMSLegConvention) c;
      return followCMSLegConvention(cmsLegConvention, parameter);
    } else if (c instanceof CompoundingIborLegConvention) {
      CompoundingIborLegConvention compoundingIborLegConvention = (CompoundingIborLegConvention) c;
      return followCompoundingIborLegConvention(compoundingIborLegConvention, parameter);
    } else if (c instanceof DepositConvention) {
      DepositConvention depositConvention = (DepositConvention) c;
      return followDepositorConvention(depositConvention, parameter);
    } else if (c instanceof EquityConvention) {
      EquityConvention equityConvention = (EquityConvention) c;
      return followEquityConvention(equityConvention, parameter);
    } else if (c instanceof ExchangeTradedFutureAndOptionConvention) {
      if (c instanceof DeliverablePriceQuotedSwapFutureConvention) {
        DeliverablePriceQuotedSwapFutureConvention deliverablePriceQuotedSwapFutureConvention = (DeliverablePriceQuotedSwapFutureConvention) c;
        return followDeliverablePriceQuotedSwapFutureConvention(deliverablePriceQuotedSwapFutureConvention, parameter);
      } else if (c instanceof FederalFundsFutureConvention) {
        FederalFundsFutureConvention federalFundsFutureConvention = (FederalFundsFutureConvention) c;
        return followFederalFundsFutureConvention(federalFundsFutureConvention, parameter);
      } else if (c instanceof InterestRateFutureConvention) {
        InterestRateFutureConvention interestRateFutureConvention = (InterestRateFutureConvention) c;
        return followInterestRateFutureConvention(interestRateFutureConvention, parameter);
      } else {
        ExchangeTradedFutureAndOptionConvention exchangeTradedFutureAndOptionConvention = (ExchangeTradedFutureAndOptionConvention) c;
        return followOtherExchangeTradedFutureAndOptionConvention(exchangeTradedFutureAndOptionConvention, parameter);
        // other subclass of this, handled explicit known above
      }
    } else if (c instanceof FixedLegRollDateConvention) {
      FixedLegRollDateConvention fixedLegRollDateConvention = (FixedLegRollDateConvention) c;
      return followFixedLegRollRateConvention(fixedLegRollDateConvention, parameter);
    } else if (c instanceof FXForwardAndSwapConvention) {
      FXForwardAndSwapConvention fxForwardAndSwapConvention = (FXForwardAndSwapConvention) c;
      return followFXForwardAndSwapConvention(fxForwardAndSwapConvention, parameter);
    } else if (c instanceof FXSpotConvention) {
      FXSpotConvention fxSpotConvention = (FXSpotConvention) c;
      return followFXSpotConvention(fxSpotConvention, parameter);
    } else if (c instanceof IborIndexConvention) {
      IborIndexConvention iborIndexConvention = (IborIndexConvention) c;
      return followIborIndexConvention(iborIndexConvention, parameter);
    } else if (c instanceof InflationLegConvention) {
      InflationLegConvention inflationLegConvention = (InflationLegConvention) c;
      return followInflationLegConvention(inflationLegConvention, parameter);
    } else if (c instanceof InterestRateSwapLegConvention) {
      if (c instanceof FixedInterestRateSwapLegConvention) {
        FixedInterestRateSwapLegConvention fixedInterestRateSwapLegConvention = (FixedInterestRateSwapLegConvention) c;
        return followFixedInterestRateSwapLegConvention(fixedInterestRateSwapLegConvention, parameter);
      } else if (c instanceof FloatingInterestRateSwapLegConvention) {
        FloatingInterestRateSwapLegConvention floatingInterestRateSwapLegConvention = (FloatingInterestRateSwapLegConvention) c;
        return followFloatingInterestRateSwapLegConvention(floatingInterestRateSwapLegConvention, parameter);
      }
    } else if (c instanceof OISLegConvention) {
      OISLegConvention oisLegConvention = (OISLegConvention) c;
      return followOISLegConvention(oisLegConvention, parameter);
    } else if (c instanceof ONArithmeticAverageLegConvention) {
      ONArithmeticAverageLegConvention onArithmeticAverageLegConvention = (ONArithmeticAverageLegConvention) c;
      return followONArithmeticAverageLegConvention(onArithmeticAverageLegConvention, parameter);
    } else if (c instanceof ONCompoundedLegRollDateConvention) {
      ONCompoundedLegRollDateConvention onCompoundedLegRollDateConvention = (ONCompoundedLegRollDateConvention) c;
      return followONCompoundedLegRollRateConvention(onCompoundedLegRollDateConvention, parameter);
    } else if (c instanceof OvernightIndexConvention) {
      OvernightIndexConvention overnightIndexConvention = (OvernightIndexConvention) c;
      return followOvernightIndexConvention(overnightIndexConvention, parameter);
    } else if (c instanceof PriceIndexConvention) {
      PriceIndexConvention priceIndexConvention = (PriceIndexConvention) c;
      return followPriceIndexConvention(priceIndexConvention, parameter);
    } else if (c instanceof RollDateFRAConvention) {
      RollDateFRAConvention rollDateFRAConvention = (RollDateFRAConvention) c;
      return followRollDateFRAConvention(rollDateFRAConvention, parameter);
    } else if (c instanceof RollDateSwapConvention) {
      RollDateSwapConvention rollDateSwapConvention = (RollDateSwapConvention) c;
      return followRollDateSwapConvention(rollDateSwapConvention, parameter);
    } else if (c instanceof SwapConvention) {
      SwapConvention swapConvention = (SwapConvention) c;
      return followSwapConvention(swapConvention, parameter);
    } else if (c instanceof SwapFixedLegConvention) {
      SwapFixedLegConvention swapFixedLegConvention = (SwapFixedLegConvention) c;
      return followSwapFixedLegConvention(swapFixedLegConvention, parameter);
    } else if (c instanceof SwapIndexConvention) {
      SwapIndexConvention swapIndexConvention = (SwapIndexConvention) c;
      return followSwapIndexConvention(swapIndexConvention, parameter);
    } else if (c instanceof VanillaIborLegConvention) {
      VanillaIborLegConvention vanillaIborLegConvention = (VanillaIborLegConvention) c;
      return followVanillaIborLegConvention(vanillaIborLegConvention, parameter);
    } else if (c instanceof VanillaIborLegRollDateConvention) {
      VanillaIborLegRollDateConvention vanillaIborLegRollDateConvention = (VanillaIborLegRollDateConvention) c;
      return followVanillaIborLegRollDateConvention(vanillaIborLegRollDateConvention, parameter);
    }
    throw new OpenGammaRuntimeException("Unhandled convention:" + c);
  }

  public abstract R followVanillaIborLegRollDateConvention(VanillaIborLegRollDateConvention vanillaIborLegRollDateConvention, P parameter);
  

  public abstract R followVanillaIborLegConvention(VanillaIborLegConvention vanillaIborLegConvention, P parameter);
  

  public abstract R followSwapIndexConvention(SwapIndexConvention swapIndexConvention, P parameter);
  

  public abstract R followSwapFixedLegConvention(SwapFixedLegConvention swapFixedLegConvention, P parameter);
  

  public abstract R followSwapConvention(SwapConvention swapConvention, P parameter);
  

  public abstract R followRollDateSwapConvention(RollDateSwapConvention rollDateSwapConvention, P parameter);
  

  public abstract R followRollDateFRAConvention(RollDateFRAConvention rollDateFRAConvention, P parameter);
  

  public abstract R followPriceIndexConvention(PriceIndexConvention priceIndexConvention, P parameter);
  

  public abstract R followOvernightIndexConvention(OvernightIndexConvention overnightIndexConvention, P parameter);
  

  public abstract R followONCompoundedLegRollRateConvention(ONCompoundedLegRollDateConvention onCompoundedLegRollDateConvention, P parameter);
  

  public abstract R followONArithmeticAverageLegConvention(ONArithmeticAverageLegConvention onArithmeticAverageLegConvention, P parameter);
  

  public abstract R followOISLegConvention(OISLegConvention oisLegConvention, P parameter);
  

  public abstract R followMockConvention(MockConvention mockConvention, P parameter);
  

  public abstract R followFloatingInterestRateSwapLegConvention(FloatingInterestRateSwapLegConvention floatingInterestRateSwapLegConvention, P parameter);
  

  public abstract R followFixedInterestRateSwapLegConvention(FixedInterestRateSwapLegConvention fixedInterestRateSwapLegConvention, P parameter);
  

  public abstract R followInflationLegConvention(InflationLegConvention inflationLegConvention, P parameter);
  

  public abstract R followIborIndexConvention(IborIndexConvention iborIndexConvention, P parameter);
  

  public abstract R followFXSpotConvention(FXSpotConvention fxSpotConvention, P parameter);
  

  public abstract R followFXForwardAndSwapConvention(FXForwardAndSwapConvention fxForwardAndSwapConvention, P parameter);
  

  public abstract R followFixedLegRollRateConvention(FixedLegRollDateConvention fixedLegRollDateConvention, P parameter);
  

  public abstract R followOtherExchangeTradedFutureAndOptionConvention(ExchangeTradedFutureAndOptionConvention exchangeTradedFutureAndOptionConvention, P parameter);
  

  public abstract R followInterestRateFutureConvention(InterestRateFutureConvention interestRateFutureConvention, P parameter);
  

  public abstract R followFederalFundsFutureConvention(FederalFundsFutureConvention federalFundsFutureConvention, P parameter);
  

  public abstract R followDeliverablePriceQuotedSwapFutureConvention(DeliverablePriceQuotedSwapFutureConvention deliverablePriceQuotedSwapFutureConvention, P parameter);
  

  public abstract R followEquityConvention(EquityConvention equityConvention, P parameter);
  

  public abstract R followDepositorConvention(DepositConvention depositConvention, P parameter);
  

  public abstract R followCompoundingIborLegConvention(CompoundingIborLegConvention compoundingIborLegConvention, P parameter);
  

  public abstract R followCMSLegConvention(CMSLegConvention cmsLegConvention, P parameter);

}
