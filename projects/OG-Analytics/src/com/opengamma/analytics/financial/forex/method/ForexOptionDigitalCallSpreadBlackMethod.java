/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * Pricing method for digital Forex option transactions as a call or put spread option with the underlying vanilla price by a Black formula with implied volatility.
 */
public class ForexOptionDigitalCallSpreadBlackMethod extends ForexOptionDigitalCallSpreadMethod {

  /**
   * Constructor from the spread.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public ForexOptionDigitalCallSpreadBlackMethod(final double spread) {
    super(ForexOptionVanillaBlackMethod.getInstance(), spread);
  }

  /**
   * Computes the volatility sensitivity of the vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueVolatilitySensitivity(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionDigital, "Forex option difital");
    Validate.notNull(smile, "Curve and smile data");
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    PresentValueForexBlackVolatilitySensitivity pvbsP = ((ForexOptionVanillaBlackMethod) getBaseMethod()).presentValueVolatilitySensitivity(callSpread[0], smile);
    PresentValueForexBlackVolatilitySensitivity pvbsM = ((ForexOptionVanillaBlackMethod) getBaseMethod()).presentValueVolatilitySensitivity(callSpread[1], smile);
    return pvbsM.plus(pvbsP.multipliedBy(-1.0)).multipliedBy(
        omega / (callSpread[0].getStrike() - callSpread[1].getStrike()) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
  }

  /**
   * Computes the present value volatility sensitivity with a generic instrument as argument.
   * @param instrument A Digital Forex option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueVolatilitySensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueVolatilitySensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

}
