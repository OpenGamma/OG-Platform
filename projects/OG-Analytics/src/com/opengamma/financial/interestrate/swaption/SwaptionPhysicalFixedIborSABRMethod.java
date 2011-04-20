/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model.
 */
public class SwaptionPhysicalFixedIborSABRMethod {

  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public double presentValue(final SwaptionPhysicalFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData);
    double pvbpModified = SwapFixedIborMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getSABRParameter().getDayCount(), sabrData);
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    double forwardModified = forward * pvbp / pvbpModified;
    double strikeModified = SwapFixedIborMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData);
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    // TODO: A better notion of maturity may be required (using period?)
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
    BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
    Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return price;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(final SwaptionPhysicalFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    // Derivative of the forward with respect to the rates.
    PresentValueSensitivity forwardDr = new PresentValueSensitivity(PRSC.visit(swaption.getUnderlyingSwap(), sabrData));
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()));
    // Derivative of the PVBP with respect to the rates.
    PresentValueSensitivity pvbpDr = SwapFixedIborMethod.presentValueBasisPointSensitivity(swaption.getUnderlyingSwap(), sabrData);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    double strike = SwapFixedIborMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, sabrData);
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strike, forward);
    BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    PresentValueSensitivity result = pvbpDr.multiply(bsAdjoint[0]);
    result = result.add(forwardDr.multiply(pvbp * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
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
  public PresentValueSABRSensitivity presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    PresentValueSABRSensitivity sensi = new PresentValueSABRSensitivity();
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()));
    double strike = SwapFixedIborMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, sabrData);
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strike, forward);
    BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * pvbp * bsAdjoint[2] * volatilityAdjoint[3]);
    sensi.addRho(expiryMaturity, omega * pvbp * bsAdjoint[2] * volatilityAdjoint[4]);
    sensi.addNu(expiryMaturity, omega * pvbp * bsAdjoint[2] * volatilityAdjoint[5]);
    return sensi;
  }
}
