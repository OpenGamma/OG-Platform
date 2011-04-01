/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivityUtil;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical swaption with SABR model.
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
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    //    double pvbp = PVC.visit(RRV.visit(annuityFixed, 1.0), sabrData);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()));
    double strike = SwapFixedIborMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, sabrData);
    // TODO: A better notion of maturity may be required (using period?)
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation: option required to pass the strike (in case the swap has non-constant coupon).
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strike, forward);
    BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp, volatility);
    Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return price;
  }

  /**
   * Computes the present value rate sensitivity of a physical delivery European swaption in the SABR model.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value.
   */
  public Map<String, List<DoublesPair>> presentValueSensitivity(final SwaptionPhysicalFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    // Derivative of the forward with respect to the rates.
    Map<String, List<DoublesPair>> forwardDr = PRSC.visit(swaption.getUnderlyingSwap(), sabrData);
    //    double pvbp = PVC.visit(RRV.visit(annuityFixed, 1.0), sabrData);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()));
    // Derivative of the PVBP with respect to the rates.
    Map<String, List<DoublesPair>> pvbpDr = SwapFixedIborMethod.presentValueBasisPointSensitivity(swaption.getUnderlyingSwap(), sabrData);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    double strike = SwapFixedIborMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, sabrData);
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(swaption.getTimeToExpiry(), maturity, strike, forward);
    BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result = PresentValueSensitivityUtil.multiplySensitivity(pvbpDr, bsAdjoint[0]);
    result = PresentValueSensitivityUtil.addSensitivity(sabrData, result, PresentValueSensitivityUtil.multiplySensitivity(forwardDr, pvbp * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
    return result;
  }

}
