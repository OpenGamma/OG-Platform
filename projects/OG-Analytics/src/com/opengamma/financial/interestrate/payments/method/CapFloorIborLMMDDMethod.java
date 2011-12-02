/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 *  Class used to compute the price a Ibor cap/floor with LMM.  
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public class CapFloorIborLMMDDMethod implements PricingMethod {

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value of the cap/floor in the LMM. It is computed using a Black formula (on the shifted rate). The volatility is the LMM volatilities for the 
   * relevant period multiplied by the time dependent factor square mean.
   * The method is used mainly for calibration purposes.
   * @param cap The cap. Should have the same underlying index as the model (same payment frequency). 
   * @param lmmData The Model parameters.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CapFloorIbor cap, final LiborMarketModelDisplacedDiffusionDataBundle lmmData) {
    int index = lmmData.getLmmParameter().getTimeIndex(cap.getFixingPeriodStartTime());
    double volatility = 0;
    for (int loopfact = 0; loopfact < lmmData.getLmmParameter().getNbFactor(); loopfact++) {
      volatility += lmmData.getLmmParameter().getVolatility()[index][loopfact] * lmmData.getLmmParameter().getVolatility()[index][loopfact];
    }
    volatility = Math.sqrt(volatility);
    double timeDependentFactor = Math.sqrt((Math.exp(2 * lmmData.getLmmParameter().getMeanReversion() * cap.getFixingTime()) - 1.0) / (2.0 * lmmData.getLmmParameter().getMeanReversion()));
    volatility *= timeDependentFactor;
    double displacement = lmmData.getLmmParameter().getDisplacement()[index];
    double beta = lmmData.getCurve(cap.getForwardCurveName()).getDiscountFactor(cap.getFixingPeriodStartTime())
        / lmmData.getCurve(cap.getForwardCurveName()).getDiscountFactor(cap.getFixingPeriodEndTime()) * lmmData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getFixingPeriodEndTime())
        / lmmData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getFixingPeriodStartTime());
    double strikeAdjusted = (cap.getStrike() - (beta - 1) / cap.getFixingYearFraction()) / beta;
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeAdjusted + displacement, 1.0, cap.isCap()); // Time is in timeDependentFactor
    double forwardDsc = (lmmData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getFixingPeriodStartTime())
        / lmmData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getFixingPeriodEndTime()) - 1.0)
        / cap.getFixingYearFraction();
    final double df = lmmData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardDsc + displacement, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = beta * func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return CurrencyAmount.of(cap.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Ibor Cap/floor");
    Validate.isTrue(curves instanceof LiborMarketModelDisplacedDiffusionDataBundle, "Bundle should contain LMM data");
    return presentValue((CapFloorIbor) instrument, (LiborMarketModelDisplacedDiffusionDataBundle) curves);
  }

}
