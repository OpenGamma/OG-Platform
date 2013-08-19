/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalCallSpreadBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for digital Forex option transactions as a call or put spread option with the underlying vanilla price by a Black formula with implied volatility.
 * @deprecated Use {@link ForexOptionDigitalCallSpreadBlackSmileMethod}
 */
@Deprecated
public class ForexOptionDigitalCallSpreadBlackMethod extends ForexOptionDigitalCallSpreadMethod {

  /**
   * Constructor from the spread.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public ForexOptionDigitalCallSpreadBlackMethod(final double spread) {
    super(ForexOptionVanillaBlackSmileMethod.getInstance(), spread);
  }

  /**
   * Computes the relative gamma of the Forex option. The relative gamma is the second order derivative of the pv relative to the option notional.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The gamma.
   */
  public double gammaRelative(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    final CurrencyAmount gamma = gamma(optionDigital, smile);
    return gamma.getAmount() / Math.abs(optionDigital.getUnderlyingForex().getPaymentCurrency2().getAmount());
  }

  /**
   * Computes the gamma of the Forex option. The relative is the second order derivative of the pv.
   * @param optionDigital The option.
   * @param curves The yield curve bundle.
   * @return The gamma.
   */
  public CurrencyAmount gamma(final ForexOptionDigital optionDigital, final YieldCurveBundle curves) {
    Validate.notNull(optionDigital, "Forex option");
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    final CurrencyAmount gammaM = ((ForexOptionVanillaBlackSmileMethod) getBaseMethod()).gamma(callSpread[0], smile, optionDigital.payDomestic());
    final CurrencyAmount gammaP = ((ForexOptionVanillaBlackSmileMethod) getBaseMethod()).gamma(callSpread[1], smile, optionDigital.payDomestic());
    return gammaM.plus(gammaP);
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionDigital The option.
   * @param curves The yield curve bundle.
   * @return The gamma.
   */
  public CurrencyAmount gammaSpot(final ForexOptionDigital optionDigital, final YieldCurveBundle curves) {
    Validate.notNull(optionDigital, "Forex option");
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    final CurrencyAmount gammaM = ((ForexOptionVanillaBlackSmileMethod) getBaseMethod()).gammaSpot(callSpread[0], smile, optionDigital.payDomestic());
    final CurrencyAmount gammaP = ((ForexOptionVanillaBlackSmileMethod) getBaseMethod()).gammaSpot(callSpread[1], smile, optionDigital.payDomestic());
    return gammaM.plus(gammaP);
  }

  /**
   * Computes the volatility sensitivity of the vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionDigital The option.
   * @param smile The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionDigital optionDigital, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionDigital, "Forex option difital");
    Validate.notNull(smile, "Curve and smile data");
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    final ForexOptionVanilla[] callSpread = callSpread(optionDigital, getSpread());
    // Spread value
    final PresentValueForexBlackVolatilitySensitivity pvbsM = ((ForexOptionVanillaBlackSmileMethod) getBaseMethod()).presentValueBlackVolatilitySensitivity(callSpread[0], smile);
    final PresentValueForexBlackVolatilitySensitivity pvbsP = ((ForexOptionVanillaBlackSmileMethod) getBaseMethod()).presentValueBlackVolatilitySensitivity(callSpread[1], smile);
    return pvbsM.plus(pvbsP);
  }

  /**
   * Computes the present value volatility sensitivity with a generic instrument as argument.
   * @param instrument A Digital Forex option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueBlackVolatilitySensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a digital option. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionDigital The option.
   * @param curves The yield curve bundle.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionDigital optionDigital, final YieldCurveBundle curves) {
    Validate.notNull(optionDigital, "Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    Validate.isTrue(smile.checkCurrencies(optionDigital.getCurrency1(), optionDigital.getCurrency2()), "Option currencies not compatible with smile data");
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionDigital, smile); // In ccy2
    final double df = smile.getCurve(optionDigital.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionDigital.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRates().getFxRate(optionDigital.getCurrency1(), optionDigital.getCurrency2());
    final double forward = spot * smile.getCurve(optionDigital.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionDigital.getUnderlyingForex().getPaymentTime())
        / df;
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smile.getVolatilityModel();
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (final DoublesPair point : pointSensitivity.getVega().getMap().keySet()) {
      final VolatilityAndBucketedSensitivities volAndSensitivities = FXVolatilityUtils.getVolatilityAndSensitivities(smile, optionDigital.getCurrency1(), optionDigital.getCurrency2(),
          optionDigital.getExpirationTime(), point.second, forward);
      final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
      for (int loopexp = 0; loopexp < volatilityModel.getNumberExpiration(); loopexp++) {
        for (int loopstrike = 0; loopstrike < volatilityModel.getNumberStrike(); loopstrike++) {
          vega[loopexp][loopstrike] += nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
        }
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(optionDigital.getUnderlyingForex().getCurrency1(), optionDigital.getUnderlyingForex().getCurrency2(), new DoubleMatrix1D(
        volatilityModel.getTimeToExpiration()), new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
