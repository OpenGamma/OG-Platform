/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a cash swaption with SABR model.
 */
public class SwaptionCashFixedIborSABRMethod {

  public double price(final SwaptionCashFixedIbor swaption, SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    ParRateCalculator prc = ParRateCalculator.getInstance();
    AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    double pvbp = SwapFixedIborMethod.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    double strike = annuityFixed.getNthPayment(0).getFixedRate();
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not required.
    // TODO: A better notion of maturity may be required (using period?)
    double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double volatility = sabrData.getSABRParameter().getVolatility(new DoublesPair(swaption.getTimeToExpiry(), maturity), strike, forward);
    double discountFactorSettle = sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
    Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
    double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return price;
  }

}
