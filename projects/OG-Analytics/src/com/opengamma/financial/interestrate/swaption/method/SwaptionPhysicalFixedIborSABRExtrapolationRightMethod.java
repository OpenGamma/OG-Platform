/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.SwapFixedDiscountingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class used to compute the price and sensitivity of a physical delivery swaption with SABR model and extrapolation to the right. 
 * Implemented only for the SABRHaganVolatilityFunction.
 * OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
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
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  /**
   * Constructor from cut-off strike and tail parameter.
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(final double cutOffStrike, final double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Computes the present value of a physical delivery European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data.
   * @return The present value.
   */
  public double presentValue(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData);
    final double pvbpModified = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getSABRParameter().getDayCount(), sabrData);
    final double forward = PRC.visit(swaption.getUnderlyingSwap(), sabrData);
    final double forwardModified = forward * pvbp / pvbpModified;
    final double strikeModified = SwapFixedDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbpModified, sabrData);
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    if (strikeModified <= _cutOffStrike) { // No extrapolation
      final BlackPriceFunction blackFunction = new BlackPriceFunction();
      final double volatility = sabrData.getSABRParameter().getVolatility(swaption.getTimeToExpiry(), maturity, strikeModified, forwardModified);
      final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, pvbpModified, volatility);
      final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    }
    // With extrapolation
    final DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forwardModified, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    return pvbpModified * sabrExtrapolation.price(option) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    final ParRateCalculator prc = ParRateCalculator.getInstance();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    // Derivative of the forward with respect to the rates.
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(PRSC.visit(swaption.getUnderlyingSwap(), sabrData));
    final double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()));
    // Derivative of the PVBP with respect to the rates.
    final InterestRateCurveSensitivity pvbpDr = SwapFixedDiscountingMethod.presentValueBasisPointCurveSensitivity(swaption.getUnderlyingSwap(), sabrData);
    // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    final double strike = SwapFixedDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, sabrData);
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    final DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    result = pvbpDr.multiply(sabrExtrapolation.price(option));
    final double priceDF = sabrExtrapolation.priceDerivativeForward(option);
    result = result.add(forwardDr.multiply(pvbp * priceDF));
    if (!swaption.isLong()) {
      result = result.multiply(-1);
    }
    return result;
  }

  /**
   * Computes the present value SABR sensitivity of a physical delivery European swaption in the SABR model with extrapolation to the right.
   * @param swaption The swaption.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final SwaptionPhysicalFixedIbor swaption, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(swaption);
    Validate.notNull(sabrData);
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final ParRateCalculator prc = ParRateCalculator.getInstance();
    final AnnuityCouponFixed annuityFixed = swaption.getUnderlyingSwap().getFixedLeg();
    final double forward = prc.visit(swaption.getUnderlyingSwap(), sabrData);
    final double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swaption.getUnderlyingSwap(), sabrData.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()));
    final double strike = SwapFixedDiscountingMethod.couponEquivalent(swaption.getUnderlyingSwap(), pvbp, sabrData);
    final double maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - swaption.getSettlementTime();
    final DoublesPair expiryMaturity = new DoublesPair(swaption.getTimeToExpiry(), maturity);
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, swaption.getTimeToExpiry(), swaption.isCall());
    final double alpha = sabrData.getSABRParameter().getAlpha(expiryMaturity);
    final double beta = sabrData.getSABRParameter().getBeta(expiryMaturity);
    final double rho = sabrData.getSABRParameter().getRho(expiryMaturity);
    final double nu = sabrData.getSABRParameter().getNu(expiryMaturity);
    final SABRFormulaData sabrParam = new SABRFormulaData(alpha, beta, rho, nu);
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrParam, _cutOffStrike, swaption.getTimeToExpiry(), _mu);
    final double[] priceDSabr = new double[3];
    sabrExtrapolation.priceAdjointSABR(option, priceDSabr);
    final double omega = (swaption.isLong() ? 1.0 : -1.0);
    sensi.addAlpha(expiryMaturity, omega * pvbp * priceDSabr[0]);
    sensi.addRho(expiryMaturity, omega * pvbp * priceDSabr[1]);
    sensi.addNu(expiryMaturity, omega * pvbp * priceDSabr[2]);
    return sensi;
  }

}
