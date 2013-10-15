/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of cash-settled swaptions with SABR model.
 *  @deprecated Use {@link SwaptionCashFixedIborSABRMethod}
 */
@Deprecated
public final class SwaptionCashFixedIborSABRMethod implements PricingMethod {

  private static final SwaptionCashFixedIborSABRMethod INSTANCE = new SwaptionCashFixedIborSABRMethod();

  public static SwaptionCashFixedIborSABRMethod getInstance() {
    return INSTANCE;
  }

  private SwaptionCashFixedIborSABRMethod() {
  }

  /**
   * The calculators and methods.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  /**
   * Computes the present value of a cash-settled European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "SARB data");
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = swaption.getUnderlyingSwap().accept(PRC, sabrData);
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    // TODO: A better notion of maturity may be required (using period?)
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), swaption.getMaturityTime(), swaption.getStrike(), forward);
    final double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionCashFixedIbor, "Cash settlement swaption");
    ArgumentChecker.isTrue(curves instanceof SABRInterestRateDataBundle, "Bundle should contain SABR data");
    return presentValue((SwaptionCashFixedIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value rate sensitivity of a cash delivery European swaption in the SABR model. The strike equivalent dependency on curve is ignored.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final SwaptionCashFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "SARB data");
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = swaption.getUnderlyingSwap().accept(PRC, sabrData);
    // Derivative of the forward with respect to the rates.
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(swaption.getUnderlyingSwap().accept(PRSC, sabrData));
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Derivative of the cash annuity with respect to the forward.
    final double pvbpDf = METHOD_SWAP.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
    final double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    final double sensiDF = -swaption.getSettlementTime() * discountFactorSettle * pvbp * bsAdjoint[0];
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(swaption.getSettlementTime(), sensiDF));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    resultMap.put(annuityFixed.getNthPayment(0).getFundingCurveName(), list);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    result = result.plus(forwardDr.multipliedBy(discountFactorSettle * (pvbpDf * bsAdjoint[0] + pvbp * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1]))));
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
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionCashFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "SARB data");
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = swaption.getUnderlyingSwap().accept(PRC, sabrData);
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final DoublesPair expiryMaturity = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    final double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[3]);
    sensi.addBeta(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[4]);
    sensi.addRho(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[5]);
    sensi.addNu(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[6]);
    return sensi;
  }

}
