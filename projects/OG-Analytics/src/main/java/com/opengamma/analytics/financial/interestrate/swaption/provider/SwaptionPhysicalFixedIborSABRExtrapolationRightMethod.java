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
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class used to compute the price and sensitivity of a physical delivery swaption with SABR model and extrapolation to the right.
 * Implemented only for the SABRHaganVolatilityFunction.
 * OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 */
public class SwaptionPhysicalFixedIborSABRExtrapolationRightMethod {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /**
   * The calculator and methods.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(final double cutOffStrike, final double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final Currency ccy = swaption.getCurrency();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    if (strikeModified <= _cutOffStrike) { // No extrapolation
      final BlackPriceFunction blackFunction = new BlackPriceFunction();
      final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
      final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
      final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
      return MultipleCurrencyAmount.of(ccy, func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0));
    }
    // With extrapolation
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forwardModified, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    return MultipleCurrencyAmount.of(ccy, pvbpModified * sabrExtrapolation.price(option) * (swaption.isLong() ? 1.0 : -1.0));
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final Currency ccy = swaption.getCurrency();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    // Derivative of the forward and pvbp with respect to the rates.
    final MulticurveSensitivity pvbpModifiedDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final MulticurveSensitivity forwardModifiedDr = PRCSDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());

    // With extrapolation
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forwardModified, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    MulticurveSensitivity result = pvbpModifiedDr.multipliedBy(sabrExtrapolation.price(option));
    final double priceDF = sabrExtrapolation.priceDerivativeForward(option);
    result = result.plus(forwardModifiedDr.multipliedBy(pvbpModified * priceDF));
    if (!swaption.isLong()) {
      result = result.multipliedBy(-1);
    }
    return MultipleCurrencyMulticurveSensitivity.of(ccy, result);
  }

  /**
   * Computes the present value SABR sensitivity of a physical delivery European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final DayCount dayCountModification = sabrData.getSABRGenerator().getFixedLegDayCount();
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final double pvbpModified = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double forwardModified = PRDC.visitFixedCouponSwap(swaption.getUnderlyingSwap(), dayCountModification, multicurves);
    final double strikeModified = METHOD_SWAP.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, multicurves);
    final double maturity = swaption.getMaturityTime();
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forwardModified, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    final double[] priceDSabr = new double[4];
    sabrExtrapolation.priceAdjointSABR(option, priceDSabr);
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * pvbpModified * priceDSabr[0]);
    sensi.addBeta(expiryMaturity, omega * pvbpModified * priceDSabr[1]);
    sensi.addRho(expiryMaturity, omega * pvbpModified * priceDSabr[2]);
    sensi.addNu(expiryMaturity, omega * pvbpModified * priceDSabr[3]);
    return sensi;
  }

}
