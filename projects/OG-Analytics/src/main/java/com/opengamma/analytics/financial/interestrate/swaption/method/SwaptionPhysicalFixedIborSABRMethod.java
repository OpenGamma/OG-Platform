/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model.
 *  @deprecated Use {@link SwaptionPhysicalFixedIborSABRMethod}
 */
@Deprecated
public final class SwaptionPhysicalFixedIborSABRMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborSABRMethod INSTANCE = new SwaptionPhysicalFixedIborSABRMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborSABRMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborSABRMethod() {
  }

  /**
   * The calculator and methods.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "sabr data");
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getSABRParameter().getDayCount(), sabrData);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), sabrData.getSABRParameter().getDayCount(), sabrData);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData);
    final double maturity = swaption.getMaturityTime();
    // TODO: A better notion of maturity may be required (using period?)
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof SABRInterestRateDataBundle, "Bundle should contain SABR data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "sabr data");
    final DayCount dayCountModification = sabrData.getSABRParameter().getDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, sabrData);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData);
    final double maturity = swaption.getMaturityTime();
    // Derivative of the forward and pvbp with respect to the rates.
    final InterestRateCurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification, sabrData);
    final InterestRateCurveSensitivity forwardModifiedDr = new InterestRateCurveSensitivity(PRSC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData));
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    InterestRateCurveSensitivity result = pvbpModifiedDr.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return result;
  }

  /**
   * Computes the present value SABR sensitivity of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "sabr data");
    final DayCount dayCountModification = sabrData.getSABRParameter().getDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, sabrData);
    final double forwardModified = PRC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData);
    final double maturity = swaption.getMaturityTime();
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[3]);
    sensi.addBeta(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[4]);
    sensi.addRho(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[5]);
    sensi.addNu(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[6]);
    return sensi;
  }

}
