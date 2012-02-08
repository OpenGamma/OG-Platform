/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
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
   * The relative spread used in the call-spread pricing.
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
   * Computes the present value of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionDigital, "Forex option difital");
    Validate.notNull(smile, "Curve and smile data");
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    // Create call spread
    double strike = optionDigital.getStrike();
    double strikeM = strike * (1 - _spread);
    double strikeP = strike * (1 + _spread);
    Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    MultipleCurrencyAmount pvP = _baseMethod.presentValue(vanillaP, smile);
    MultipleCurrencyAmount pvM = _baseMethod.presentValue(vanillaM, smile);
    return pvM.plus(pvP.multipliedBy(-1.0)).multipliedBy(omega / (strikeP - strikeM) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
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
    // Create call spread
    double strike = optionDigital.getStrike();
    double strikeM = strike * (1 - _spread);
    double strikeP = strike * (1 + _spread);
    Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    MultipleCurrencyAmount ceP = _baseMethod.currencyExposure(vanillaP, smile);
    MultipleCurrencyAmount ceM = _baseMethod.currencyExposure(vanillaM, smile);
    return ceM.plus(ceP.multipliedBy(-1.0)).multipliedBy(omega / (strikeP - strikeM) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
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
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    // Create call spread
    double strike = optionDigital.getStrike();
    double strikeM = strike * (1 - _spread);
    double strikeP = strike * (1 + _spread);
    Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeM));
    Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(1.0), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeP));
    ForexOptionVanilla vanillaM = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    ForexOptionVanilla vanillaP = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), optionDigital.isCall(), optionDigital.isLong());
    // Spread value
    double omega = optionDigital.isCall() ? +1.0 : -1.0;
    MultipleCurrencyInterestRateCurveSensitivity pvcsP = _baseMethod.presentValueCurveSensitivity(vanillaP, smile);
    MultipleCurrencyInterestRateCurveSensitivity pvcsM = _baseMethod.presentValueCurveSensitivity(vanillaM, smile);
    return pvcsM.plus(pvcsP.multipliedBy(-1.0)).multipliedBy(omega / (strikeP - strikeM) * Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount()));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

}
