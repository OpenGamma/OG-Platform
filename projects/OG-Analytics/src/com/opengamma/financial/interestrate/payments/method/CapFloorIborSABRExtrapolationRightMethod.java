/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor with SABR model and extrapolation for high strikes. 
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public class CapFloorIborSABRExtrapolationRightMethod implements PricingMethod {

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
   * The Par Rate Calculator used in the pricing.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();

  /**
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public CapFloorIborSABRExtrapolationRightMethod(final double cutOffStrike, final double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a cash-settled European swaption in the SABR model with extrapolation to the right.
   * @param cap The cap/floor.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    double forward = PRC.visit(cap, sabrData);
    double df = sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    double price;
    if (cap.getStrike() <= _cutOffStrike) { // No extrapolation
      double volatility = sabrData.getSABRParameter().getVolatility(cap.getFixingTime(), maturity, cap.getStrike(), forward);
      BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
      Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
      price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    } else { // With extrapolation
      SABRExtrapolationRightFunction sabrExtrapolation;
      DoublesPair expiryMaturity = new DoublesPair(cap.getFixingTime(), maturity);
      double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
      double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
      double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
      double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
      SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, cap.getFixingTime(), _mu);
      price = df * sabrExtrapolation.price(option) * cap.getNotional() * cap.getPaymentYearFraction();
    }
    return CurrencyAmount.of(cap.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Cap/Floor on Ibor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "SABR interest rate data bundle required");
    return presentValue((CapFloorIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value sensitivity to the yield curves of a Ibor cap/floor in the SABR framework with extrapolation on the right.
   * @param cap The cap/floor.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    double forward = PRC.visit(cap, sabrData);
    InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(PRSC.visit(cap, sabrData));
    double df = sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    double dfDr = -cap.getPaymentTime() * df;
    double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    InterestRateCurveSensitivity result;
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(cap.getFundingCurveName(), list);
    result = new InterestRateCurveSensitivity(resultMap); // result contains \partial df / \partial r
    double bsPrice;
    double bsDforward;
    if (cap.getStrike() <= _cutOffStrike) { // No extrapolation
      double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
      BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
      double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      bsPrice = bsAdjoint[0];
      bsDforward = df * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1]);
    } else { // With extrapolation
      DoublesPair expiryMaturity = new DoublesPair(cap.getFixingTime(), maturity);
      double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
      double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
      double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
      double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
      SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, cap.getFixingTime(), _mu);
      bsPrice = sabrExtrapolation.price(option);
      bsDforward = sabrExtrapolation.priceDerivativeForward(option);
    }
    result = result.multiply(bsPrice);
    result = result.add(forwardDr.multiply(bsDforward));
    result = result.multiply(cap.getNotional() * cap.getPaymentYearFraction());
    return result;
  }

  /**
   * Computes the present value SABR sensitivity of a cap/floor in the SABR framework with extrapolation on the right. 
   * @param cap The cap/floor.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = PRC.visit(cap, sabrData);
    final double df = sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    double[] bsDsabr = new double[3];
    if (cap.getStrike() <= _cutOffStrike) { // No extrapolation
      double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
      BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
      double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      bsDsabr[0] = bsAdjoint[2] * volatilityAdjoint[3];
      bsDsabr[1] = bsAdjoint[2] * volatilityAdjoint[4];
      bsDsabr[2] = bsAdjoint[2] * volatilityAdjoint[5];
    } else { // With extrapolation
      DoublesPair expiryMaturity = new DoublesPair(cap.getFixingTime(), maturity);
      double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
      double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
      double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
      double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
      SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, cap.getFixingTime(), _mu);
      sabrExtrapolation.priceAdjointSABR(option, bsDsabr);
    }
    PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = new DoublesPair(cap.getFixingTime(), maturity);
    sensi.addAlpha(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[0]);
    sensi.addRho(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[1]);
    sensi.addNu(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsDsabr[2]);
    return sensi;
  }

  //TODO: presentValue SABR sensi

}
