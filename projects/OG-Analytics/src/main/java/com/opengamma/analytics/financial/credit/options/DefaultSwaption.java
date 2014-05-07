/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnnuityForSpreadISDAFunction;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.SuperFastCreditCurveBuilder;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Pricer for option to enter a forward starting CDS (aka default swaption)
 */
public class DefaultSwaption {

  private final AnalyticCDSPricer _pricer = new AnalyticCDSPricer();

  /**
   * Price single-name CDS option
   * @param forwardStartingCDS The underlying forward starting CDS
   * @param yieldCurve The yield curve
   * @param creditCurve The credit curve
   * @param strike The fractional strike
   * @param optionExpiry The option expiry
   * @param vol The spread volatility 
   * @param isPayer True if payer swaption 
   * @param hasFrontEndProt True if no-knockout swaption
   * @return The option price 
   */
  public double price(final CDSAnalytic forwardStartingCDS, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double strike, final double optionExpiry,
      final double vol, final boolean isPayer, final boolean hasFrontEndProt) {

    ArgumentChecker.isTrue(forwardStartingCDS.getEffectiveProtectionStart() >= optionExpiry,
        "Have not provided a forward CDS. The option expiry is {}, but the CDS(effective) protection start time is {}", optionExpiry, forwardStartingCDS.getEffectiveProtectionStart());

    //front end protection is worth zero for an option to be the seller of protection 
    final double fep = isPayer && hasFrontEndProt ? forwardStartingCDS.getLGD() * yieldCurve.getDiscountFactor(optionExpiry) * (1 - creditCurve.getSurvivalProbability(optionExpiry)) : 0.0;
    final double annuity = _pricer.annuity(forwardStartingCDS, yieldCurve, creditCurve, PriceType.CLEAN, 0);
    final double protLeg = _pricer.protectionLeg(forwardStartingCDS, yieldCurve, creditCurve, 0);
    final double fwdSpread = protLeg / annuity;
    final double koVal = annuity * BlackFormulaRepository.price(fwdSpread, strike, optionExpiry, vol, isPayer);
    return koVal + fep;
  }

  public double priceFlat(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double strike, final double optionExpiry,
      final double vol, final boolean isPayer, final boolean hasFrontEndProt, final double coupon) {

    //note cds is the underlying CDS seen at the option expiry 
    ArgumentChecker.isTrue(cds.getCashSettleTime() >= optionExpiry, "Have provided a forward CDS. The option expiry is {}, but the CDS cash-settlement time is {}", optionExpiry,
        cds.getCashSettleTime());

    final ISDACompliantYieldCurve fwdYC = yieldCurve.withOffset(optionExpiry);
    final ISDACompliantCreditCurve fwdCC = new ISDACompliantCreditCurve(creditCurve.withOffset(optionExpiry));
    //this is the expected price at option expiry (+ 3 working days) 
    final double expPrice = _pricer.pv(cds, fwdYC, fwdCC, coupon);
    final SuperFastCreditCurveBuilder ccBuidler = new SuperFastCreditCurveBuilder();
    final ISDACompliantCreditCurve flatCC = ccBuidler.calibrateCreditCurve(cds, coupon, fwdYC, expPrice);

    //these values are condition on no default by option expiry 
    final double annuity = _pricer.annuity(cds, fwdYC, flatCC, PriceType.CLEAN);
    final double protLeg = _pricer.protectionLeg(cds, fwdYC, flatCC);
    final double fwdSpread = protLeg / annuity;

    //discount using 'real' credit curve 
    final double disAnnuity = yieldCurve.getDiscountFactor(cds.getCashSettleTime()) * creditCurve.getSurvivalProbability(optionExpiry) * annuity;
    final double koVal = disAnnuity * BlackFormulaRepository.price(fwdSpread, strike, optionExpiry, vol, isPayer);
    return koVal;
  }

  public double priceMod(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double strike, final double optionExpiry, final double vol,
      final boolean isPayer, final boolean hasFrontEndProt, final double coupon) {

    final AnnuityForSpreadFunction annuityFunction = new AnnuityForSpreadISDAFunction(cds, yieldCurve.withOffset(optionExpiry));

    //front end protection is worth zero for an option to be the seller of protection 
    final double fep = isPayer && hasFrontEndProt ? cds.getLGD() * yieldCurve.getDiscountFactor(optionExpiry) * (1 - creditCurve.getSurvivalProbability(optionExpiry)) : 0.0;
    final double rpv01 = _pricer.annuity(cds, yieldCurve, creditCurve, PriceType.CLEAN, 0);
    final double protLeg = _pricer.protectionLeg(cds, yieldCurve, creditCurve, 0);
    final double fwdSpread = (protLeg + fep) / rpv01;
    final double modK = coupon + (strike - coupon) * annuityFunction.evaluate(strike) / rpv01;
    final double koVal = rpv01 * BlackFormulaRepository.price(fwdSpread, modK, optionExpiry, vol, isPayer);
    return koVal;
  }

  /**
   * Compute volatility implied by {@link #price}
   * @param forwardStartingCDS The underlying forward starting CDS
   * @param yieldCurve The yield curve
   * @param creditCurve The credit curve
   * @param strike The fractional strike
   * @param optionExpiry The option expiry
   * @param price The option price
   * @param isPayer True of payer swaption
   * @param hasFrontEndProt True if no-knockout swaption 
   * @return The implied volatility
   */
  public double impliedVol(final CDSAnalytic forwardStartingCDS, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double strike, final double optionExpiry,
      final double price, final boolean isPayer, final boolean hasFrontEndProt) {
    final double fep = isPayer && hasFrontEndProt ? forwardStartingCDS.getLGD() * yieldCurve.getDiscountFactor(optionExpiry) * (1 - creditCurve.getSurvivalProbability(optionExpiry)) : 0.0;
    final double rpv01 = _pricer.annuity(forwardStartingCDS, yieldCurve, creditCurve, PriceType.CLEAN, 0);
    final double protLeg = _pricer.protectionLeg(forwardStartingCDS, yieldCurve, creditCurve, 0);
    final double fwdSpread = protLeg / rpv01;
    final double fwdPrice = (price - fep) / rpv01;
    return BlackFormulaRepository.impliedVolatility(fwdPrice, fwdSpread, strike, optionExpiry, isPayer);
  }
}
