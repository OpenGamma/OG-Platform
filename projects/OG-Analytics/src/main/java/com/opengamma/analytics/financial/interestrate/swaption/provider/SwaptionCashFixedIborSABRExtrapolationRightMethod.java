/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class used to compute the price and sensitivity of a cash-settled European swaption with SABR model and extrapolation to the right.
 * Implemented only for the SABRHaganVolatilityFunction.
 * OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 */
public class SwaptionCashFixedIborSABRExtrapolationRightMethod {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParRateCurveSensitivityDiscountingCalculator PRCSDC = ParRateCurveSensitivityDiscountingCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SwaptionCashFixedIborSABRExtrapolationRightMethod(final double cutOffStrike, final double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a cash-settled European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final Currency ccy = swaption.getCurrency();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, multicurves);
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final double discountFactorSettle = multicurves.getDiscountFactor(ccy, swaption.getSettlementTime());
    double pv;
    if (swaption.getStrike() <= _cutOffStrike) { // No extrapolation
      final BlackPriceFunction blackFunction = new BlackPriceFunction();
      final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
      final BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
      final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
      pv = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    } else { // With extrapolation
      final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
      final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
      final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
      final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
      final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
      final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
      final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
      pv = discountFactorSettle * pvbp * sabrExtrapolation.price(swaption) * (swaption.isLong() ? 1.0 : -1.0);
    }
    return MultipleCurrencyAmount.of(ccy, pv);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cash-settled European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final Currency ccy = swaption.getCurrency();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, multicurves);
    // Derivative of the forward with respect to the rates.
    final MulticurveSensitivity forwardDr = swaption.getUnderlyingSwap().accept(PRCSDC, multicurves);
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Derivative of the annuity with respect to the forward.
    final double pvbpDf = METHOD_SWAP.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    final double discountFactorSettle = multicurves.getDiscountFactor(ccy, swaption.getSettlementTime());
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double dfDr = -swaption.getSettlementTime() * discountFactorSettle;
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(swaption.getSettlementTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(multicurves.getName(ccy), list);
    MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    final double price = sabrExtrapolation.price(swaption);
    result = result.multipliedBy(pvbp * price);
    result = result.plus(forwardDr.multipliedBy(discountFactorSettle * (pvbpDf * price + pvbp * sabrExtrapolation.priceDerivativeForward(swaption))));
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
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(sabrData, "SABR swaption provider");
    final MulticurveProviderInterface multicurves = sabrData.getMulticurveProvider();
    final Currency ccy = swaption.getCurrency();
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = swaption.getUnderlyingSwap().accept(PRDC, multicurves);
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final double discountFactorSettle = multicurves.getDiscountFactor(ccy, swaption.getSettlementTime());
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    final double[] priceDSabr = new double[4];
    sabrExtrapolation.priceAdjointSABR(swaption, priceDSabr);
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * discountFactorSettle * pvbp * priceDSabr[0]);
    sensi.addBeta(expiryMaturity, omega * discountFactorSettle * pvbp * priceDSabr[1]);
    sensi.addRho(expiryMaturity, omega * discountFactorSettle * pvbp * priceDSabr[2]);
    sensi.addNu(expiryMaturity, omega * discountFactorSettle * pvbp * priceDSabr[3]);
    return sensi;
  }

}
