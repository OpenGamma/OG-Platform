/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model.
 */
public final class SwaptionPhysicalFixedIborSABRMethod {

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
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    // TODO: A better notion of maturity may be required (using period?)
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), pv);
  }

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR and multi-curves provider.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    return volatility;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData.getMulticurveProvider());
    final double maturity = swaption.getMaturityTime();
    // Derivative of the forward and pvbp with respect to the rates.
    final MulticurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final MulticurveSensitivity forwardModifiedDr = PRCSDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    MulticurveSensitivity result = pvbpModifiedDr.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return MultipleCurrencyMulticurveSensitivity.of(swaption.getCurrency(), result);
  }

  /**
   * Computes the present value SABR sensitivity of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData.getMulticurveProvider());
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

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model and its sensitivities with respect to the curve and SABR parameters.
   * The sensitivities are computed using algorithmic differentiation.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value, present value curves sensitivity and present value SABR sensitivity.
   */
  public Triple<MultipleCurrencyAmount, MultipleCurrencyMulticurveSensitivity, PresentValueSABRSensitivityDataBundle> presentValueAD(final SwaptionPhysicalFixedIbor swaption,
      final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData.getMulticurveProvider());
    final double maturity = swaption.getMaturityTime();
    // TODO: A better notion of maturity may be required (using period?)
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1.0, volatilityAdjoint[0]);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final MultipleCurrencyAmount pv = MultipleCurrencyAmount.of(swaption.getCurrency(), pvbpModified * func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0));
    // Curve sensitivity
    final MulticurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final MulticurveSensitivity forwardModifiedDr = PRCSDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, sabrData.getMulticurveProvider());
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    MulticurveSensitivity result = pvbpModifiedDr.multipliedBy(bsAdjoint[0]);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    final MultipleCurrencyMulticurveSensitivity pvcs = MultipleCurrencyMulticurveSensitivity.of(swaption.getCurrency(), result);
    // SABR sensitivity
    final PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    pvss.addAlpha(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[3]);
    pvss.addBeta(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[4]);
    pvss.addRho(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[5]);
    pvss.addNu(expiryMaturity, omega * pvbpModified * bsAdjoint[2] * volatilityAdjoint[6]);
    return Triple.of(pv, pvcs, pvss);
  }

}
