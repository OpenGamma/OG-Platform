/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

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
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swap.SwapFixedDiscountingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of cash-settled swaptions with SABR model.
 */
public final class SwaptionCashFixedIborSABRMethod implements PricingMethod {

  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final SwaptionCashFixedIborSABRMethod INSTANCE = new SwaptionCashFixedIborSABRMethod();

  public static SwaptionCashFixedIborSABRMethod getInstance() {
    return INSTANCE;
  }

  private SwaptionCashFixedIborSABRMethod() {
  }

  /**
   * Computes the present value of a cash-settled European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), sabrData);
    final double pvbp = SwapFixedDiscountingMethod.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    // TODO: A better notion of maturity may be required (using period?)
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
    final double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionCashFixedIbor, "Cash settlement swaption");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "Bundle should contain SABR data");
    return presentValue((SwaptionCashFixedIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value rate sensitivity of a cash delivery European swaption in the SABR model. The strike equivalent dependency on curve is ignored.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final SwaptionCashFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    final ParRateCalculator prc = ParRateCalculator.getInstance();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    // Derivative of the forward with respect to the rates.
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(PRSC.visit(swaption.getUnderlyingSwap(), sabrData));
    final double pvbp = SwapFixedDiscountingMethod.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Derivative of the cash annuity with respect to the forward.
    final double pvbpDf = SwapFixedDiscountingMethod.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
    final double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    final double sensiDF = -swaption.getSettlementTime() * discountFactorSettle * pvbp * bsAdjoint[0];
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(swaption.getSettlementTime(), sensiDF));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(annuityFixed.getNthPayment(0).getFundingCurveName(), list);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    result = result.add(forwardDr.multiply(discountFactorSettle * (pvbpDf * bsAdjoint[0] + pvbp * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1]))));
    if (!swaption.isLong()) {
      result = result.multiply(-1);
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
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final ParRateCalculator prc = ParRateCalculator.getInstance();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    final double pvbp = SwapFixedDiscountingMethod.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, swaption.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    final double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[3]);
    sensi.addRho(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[4]);
    sensi.addNu(expiryMaturity, omega * discountFactorSettle * pvbp * bsAdjoint[2] * volatilityAdjoint[5]);
    return sensi;
  }

}
