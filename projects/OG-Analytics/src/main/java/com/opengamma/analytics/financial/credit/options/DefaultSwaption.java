/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;

/**
 * Pricer for option to enter a forward starting CDS (aka default swaption)
 */
public class DefaultSwaption {

  private final AnalyticCDSPricer _pricer = new AnalyticCDSPricer();

  /**
   * Price a default swaption
   * @param cds
   * @param yieldCurve
   * @param creditCurve
   * @param strike
   * @param optionExpiry
   * @param vol
   * @param isPayer
   * @param hasFrontEndProt
   * @return The price of the swaption
   */
  public double price(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double strike, final double optionExpiry, final double vol,
      final boolean isPayer, final boolean hasFrontEndProt) {

    //front end protection is worth zero for an option to be the seller of protection
    final double fep = isPayer && hasFrontEndProt ? cds.getLGD() * yieldCurve.getDiscountFactor(optionExpiry) * (1 - creditCurve.getSurvivalProbability(optionExpiry)) : 0.0;
    final double rpv01 = _pricer.pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.CLEAN);
    final double protLeg = _pricer.protectionLeg(cds, yieldCurve, creditCurve);
    final double fwdSpread = protLeg / rpv01;
    final double koVal = rpv01 * BlackFormulaRepository.price(fwdSpread, strike, optionExpiry, vol, isPayer);
    return koVal + fep;
  }

  public double impliedVol(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double strike, final double optionExpiry,
      final double price, final boolean isPayer, final boolean hasFrontEndProt) {
    final double fep = isPayer && hasFrontEndProt ? cds.getLGD() * yieldCurve.getDiscountFactor(optionExpiry) * (1 - creditCurve.getSurvivalProbability(optionExpiry)) : 0.0;
    final double rpv01 = _pricer.pvPremiumLegPerUnitSpread(cds, yieldCurve, creditCurve, PriceType.CLEAN);
    final double protLeg = _pricer.protectionLeg(cds, yieldCurve, creditCurve);
    final double fwdSpread = protLeg / rpv01;
    final double fwdPrice = (price - fep) / rpv01;
    return BlackFormulaRepository.impliedVolatility(fwdPrice, fwdSpread, strike, optionExpiry, isPayer);
  }
}
