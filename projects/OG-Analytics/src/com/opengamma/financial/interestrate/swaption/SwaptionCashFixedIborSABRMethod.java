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
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
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
    // FIXME: A better notion of maturity is required
    double maturity = annuityFixed.getNthPayment(0).getPaymentYearFraction();
    if (annuityFixed.getNumberOfPayments() >= 2) {
      maturity += annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - annuityFixed.getNthPayment(0).getPaymentTime();
    }
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation: option required to pass the strike (in case the swap has non-constant coupon).
    BlackPriceFunction blackFunction = new BlackPriceFunction();
    double volatility = sabrData.getSABRParameter().getVolatility(new DoublesPair(swaption.getTimeToExpiry(), maturity), strike, forward);
    BlackFunctionData dataBlack = new BlackFunctionData(forward, pvbp, volatility);
    Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return price;
  }

}
