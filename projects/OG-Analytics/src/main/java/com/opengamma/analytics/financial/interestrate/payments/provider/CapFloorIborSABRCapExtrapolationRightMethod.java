/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.method.CapFloorIborSABRCapMethodInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor with SABR model and extrapolation for high strikes.
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public class CapFloorIborSABRCapExtrapolationRightMethod implements CapFloorIborSABRCapMethodInterface {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;
  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public CapFloorIborSABRCapExtrapolationRightMethod(final double cutOffStrike, final double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a cash-settled European swaption in the SABR model with extrapolation to the right.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider. The SABR function need to be the Hagan function.
   * @return The present value.
   */
  @Override
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = sabr.getMulticurveProvider().getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = sabr.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    double price;
    if (cap.getStrike() <= _cutOffStrike) { // No extrapolation
      final double volatility = sabr.getSABRParameter().getVolatility(cap.getFixingTime(), maturity, cap.getStrike(), forward);
      final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
      final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
      price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    } else { // With extrapolation
      SABRExtrapolationRightFunction sabrExtrapolation;
      final DoublesPair expiryMaturity = DoublesPair.of(cap.getFixingTime(), maturity);
      final double alpha = sabr.getSABRParameter().getAlpha(expiryMaturity);
      final double beta = sabr.getSABRParameter().getBeta(expiryMaturity);
      final double rho = sabr.getSABRParameter().getRho(expiryMaturity);
      final double nu = sabr.getSABRParameter().getNu(expiryMaturity);
      final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, cap.getFixingTime(), _mu);
      price = df * sabrExtrapolation.price(option) * cap.getNotional() * cap.getPaymentYearFraction();
    }
    return MultipleCurrencyAmount.of(cap.getCurrency(), price);
  }

  /**
   * Computes the present value sensitivity to the yield curves of a Ibor cap/floor in the SABR framework with extrapolation on the right.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final MulticurveProviderInterface multicurve = sabr.getMulticurveProvider();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = multicurve.getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = multicurve.getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final MulticurveSensitivity forwardDr = MulticurveSensitivity.ofForward(sabr.getMulticurveProvider().getName(cap.getIndex()),
        new SimplyCompoundedForwardSensitivity(cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), 1.0));
    final double dfDr = -cap.getPaymentTime() * df;
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(multicurve.getName(cap.getCurrency()), list);
    MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    double bsPrice;
    double bsDforward;
    if (cap.getStrike() <= _cutOffStrike) { // No extrapolation
      final double[] volatilityAdjoint = sabr.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
      final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
      final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      bsPrice = bsAdjoint[0];
      bsDforward = df * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1]);
    } else { // With extrapolation
      final DoublesPair expiryMaturity = DoublesPair.of(cap.getFixingTime(), maturity);
      final double alpha = sabr.getSABRParameter().getAlpha(expiryMaturity);
      final double beta = sabr.getSABRParameter().getBeta(expiryMaturity);
      final double rho = sabr.getSABRParameter().getRho(expiryMaturity);
      final double nu = sabr.getSABRParameter().getNu(expiryMaturity);
      final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, cap.getFixingTime(), _mu);
      bsPrice = sabrExtrapolation.price(option);
      bsDforward = sabrExtrapolation.priceDerivativeForward(option);
    }
    result = result.multipliedBy(bsPrice);
    result = result.plus(forwardDr.multipliedBy(bsDforward));
    result = result.multipliedBy(cap.getNotional() * cap.getPaymentYearFraction());
    return MultipleCurrencyMulticurveSensitivity.of(cap.getCurrency(), result);
  }

  /**
   * Computes the present value SABR sensitivity of a cap/floor in the SABR framework with extrapolation on the right.
   * @param cap The cap/floor.
   * @param sabr The SABR cap and multi-curves provider. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(sabr, "SABR cap provider");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = sabr.getMulticurveProvider().getSimplyCompoundForwardRate(cap.getIndex(), cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor());
    final double df = sabr.getMulticurveProvider().getDiscountFactor(cap.getCurrency(), cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    final double[] bsDsabr = new double[4];
    if (cap.getStrike() <= _cutOffStrike) { // No extrapolation
      final double[] volatilityAdjoint = sabr.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
      final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
      final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      bsDsabr[0] = bsAdjoint[2] * volatilityAdjoint[3];
      bsDsabr[1] = bsAdjoint[2] * volatilityAdjoint[4];
      bsDsabr[2] = bsAdjoint[2] * volatilityAdjoint[5];
      bsDsabr[3] = bsAdjoint[2] * volatilityAdjoint[6];
    } else { // With extrapolation
      final DoublesPair expiryMaturity = DoublesPair.of(cap.getFixingTime(), maturity);
      final double alpha = sabr.getSABRParameter().getAlpha(expiryMaturity);
      final double beta = sabr.getSABRParameter().getBeta(expiryMaturity);
      final double rho = sabr.getSABRParameter().getRho(expiryMaturity);
      final double nu = sabr.getSABRParameter().getNu(expiryMaturity);
      final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, cap.getFixingTime(), _mu);
      sabrExtrapolation.priceAdjointSABR(option, bsDsabr);
    }
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = DoublesPair.of(cap.getFixingTime(), maturity);
    sensi.addAlpha(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[0]);
    sensi.addBeta(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[1]);
    sensi.addRho(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[2]);
    sensi.addNu(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[3]);
    return sensi;
  }

}
