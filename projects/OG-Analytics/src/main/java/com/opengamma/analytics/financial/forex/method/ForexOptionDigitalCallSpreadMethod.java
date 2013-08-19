/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalCallSpreadBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for digital Forex option transactions as a call or put spread option.
 * @deprecated Use {@link ForexOptionDigitalCallSpreadBlackSmileMethod}
 */
@Deprecated
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
    ArgumentChecker.notNull(optionDigital, "Forex option difital");
    ArgumentChecker.notNull(smile, "Curve and smile data");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    final MultipleCurrencyAmount pvM = _baseMethod.presentValue(callSpread[0], smile);
    final MultipleCurrencyAmount pvP = _baseMethod.presentValue(callSpread[1], smile);
    return pvM.plus(pvP);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the currency exposure of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    ArgumentChecker.notNull(optionDigital, "Forex option difital");
    ArgumentChecker.notNull(smile, "Curve and smile data");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    final MultipleCurrencyAmount ceM = _baseMethod.currencyExposure(callSpread[0], smile);
    final MultipleCurrencyAmount ceP = _baseMethod.currencyExposure(callSpread[1], smile);
    return ceM.plus(ceP);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the curve sensitivity of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    final MultipleCurrencyInterestRateCurveSensitivity pvcsM = _baseMethod.presentValueCurveSensitivity(callSpread[0], smile);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsP = _baseMethod.presentValueCurveSensitivity(callSpread[1], smile);
    return pvcsM.plus(pvcsP);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  protected ForexOptionVanilla[] callSpread(final ForexOptionDigital optionDigital, final double spread) {
    final ForexOptionVanilla[] callSpread = new ForexOptionVanilla[2];
    final double strike = optionDigital.getStrike();
    final double strikeM = strike * (1 - spread);
    final double strikeP = strike * (1 + spread);
    double amountPaid;
    double strikeRelM;
    double strikeRelP;
    if (optionDigital.payDomestic()) {
      amountPaid = Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount());
      strikeRelM = strikeM;
      strikeRelP = strikeP;
      final double amount = amountPaid / (strikeRelP - strikeRelM);
      final Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(amount),
          optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeRelM * amount));
      final Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(amount),
          optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeRelP * amount));
      callSpread[0] = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), optionDigital.isCall(),
          (optionDigital.isLong() == optionDigital.isCall()));
      callSpread[1] = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), optionDigital.isCall(),
          !(optionDigital.isLong() == optionDigital.isCall()));
    } else {
      amountPaid = Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency1().getAmount());
      strikeRelM = 1.0 / strikeP;
      strikeRelP = 1.0 / strikeM;
      final double amount = amountPaid / (strikeRelP - strikeRelM);
      final Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
          optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(-strikeRelM * amount));
      final Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(amount),
          optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(-strikeRelP * amount));
      callSpread[0] = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), !optionDigital.isCall(),
          !(optionDigital.isLong() == optionDigital.isCall()));
      callSpread[1] = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), !optionDigital.isCall(),
          (optionDigital.isLong() == optionDigital.isCall()));
    }
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
