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

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.util.surface.SurfaceValue;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
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
    final double spot = smile.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
    final double sigmaRootT = volatility * Math.sqrt(expiry);
    final double dM = Math.log(forward / strike) / sigmaRootT - 0.5 * sigmaRootT;
    final double pv = amount * dfDomestic * NORMAL.getCDF(omega * dM) * (optionForex.isLong() ? 1.0 : -1.0);
    final CurrencyAmount priceCurrency = CurrencyAmount.of(domesticCcy, pv);
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
    final double spot = smile.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
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
    final double spot = smile.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
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
    final List<DoublesPair> listForeign = new ArrayList<DoublesPair>();
    listForeign.add(new DoublesPair(payTime, rForeignBar));
    final Map<String, List<DoublesPair>> resultForeignMap = new HashMap<String, List<DoublesPair>>();
    resultForeignMap.put(foreignCurveName, listForeign);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultForeignMap);
    final List<DoublesPair> listDomestic = new ArrayList<DoublesPair>();
    listDomestic.add(new DoublesPair(payTime, rDomesticBar));
    final Map<String, List<DoublesPair>> resultDomesticMap = new HashMap<String, List<DoublesPair>>();
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
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
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
    final double spot = smile.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    final double volatility = smile.getVolatility(foreignCcy, domesticCcy, optionForex.getExpirationTime(), strike, forward);
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
   * Computes the volatility sensitivity with respect to input data for a digital option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionForex The Forex option.
   * @param smile The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionDigital optionForex, final SmileDeltaTermStructureDataBundle smile) {
    Validate.notNull(optionForex, "Forex option");
    Validate.notNull(smile, "Smile");
    Validate.isTrue(smile.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionForex, smile); // In dom ccy
    final double[][] nodeWeight = new double[smile.getSmile().getNumberExpiration()][smile.getSmile().getNumberStrike()];

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
    final double spot = smile.getFxRate(foreignCcy, domesticCcy);
    final double forward = spot * dfForeign / dfDomestic;
    smile.getVolatility(foreignCcy, domesticCcy, expiry, strike, forward, nodeWeight);
    final DoublesPair point = DoublesPair.of(optionForex.getExpirationTime(), (foreignCcy == smile.getCurrencyPair().getFirst()) ? strike : 1.0 / strike);
    final double[][] vega = new double[smile.getSmile().getNumberExpiration()][smile.getSmile().getNumberStrike()];
    for (int loopexp = 0; loopexp < smile.getSmile().getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < smile.getSmile().getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(optionForex.getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingForex().getCurrency2(), new DoubleMatrix1D(smile
        .getSmile().getTimeToExpiration()), new DoubleMatrix1D(smile.getSmile().getDeltaFull()), new DoubleMatrix2D(vega));
  }

}
