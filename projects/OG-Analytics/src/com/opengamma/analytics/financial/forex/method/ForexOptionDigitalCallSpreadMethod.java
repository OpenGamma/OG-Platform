/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for digital Forex option transactions as a call or put spread option.
 */
public class ForexOptionDigitalCallSpreadMethod implements ForexPricingMethod {

  /**
   * The base method for the pricing of standard vanilla options.
   */
  private final ForexPricingMethod _baseMethod;
  /**
   * The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  private final double _spread;

  /**
   * Constructor of the digital pricing method.
   * @param baseMethod The base method for the pricing of standard vanilla options.
   */
  public ForexOptionDigitalCallSpreadMethod(final ForexPricingMethod baseMethod) {
    _baseMethod = baseMethod;
    _spread = 0.0001;
  }

  /**
   * Constructor of the digital pricing method.
   * @param baseMethod The base method for the pricing of standard vanilla options.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public ForexOptionDigitalCallSpreadMethod(final ForexPricingMethod baseMethod, final double spread) {
    _baseMethod = baseMethod;
    _spread = spread;
  }

  /**
   * Computes the present value of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionDigital, "Forex option difital");
    Validate.notNull(smile, "Curve and smile data");
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    MultipleCurrencyAmount pvM = _baseMethod.presentValue(callSpread[0], smile);
    MultipleCurrencyAmount pvP = _baseMethod.presentValue(callSpread[1], smile);
    return pvM.plus(pvP.multipliedBy(-1.0)).multipliedBy(
        omega / (callSpread[1].getStrike() - callSpread[0].getStrike()) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the currency exposure of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionDigital, "Forex option difital");
    Validate.notNull(smile, "Curve and smile data");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    MultipleCurrencyAmount ceM = _baseMethod.currencyExposure(callSpread[0], smile);
    MultipleCurrencyAmount ceP = _baseMethod.currencyExposure(callSpread[1], smile);
    return ceM.plus(ceP.multipliedBy(-1.0)).multipliedBy(
        omega / (callSpread[1].getStrike() - callSpread[0].getStrike()) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the curve sensitivity of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionDigital, "Forex option");
    Validate.notNull(smile, "Smile");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    MultipleCurrencyInterestRateCurveSensitivity pvcsM = _baseMethod.presentValueCurveSensitivity(callSpread[0], smile);
    MultipleCurrencyInterestRateCurveSensitivity pvcsP = _baseMethod.presentValueCurveSensitivity(callSpread[1], smile);
    return pvcsM.plus(pvcsP.multipliedBy(-1.0)).multipliedBy(
        omega / (callSpread[1].getStrike() - callSpread[0].getStrike()) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  protected ForexOptionVanilla[] callSpread(final ForexOptionDigital optionDigital, final double spread) {
    ForexOptionVanilla[] callSpread = new ForexOptionVanilla[2];
    double strike = optionDigital.getStrike();
    double strikeM = strike * (1 - spread);
    double strikeP = strike * (1 + spread);
    Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeP));
    callSpread[0] = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    callSpread[1] = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    return callSpread;
  }

  /**
   * Gets the base pricing method.
   * @return The method.
   */
  public ForexPricingMethod getBaseMethod() {
    return _baseMethod;
  }

  /**
   * Gets the spread used for call spread.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

}
