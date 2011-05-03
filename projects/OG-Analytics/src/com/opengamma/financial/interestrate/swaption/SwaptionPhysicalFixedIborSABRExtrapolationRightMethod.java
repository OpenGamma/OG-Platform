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
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a physical delivery swaption with SABR model and extrapolation to the right. 
 *  Implemented only for the SABRHaganVolatilityFunction.
 *  Implemented only for payers.
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
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(double cutOffStrike, double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model with extrapolation to the right.
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
    EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    double price;
    if (strikeModified <= _cutOffStrike) { // No extrapolation
      BlackPriceFunction blackFunction = new BlackPriceFunction();
      double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
      BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
      Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
      price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    } else { // With extrapolation
      DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
      double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
      double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
      double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
      double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
      SABRFormulaData sabrParam = new SABRFormulaData(forwardModified, alpha, beta, nu, rho);
      SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
      price = pvbpModified * sabrExtrapolation.price(option) * (swaption.isLong() ? 1.0 : -1.0);
    }
    return price;
  }

}
