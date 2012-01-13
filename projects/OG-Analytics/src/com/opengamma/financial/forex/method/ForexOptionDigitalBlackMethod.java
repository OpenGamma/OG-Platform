/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.surface.SurfaceValue;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for digital Forex option transactions with Black function and a volatility provider.
 */
public final class ForexOptionDigitalBlackMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionDigitalBlackMethod INSTANCE = new ForexOptionDigitalBlackMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexOptionDigitalBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexOptionDigitalBlackMethod() {
  }

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the digital option with the Black function and a volatility from a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double strike = optionForex.getStrike();
    final double expiry = optionForex.getExpirationTime();
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getExpirationTime(), optionForex.getStrike(), forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    final int omega = optionForex.isCall() ? 1 : -1;
    final double pv = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), pv);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the currency exposure of the digital option with the Black function and a volatility from a volatility surface. The exposure is computed in both option currencies.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double strike = optionForex.getStrike();
    final double expiry = optionForex.getExpirationTime();
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getExpirationTime(), optionForex.getStrike(), forward);

    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    final int omega = optionForex.isCall() ? 1 : -1;
    final double pv = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);

    final double deltaSpot = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getPDF(dM) * omega / (sigmaRootT * spot)
        * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency1(), deltaSpot);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot + PV
    currencyExposure[1] = CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), -deltaSpot * spot + pv);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return currencyExposure((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not taken into account. It is the curve
   * sensitivity in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param smile The smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final String domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
    final String foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
    final double strike = optionForex.getStrike();
    final double expiry = optionForex.getExpirationTime();
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    // Forward sweep
    final double dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(payTime);
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getExpirationTime(), optionForex.getStrike(), forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    final int omega = optionForex.isCall() ? 1 : -1;
    final double pv = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    // Backward sweep
    final double pvBar = 1.0;
    final double dMBar = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getPDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double forwardBar = 1 / (forward * sigmaRootT) * dMBar;
    final double dfForeignBar = spot / dfDomestic * forwardBar;
    final double dfDomesticBar = -spot / (dfDomestic * dfDomestic) * dfForeign * forwardBar + pv / dfDomestic * pvBar;
    final double rForeignBar = -payTime * dfForeign * dfForeignBar;
    final double rDomesticBar = -payTime * dfDomestic * dfDomesticBar;
    // Sensitivity object
    final List<DoublesPair> listForeign = new ArrayList<DoublesPair>();
    listForeign.add(new DoublesPair(payTime, rForeignBar));
    final Map<String, List<DoublesPair>> resultForeignMap = new HashMap<String, List<DoublesPair>>();
    resultForeignMap.put(foreignCurveName, listForeign);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultForeignMap);
    final List<DoublesPair> listDomestic = new ArrayList<DoublesPair>();
    listDomestic.add(new DoublesPair(payTime, rDomesticBar));
    final Map<String, List<DoublesPair>> resultDomesticMap = new HashMap<String, List<DoublesPair>>();
    resultDomesticMap.put(domesticCurveName, listDomestic);
    result = result.add(new InterestRateCurveSensitivity(resultDomesticMap));
    return MultipleCurrencyInterestRateCurveSensitivity.of(optionForex.getUnderlyingForex().getCurrency2(), result);
  }

  /**
   * Present value curve sensitivity with a generic instrument as argument.
   * @param instrument A Digital Forex option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The curve sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    Validate.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the volatility sensitivity of the digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueVolatilitySensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {

    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final String domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
    final String foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
    final double strike = optionForex.getStrike();
    final double expiry = optionForex.getExpirationTime();
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    // Forward sweep
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getExpirationTime(), optionForex.getStrike(), forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT + 0.5 * sigmaRootT;
    final int omega = optionForex.isCall() ? 1 : -1;
    // Backward sweep
    double pvBar = 1.0;
    final double dMBar = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount()) * dfDomestic * NORMAL.getPDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double sigmaRootTBar = (-Math.log(forward / strike) / (sigmaRootT * sigmaRootT) + 0.5) * dMBar;
    final double volatilityBar = Math.sqrt(expiry) * sigmaRootTBar;
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(),
        (optionForex.getCurrency1() == smile.getCurrencyPair().getFirst()) ? optionForex.getStrike() : 1.0 / optionForex.getStrike());
    // Implementation note: The strike should be in the same currency order as the input data.
    SurfaceValue result = SurfaceValue.from(point, volatilityBar);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex()
        .getCurrency2(), result);
    return sensi;
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

  /**
   * Computes the volatility sensitivity with respect to input data for a digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueVolatilityNodeSensitivityDataBundle presentValueVolatilityNodeSensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueVolatilitySensitivity(optionForex, smile); // In ccy2
    final double[][] nodeWeight = new double[smile.getSmile().getNumberExpiration()][smile.getSmile().getNumberStrike()];
    final double df = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
    final double spot = smile.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime())
        / df;
    smile.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getExpirationTime(), optionForex.getStrike(), forward, nodeWeight);
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(),
        (optionForex.getCurrency1() == smile.getCurrencyPair().getFirst()) ? optionForex.getStrike() : 1.0 / optionForex.getStrike());
    final double[][] vega = new double[smile.getSmile().getNumberExpiration()][smile.getSmile().getNumberStrike()];
    for (int loopexp = 0; loopexp < smile.getSmile().getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < smile.getSmile().getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueVolatilityNodeSensitivityDataBundle(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex().getCurrency2(), new DoubleMatrix1D(smile.getSmile()
        .getTimeToExpiration()), new DoubleMatrix1D(smile.getSmile().getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
