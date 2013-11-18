/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
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
 */
public final class ForexOptionDigitalBlackSmileMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionDigitalBlackSmileMethod INSTANCE = new ForexOptionDigitalBlackSmileMethod();

  /**
   * Private constructor.
   */
  private ForexOptionDigitalBlackSmileMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexOptionDigitalBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The normal probability distribution used.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the digital option with the Black function and a volatility from a volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The present value. The value is in the domestic currency (currency 2).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionDigital optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
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
      dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), optionForex.getUnderlyingForex().getPaymentTime());
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency1(), optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency2(), optionForex.getUnderlyingForex().getPaymentTime());
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(domesticCcy, pv);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Computes the currency exposure of the digital option with the Black function and a volatility from a volatility surface. The exposure is computed in both option currencies.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The currency exposure
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionDigital optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
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
      dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), optionForex.getUnderlyingForex().getPaymentTime());
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency1(), optionForex.getUnderlyingForex().getPaymentTime());
      dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency2(), optionForex.getUnderlyingForex().getPaymentTime());
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
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

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not taken into account. It is the curve
   * sensitivity in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexOptionDigital optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final double expiry = optionForex.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double amount;
    final double omega;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double dfDomestic = multicurves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = multicurves.getDiscountFactor(foreignCcy, payTime);
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
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
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar));
    resultMap.put(multicurves.getName(foreignCcy), listForeign);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar));
    resultMap.put(multicurves.getName(domesticCcy), listDomestic);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(domesticCcy, result);
  }

  /**
   * Computes the volatility sensitivity of the digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to the computed Black implied volatility and not with respect to the volatility surface input.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The volatility sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionDigital optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final double expiry = optionForex.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    final double amount;
    final double omega;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency2().getAmount());
      omega = optionForex.isCall() ? 1.0 : -1.0;
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
      amount = Math.abs(optionForex.getUnderlyingForex().getPaymentCurrency1().getAmount());
      omega = optionForex.isCall() ? -1.0 : 1.0;
    }
    final double dfDomestic = multicurves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = multicurves.getDiscountFactor(foreignCcy, payTime);
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smileMulticurves.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    // Backward sweep
    final double pvBar = 1.0;
    final double dMBar = amount * dfDomestic * NORMAL.getPDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0) * omega * pvBar;
    final double sigmaRootTBar = (-Math.log(forward / strike) / (sigmaRootT * sigmaRootT) - 0.5) * dMBar;
    final double volatilityBar = Math.sqrt(expiry) * sigmaRootTBar;
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(), (foreignCcy == smileMulticurves.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    // Implementation note: The strike should be in the same currency order as the input data.
    final SurfaceValue result = SurfaceValue.from(point, volatilityBar);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(foreignCcy, domesticCcy, result);
    return sensi;
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionDigital optionForex,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionForex, smileMulticurves); // In dom ccy
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smileMulticurves.getVolatility();
    final double payTime = optionForex.getUnderlyingForex().getPaymentTime();
    final double expiry = optionForex.getExpirationTime();
    // Forward sweep
    final Currency domesticCcy;
    final Currency foreignCcy;
    final double strike;
    if (optionForex.payDomestic()) {
      domesticCcy = optionForex.getUnderlyingForex().getCurrency2();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency1();
      strike = optionForex.getStrike();
    } else {
      strike = 1.0 / optionForex.getStrike();
      domesticCcy = optionForex.getUnderlyingForex().getCurrency1();
      foreignCcy = optionForex.getUnderlyingForex().getCurrency2();
    }
    final double dfDomestic = multicurves.getDiscountFactor(domesticCcy, payTime);
    final double dfForeign = multicurves.getDiscountFactor(foreignCcy, payTime);
    final double spot = multicurves.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final VolatilityAndBucketedSensitivities volAndSensitivities = smileMulticurves.getVolatilityAndSensitivities(foreignCcy, domesticCcy, expiry, strike, forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(), (foreignCcy == smileMulticurves.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int loopexp = 0; loopexp < volatilityModel.getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < volatilityModel.getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex().getCurrency2(), new DoubleMatrix1D(
        volatilityModel.getTimeToExpiration()), new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
