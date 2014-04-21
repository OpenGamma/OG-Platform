/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Class used to compute the price a Ibor cap/floor with LMM.  
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public final class CapFloorIborLMMDDMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorIborLMMDDMethod INSTANCE = new CapFloorIborLMMDDMethod();

  /**
   * Private constructor.
   */
  private CapFloorIborLMMDDMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorIborLMMDDMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the present value of the cap/floor in the LMM. It is computed using a Black formula (on the shifted rate). The volatility is the LMM volatilities for the 
   * relevant period multiplied by the time dependent factor square mean.
   * The method is used mainly for calibration purposes.
   * @param cap The cap. Should have the same underlying index as the model (same payment frequency). 
   * @param lmmData The LMM and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final LiborMarketModelDisplacedDiffusionProviderInterface lmmData) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(lmmData, "LMM provider");
    Currency ccy = cap.getCurrency();
    MulticurveProviderInterface multicurves = lmmData.getMulticurveProvider();
    LiborMarketModelDisplacedDiffusionParameters parameters = lmmData.getLMMParameters();
    int index = lmmData.getLMMParameters().getTimeIndex(cap.getFixingPeriodStartTime());
    double volatility = 0;
    for (int loopfact = 0; loopfact < lmmData.getLMMParameters().getNbFactor(); loopfact++) {
      volatility += parameters.getVolatility()[index][loopfact] * parameters.getVolatility()[index][loopfact];
    }
    volatility = Math.sqrt(volatility);
    double timeDependentFactor = Math.sqrt((Math.exp(2 * parameters.getMeanReversion() * cap.getFixingTime()) - 1.0) / (2.0 * parameters.getMeanReversion()));
    volatility *= timeDependentFactor;
    double displacement = parameters.getDisplacement()[index];
    double forward = multicurves.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    double beta = (1.0 + cap.getFixingAccrualFactor() * forward) * multicurves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime())
        / multicurves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime());
    double strikeAdjusted = (cap.getStrike() - (beta - 1) / cap.getFixingAccrualFactor()) / beta;
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeAdjusted + displacement, 1.0, cap.isCap()); // Time is in timeDependentFactor
    double forwardDsc = (multicurves.getDiscountFactor(ccy, cap.getFixingPeriodStartTime()) / multicurves.getDiscountFactor(ccy, cap.getFixingPeriodEndTime()) - 1.0) / cap.getFixingAccrualFactor();
    final double df = multicurves.getDiscountFactor(ccy, cap.getPaymentTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardDsc + displacement, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = beta * func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }

}
