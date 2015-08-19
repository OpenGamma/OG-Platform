/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for digital Forex option transactions as a call or put spread option.
 * The underlying vanilla options are priced with the ForexOptionVanillaBlackSmileMethod.
 */
public class ForexOptionDigitalCallSpreadBlackSmileMethod {

  /**
   * Default spread.
   */
  private static final double DEFAULT_SPREAD = 1.0e-4;

  /**
   * The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  private final double _spread;

  /**
   * The base method for the pricing of standard vanilla options.
   */
  private static final ForexOptionVanillaBlackSmileMethod BASE_METHOD = ForexOptionVanillaBlackSmileMethod.getInstance();

  /**
   * Constructor of the digital pricing method with default spread.
   */
  public ForexOptionDigitalCallSpreadBlackSmileMethod() {
    this(DEFAULT_SPREAD);
  }

  /**
   * Constructor of the digital pricing method.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public ForexOptionDigitalCallSpreadBlackSmileMethod(final double spread) {
    _spread = spread;
  }

  /**
   * Computes the present value of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital optionDigital, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    MultipleCurrencyAmount pvM = BASE_METHOD.presentValue(callSpread[0], smileMulticurves);
    MultipleCurrencyAmount pvP = BASE_METHOD.presentValue(callSpread[1], smileMulticurves);
    return pvM.plus(pvP);
  }

  /**
   * Computes the currency exposure of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital optionDigital, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    MultipleCurrencyAmount ceM = BASE_METHOD.currencyExposure(callSpread[0], smileMulticurves);
    MultipleCurrencyAmount ceP = BASE_METHOD.currencyExposure(callSpread[1], smileMulticurves);
    return ceM.plus(ceP);
  }

  /**
   * Computes the curve sensitivity of a digital Forex option by call-spread.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexOptionDigital optionDigital, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, _spread);
    // Spread value
    MultipleCurrencyMulticurveSensitivity pvcsM = BASE_METHOD.presentValueCurveSensitivity(callSpread[0], smileMulticurves);
    MultipleCurrencyMulticurveSensitivity pvcsP = BASE_METHOD.presentValueCurveSensitivity(callSpread[1], smileMulticurves);
    return pvcsM.plus(pvcsP);
  }

  /**
   * Computes the delta of the Forex option. The relative is the first order derivative of the pv.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The delta.
   */
  public CurrencyAmount delta(ForexOptionDigital optionDigital, BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    CurrencyAmount deltaM = BASE_METHOD.delta(callSpread[0], smileMulticurves, optionDigital.payDomestic());
    CurrencyAmount deltaP = BASE_METHOD.delta(callSpread[1], smileMulticurves, optionDigital.payDomestic());
    return deltaM.plus(deltaP);
  }

