/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.DigitalOptionFunction;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for digital Forex option transactions with Black function and a volatility provider.
 * @deprecated Use {@link ForexOptionDigitalBlackSmileMethod}
 */
@Deprecated
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

  /** Normal distribution function */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the digital option with the Black function and a volatility from a volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double expiry = optionForex.getExpirationTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double dfDomestic;
    final double dfForeign;
    final double amount;
    final double omega;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, optionForex.getExpirationTime(), forward, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(domesticCcy, pv);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValue((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the currency exposure of the digital option with the Black function and a volatility from a volatility surface. The exposure is computed in both option currencies.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double expiry = optionForex.getExpirationTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double dfDomestic;
    final double dfForeign;
    final double amount;
    final double omega;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      dfDomestic = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = smile.getCurve(optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(optionForex.getUnderlyingForex().getPaymentTime());
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, optionForex.getExpirationTime(), forward, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    final double deltaSpot = amount * dfDomestic * NORMAL.getPDF(dM) * omega / (sigmaRootT * spot) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(foreignCcy, deltaSpot);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot + PV
    currencyExposure[1] = CurrencyAmount.of(domesticCcy, -deltaSpot * spot + pv);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
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
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final double expiry = optionForex.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double amount;
    final String foreignCurveName;
    final String domesticCurveName;
    final double omega;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, optionForex.getExpirationTime(), forward, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    // Backward sweep
    final double pvBar = 1.0;
    final double dMBar = amount * dfDomestic * NORMAL.getPDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double forwardBar = 1 / (forward * sigmaRootT) * dMBar;
    final double dfForeignBar = spot / dfDomestic * forwardBar;
    final double dfDomesticBar = -spot / (dfDomestic * dfDomestic) * dfForeign * forwardBar + pv / dfDomestic * pvBar;
    final double rForeignBar = -payTime * dfForeign * dfForeignBar;
    final double rDomesticBar = -payTime * dfDomestic * dfDomesticBar;
    // Sensitivity object
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar));
    final Map<String, List<DoublesPair>> resultForeignMap = new HashMap<>();
    resultForeignMap.put(foreignCurveName, listForeign);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultForeignMap);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar));
    final Map<String, List<DoublesPair>> resultDomesticMap = new HashMap<>();
    resultDomesticMap.put(domesticCurveName, listDomestic);
    result = result.plus(new InterestRateCurveSensitivity(resultDomesticMap));
    return MultipleCurrencyInterestRateCurveSensitivity.of(domesticCcy, result);
  }

  /**
   * Present value curve sensitivity with a generic instrument as argument.
   * @param instrument A Digital Forex option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The curve sensitivity.
   */
  @Override
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueCurveSensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the volatility sensitivity of the digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final double expiry = optionForex.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double amount;
    final String foreignCurveName;
    final String domesticCurveName;
    final double omega;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, optionForex.getExpirationTime(), forward, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    // Backward sweep
    final double pvBar = 1.0;
    final double dMBar = amount * dfDomestic * NORMAL.getPDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double sigmaRootTBar = (-Math.log(forward / strike) / (sigmaRootT * sigmaRootT) - 0.5) * dMBar;
    final double volatilityBar = Math.sqrt(expiry) * sigmaRootTBar;
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(), (foreignCcy == smile.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    // Implementation note: The strike should be in the same currency order as the input data.
    final SurfaceValue result = SurfaceValue.from(point, volatilityBar);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(foreignCcy, domesticCcy, result);
    return sensi;
  }

  /**
   * Computes the implied Black volatility of the digital option.
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final String foreignCurveName;
    final String domesticCurveName;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, optionForex.getExpirationTime(), forward, forward);
    return volatility;
  }

  /**
   * Computes the present value volatility sensitivity with a generic instrument as argument.
   * @param instrument A Digital Forex option.
   * @param curves The volatility and curves description (SmileDeltaTermStructureDataBundle).
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof ForexOptionDigital, "Digital Forex option");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Smile delta data bundle required");
    return presentValueBlackVolatilitySensitivity((ForexOptionDigital) instrument, (SmileDeltaTermStructureDataBundle) curves);
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionForex, smile); // In dom ccy
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smile.getVolatilityModel();

    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final double expiry = optionForex.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(payTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(payTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final VolatilityAndBucketedSensitivities volAndSensitivities = FXVolatilityUtils.getVolatilityAndSensitivities(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(), (foreignCcy == smile.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int loopexp = 0; loopexp < volatilityModel.getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < volatilityModel.getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex().getCurrency2(),
        new DoubleMatrix1D(volatilityModel.getTimeToExpiration()), new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

  /**
   * Computes the relative delta of the Forex option. The relative delta is the amount in the foreign currency equivalent to the option up to the first order divided by the option notional.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The delta.
   */
  public double deltaRelative(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile, final boolean directQuote) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    final double deltaDirect = DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
    if (directQuote) {
      return deltaDirect;
    }
    final double deltaReverse = -deltaDirect * spot * spot;
    return deltaReverse;
  }

  /**
   * Computes the delta of the Forex option. The delta is the first order derivative of the option present value to the spot fx rate.
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @param directQuote Flag indicating if the delta should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The delta.
   */
  public CurrencyAmount delta(final ForexOptionDigital optionForex, final YieldCurveBundle curves, final boolean directQuote) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double deltaRelative = deltaRelative(optionForex, smile, directQuote);
    final Currency domesticCcy;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
    } else {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
    }
    return CurrencyAmount.of(domesticCcy, deltaRelative * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the relative gamma of the Forex option.
   * The relative gamma is the second order derivative of the pv divided by the option notional.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public double gammaRelative(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile, final boolean directQuote) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    final double gammaDirect = DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
    if (directQuote) {
      return gammaDirect;
    }
    final double deltaDirect = DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot * spot;
    return gamma;
  }

  /**
   * Computes the gamma of the Forex option. The gamma is the second order derivative of the option present value to the spot fx rate.
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gamma(final ForexOptionDigital optionForex, final YieldCurveBundle curves, final boolean directQuote) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final Currency domesticCcy;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
    } else {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
    }
    final double gammaRelative = gammaRelative(optionForex, smile, directQuote);
    return CurrencyAmount.of(domesticCcy, gammaRelative * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the relative gamma of the Forex option multiplied by the spot rate.
   * The relative gamma is the second oder derivative of the pv relative to the option notional.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public double gammaRelativeSpot(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile, final boolean directQuote) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smile, "Smile");
    ArgumentChecker.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double logSign = (optionForex.isLong() ? 1.0 : -1.0);
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double gammaDirect = DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * logSign;
    if (directQuote) {
      return gammaDirect * spot;
    }
    final double deltaDirect = DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * logSign;
    final double gamma = (gammaDirect * spot + 2 * deltaDirect) * spot * spot;
    return gamma;
  }

  /**
   * Computes the gamma of the Forex option multiplied by the spot rate. The gamma is the second order derivative of the pv.
   * The reason to multiply by the spot rate is to be able to compute the change of delta for a relative increase of e of the spot rate (from X to X(1+e)).
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @param directQuote Flag indicating if the gamma should be computed with respect to the direct quote (1 foreign = x domestic) or the reverse quote (1 domestic = x foreign)
   * @return The gamma.
   */
  public CurrencyAmount gammaSpot(final ForexOptionDigital optionForex, final YieldCurveBundle curves, final boolean directQuote) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final Currency domesticCcy;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
    } else {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
    }
    final double gammaRelativeSpot = gammaRelativeSpot(optionForex, smile, directQuote);
    return CurrencyAmount.of(domesticCcy, gammaRelativeSpot * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount()));
  }

  /**
   * Computes the theta (derivative with respect to the time). The theta is not scaled.
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The theta
   */
  public CurrencyAmount forwardTheta(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    final double theta = DigitalOptionFunction.theta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign
        * Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
    return CurrencyAmount.of(optionForex.getUnderlyingForex().getCurrency2(), theta);
  }

  /**
   * Computes the spot delta (first derivative with respect to spot).
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The spot delta
   */
  public double spotDeltaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
  }

  /**
   * Computes the forward delta (first derivative with respect to forward).
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The forward delta
   */
  public double forwardDeltaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.delta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign / dfForeign;
  }

  /**
   * Computes the spot gamma (second derivative with respect to spot).
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The spot gamma
   */
  public double spotGammaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
  }

  /**
   * Computes the forward gamma (second derivative with respect to forward).
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The forward gamma
   */
  public double forwardGammaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.gamma(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign * dfDomestic / dfForeign / dfForeign;
  }

  /**
   * Computes the forward vega (first derivative with respect to spot).
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The forward vega
   */
  public double forwardVegaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.vega(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign / dfDomestic;
  }

  /**
   * Computes the forward driftless theta (derivative with respect to the time). The theta is not scaled.
   * Reference on driftless theta: The complete guide to Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The forward driftless theta
   */
  public double forwardDriftlessThetaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.driftlessTheta(forward, strike, expiry, volatility, isCall) * longSign;
  }

  /**
   * Computes the spot theta (derivative with respect to the time). The theta is not scaled.
   * Reference on theta: The complete guide to Option Pricing Formula (2007), E. G. Haug, Mc Graw Hill, p. 67, equation (2.43)
   * @param optionForex The Forex option.
   * @param curves The yield curve bundle.
   * @return The forward driftless theta
   */
  public double thetaTheoretical(final ForexOptionDigital optionForex, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "Curves");
    ArgumentChecker.isTrue(curves instanceof SmileDeltaTermStructureDataBundle, "Yield curve bundle should contain smile data");
    final SmileDeltaTermStructureDataBundle smile = (SmileDeltaTermStructureDataBundle) curves;
    final double paymentTime = optionForex.getUnderlyingForex().getPaymentTime();
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final String foreignCurveName;
    final String domesticCurveName;
    boolean isCall;
    if (optionForex.payDomestic()) {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      isCall = optionForex.isCall();
    } else {
      foreignCurveName = optionForex.getUnderlyingForex().getPaymentCurrency2().getFundingCurveName();
      domesticCurveName = optionForex.getUnderlyingForex().getPaymentCurrency1().getFundingCurveName();
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      isCall = !optionForex.isCall();
    }
    final double dfDomestic = smile.getCurve(domesticCurveName).getDiscountFactor(paymentTime);
    final double dfForeign = smile.getCurve(foreignCurveName).getDiscountFactor(paymentTime);
    final double rDomestic = smile.getCurve(domesticCurveName).getInterestRate(paymentTime);
    final double rForeign = smile.getCurve(foreignCurveName).getInterestRate(paymentTime);
    final double spot = smile.getFxRates().getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double expiry = optionForex.getExpirationTime();
    final double volatility = FXVolatilityUtils.getVolatility(smile, foreignCcy, domesticCcy, expiry, forward, forward);
    final double longSign = (optionForex.isLong() ? 1.0 : -1.0);
    return DigitalOptionFunction.theta(spot, strike, expiry, volatility, rDomestic, rDomestic - rForeign, isCall) * longSign;
  }
}