  /**
   * Computes the relative gamma of the Forex option. The relative gamma is the second order derivative of the pv relative to the option notional.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The gamma.
   */
  public double gammaRelative(final ForexOptionDigital optionDigital,
      final BlackForexSmileProviderInterface smileMulticurves) {
    final CurrencyAmount gamma = gamma(optionDigital, smileMulticurves);
    return gamma.getAmount() / Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount());
  }

  /**
   * Computes the gamma of the Forex option. The relative is the second order derivative of the pv.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The gamma.
   */
  public CurrencyAmount gamma(final ForexOptionDigital optionDigital, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    final CurrencyAmount gammaM = BASE_METHOD.gamma(callSpread[0], smileMulticurves, optionDigital.payDomestic());
    final CurrencyAmount gammaP = BASE_METHOD.gamma(callSpread[1], smileMulticurves, optionDigital.payDomestic());
    return gammaM.plus(gammaP);
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The gamma.
   */
  public CurrencyAmount gammaSpot(final ForexOptionDigital optionDigital, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    final CurrencyAmount gammaM = BASE_METHOD.gammaSpot(callSpread[0], smileMulticurves, optionDigital.payDomestic());
    final CurrencyAmount gammaP = BASE_METHOD.gammaSpot(callSpread[1], smileMulticurves, optionDigital.payDomestic());
    return gammaM.plus(gammaP);
  }

  /**
   * Computes the volatility sensitivity of the vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionDigital optionDigital, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    final PresentValueForexBlackVolatilitySensitivity pvbsM = BASE_METHOD.presentValueBlackVolatilitySensitivity(callSpread[0], smileMulticurves);
    final PresentValueForexBlackVolatilitySensitivity pvbsP = BASE_METHOD.presentValueBlackVolatilitySensitivity(callSpread[1], smileMulticurves);
    return pvbsM.plus(pvbsP);
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a digital option. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionDigital The option.
   * @param smileMulticurves The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionDigital optionDigital,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionDigital, "Forex option");
    Validate.notNull(smileMulticurves, "Smile");
    Validate.isTrue(smileMulticurves.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionDigital, smileMulticurves); // In ccy2
    final double df = multicurves.getDiscountFactor(optionDigital.getCurrency2(), optionDigital.getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(optionDigital.getCurrency1(), optionDigital.getCurrency2());
    final double forward = spot * multicurves.getDiscountFactor(optionDigital.getCurrency1(), optionDigital.getUnderlyingForex().getPaymentTime()) / df;
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smileMulticurves.getVolatility();
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (final DoublesPair point : pointSensitivity.getVega().getMap().keySet()) {
      final VolatilityAndBucketedSensitivities volAndSensitivities = smileMulticurves.getVolatilityAndSensitivities(optionDigital.getCurrency1(), optionDigital.getCurrency2(),
          optionDigital.getExpirationTime(), point.second, forward);
      final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
      for (int loopexp = 0; loopexp < volatilityModel.getNumberExpiration(); loopexp++) {
        for (int loopstrike = 0; loopstrike < volatilityModel.getNumberStrike(); loopstrike++) {
          vega[loopexp][loopstrike] += nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
        }
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(
        optionDigital.getUnderlyingForex().getCurrency1(),
        optionDigital.getUnderlyingForex().getCurrency2(),
        new DoubleMatrix1D(volatilityModel.getTimeToExpiration()),
        new DoubleMatrix1D(volatilityModel.getDeltaFullReverse()),
        new DoubleMatrix2D(vega));
  }

  protected ForexOptionVanilla[] callSpread(final ForexOptionDigital optionDigital, final double spread) {
    ForexOptionVanilla[] callSpread = new ForexOptionVanilla[2];
    double strike = optionDigital.getStrike();
    double strikeM = strike * (1 - spread);
    double strikeP = strike * (1 + spread);
    double amountPaid;
    double strikeRelM;
    double strikeRelP;
    if (optionDigital.payDomestic()) {
      amountPaid = Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount());
      strikeRelM = strikeM;
      strikeRelP = strikeP;
      double amount = amountPaid / (strikeRelP - strikeRelM);
      Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(amount), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeRelM * amount));
      Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(amount), optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(-strikeRelP * amount));
      callSpread[0] = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), optionDigital.isCall(), (optionDigital.isLong() == optionDigital.isCall()));
      callSpread[1] = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), optionDigital.isCall(), !(optionDigital.isLong() == optionDigital.isCall()));
    } else {
      amountPaid = Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency1().getAmount());
      strikeRelM = 1.0 / strikeP;
      strikeRelP = 1.0 / strikeM;
      double amount = amountPaid / (strikeRelP - strikeRelM);
      Forex forexM = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(-strikeRelM * amount));
      Forex forexP = new Forex(optionDigital.getUnderlyingForex().getPaymentCurrency2().withAmount(amount), optionDigital.getUnderlyingForex().getPaymentCurrency1().withAmount(-strikeRelP * amount));
      callSpread[0] = new ForexOptionVanilla(forexM, optionDigital.getExpirationTime(), !optionDigital.isCall(), !(optionDigital.isLong() == optionDigital.isCall()));
      callSpread[1] = new ForexOptionVanilla(forexP, optionDigital.getExpirationTime(), !optionDigital.isCall(), (optionDigital.isLong() == optionDigital.isCall()));
    }
    return callSpread;
  }

  /**
   * Gets the spread used for call spread.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

}
