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

import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackBarrierPriceFunction;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Pricing method for single barrier Forex option transactions in the Black world.
 */
public final class ForexOptionSingleBarrierBlackMethod {

  /**
   * The method unique instance.
   */
  private static final ForexOptionSingleBarrierBlackMethod INSTANCE = new ForexOptionSingleBarrierBlackMethod();

  /**
   * Private constructor.
   */
  private ForexOptionSingleBarrierBlackMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexOptionSingleBarrierBlackMethod getInstance() {
    return INSTANCE;
  }

  private static final double DEFAULT_GAMMA_SHIFT = 0.00001; // 0.1 basis point
  private static final double DEFAULT_VOMMA_SHIFT = 0.00001; // 0.1 basis point
  private static final double DEFAULT_VANNA_SHIFT = 0.00001; // 0.1 basis point

  /**
   * The Black function used in the barrier pricing.
   */
  private static final BlackBarrierPriceFunction BARRIER_FUNCTION = BlackBarrierPriceFunction.getInstance();

  /**
   * Computes the present value for single barrier Forex option in Black model (log-normal spot rate).
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The present value (in domestic currency).
   */
  public MultipleCurrencyAmount presentValue(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * dfForeign / dfDomestic;
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex
        .getUnderlyingOption().getStrike(), forward);
    double price = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volatility);
    price *= Math.abs(foreignAmount) * sign;
    final CurrencyAmount priceCurrency = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(), price);
    return MultipleCurrencyAmount.of(priceCurrency);
  }

  /**
   * Computes the currency exposure for single barrier Forex option in Black model (log-normal spot rate). The sensitivity of the volatility on the spot
   * is not taken into account. It is the currency exposure in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The currency exposure.
   */
  public MultipleCurrencyAmount currencyExposure(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex
        .getUnderlyingOption().getStrike(), forward);
    final double[] priceDerivatives = new double[5];
    double price = BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volatility,
        priceDerivatives);
    price *= Math.abs(foreignAmount) * sign;
    final double deltaSpot = priceDerivatives[0];
    final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
    // Implementation note: foreign currency (currency 1) exposure = Delta_spot * amount1.
    currencyExposure[0] = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency1(), deltaSpot * Math.abs(foreignAmount) * sign);
    // Implementation note: domestic currency (currency 2) exposure = -Delta_spot * amount1 * spot+PV
    currencyExposure[1] = CurrencyAmount.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(),
        -deltaSpot * Math.abs(optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount()) * spot * sign + price);
    return MultipleCurrencyAmount.of(currencyExposure);
  }

  /**
   * Computes the curve sensitivity of the option present value. The sensitivity of the volatility on the forward (and on the curves) is not taken into account. It is the curve
   * sensitivity in the Black model where the volatility is suppose to be constant for curve and forward changes.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    // Forward sweep
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex
        .getUnderlyingOption().getStrike(), forward);
    // The Barrier pricing method parameterizes as a function of rate (r=rateDomestic), and costOfCarry (b=rateDomestic-rateForeign)
    // We wish to compute derivatives wrt rateDomestic and rateForeign, not the costOfCarry parameter.
    final double[] priceDerivatives = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volatility, priceDerivatives);
    // Backward sweep
    final double priceBar = 1.0;
    final double rCostOfCarryBar = priceDerivatives[3] * Math.abs(foreignAmount) * sign * priceBar;
    final double rDomesticBar = (priceDerivatives[2] + priceDerivatives[3]) * Math.abs(foreignAmount) * sign * priceBar;
    final double rForeignBar = -1 * rCostOfCarryBar;
    // Sensitivity object
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listForeign = new ArrayList<>();
    listForeign.add(DoublesPair.of(payTime, rForeignBar));
    resultMap.put(multicurves.getName(optionForex.getCurrency1()), listForeign);
    final List<DoublesPair> listDomestic = new ArrayList<>();
    listDomestic.add(DoublesPair.of(payTime, rDomesticBar));
    resultMap.put(multicurves.getName(optionForex.getCurrency2()), listDomestic);
    final MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    return MultipleCurrencyMulticurveSensitivity.of(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(), result);
  }

  /**
   * Computes the volatility sensitivity of the option present value.
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The curve sensitivity.
   */
  public PresentValueForexBlackVolatilitySensitivity presentValueBlackVolatilitySensitivity(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double volatility = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex
        .getUnderlyingOption().getStrike(), forward);
    final double[] priceDerivatives = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volatility, priceDerivatives);
    final double volatilitySensitivityValue = priceDerivatives[4] * Math.abs(foreignAmount) * sign;
    final DoublesPair point = DoublesPair.of(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption().getStrike());
    //    Map<DoublesPair, Double> result = new HashMap<DoublesPair, Double>();
    //    result.put(point, volatilitySensitivityValue);
    final SurfaceValue result = SurfaceValue.from(point, volatilitySensitivityValue);
    final PresentValueForexBlackVolatilitySensitivity sensi = new PresentValueForexBlackVolatilitySensitivity(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency1(), optionForex
        .getUnderlyingOption().getUnderlyingForex().getCurrency2(), result);
    return sensi;
  }

  /**
   * Computes the volatility sensitivity with respect to input data for a vanilla option with the Black function and a volatility from a volatility surface. The sensitivity
   * is computed with respect to each node in the volatility surface.
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The volatility node sensitivity. The sensitivity figures are, like the present value, in the domestic currency (currency 2).
   */
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle presentValueBlackVolatilityNodeSensitivity(final ForexOptionSingleBarrier optionForex,
      final BlackForexSmileProviderInterface smileMulticurves) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    final PresentValueForexBlackVolatilitySensitivity pointSensitivity = presentValueBlackVolatilitySensitivity(optionForex, smileMulticurves);
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityModel = smileMulticurves.getVolatility();
    final double df = multicurves.getDiscountFactor(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2(), optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime());
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot
        * multicurves.getDiscountFactor(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime()) / df;
    final VolatilityAndBucketedSensitivities volAndSensitivities = volatilityModel.getVolatilityAndSensitivities(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    final double[][] nodeWeight = volAndSensitivities.getBucketedSensitivities();
    final DoublesPair point = DoublesPair.of(optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption().getStrike());
    final double[][] vega = new double[volatilityModel.getNumberExpiration()][volatilityModel.getNumberStrike()];
    for (int loopexp = 0; loopexp < volatilityModel.getNumberExpiration(); loopexp++) {
      for (int loopstrike = 0; loopstrike < volatilityModel.getNumberStrike(); loopstrike++) {
        vega[loopexp][loopstrike] = nodeWeight[loopexp][loopstrike] * pointSensitivity.getVega().getMap().get(point);
      }
    }
    return new PresentValueForexBlackVolatilityNodeSensitivityDataBundle(optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency1(), optionForex.getUnderlyingOption().getUnderlyingForex()
        .getCurrency2(), new DoubleMatrix1D(volatilityModel.getTimeToExpiration()), new DoubleMatrix1D(volatilityModel.getDeltaFull()), new DoubleMatrix2D(vega));
  }

  /**
   * Computes the 2nd order spot fx sensitivity of the option present value by centered finite difference <p>
   * This gamma is be computed with respect to the direct quote (1 foreign = x domestic)
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Gamma
   */
  public CurrencyAmount gammaFd(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double vol = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    // Bump and compute vega
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotUp, rateDomestic - rateForeign, rateDomestic, vol, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotDown, rateDomestic - rateForeign, rateDomestic, vol, adjointDown);
    final double deltaUp = adjointUp[0] * Math.abs(foreignAmount) * sign;
    final double deltaDown = adjointDown[0] * Math.abs(foreignAmount) * sign;

    final double gamma = (deltaUp - deltaDown) / (2 * relShift * spot);
    final Currency ccy = optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, gamma);

  }

  /**
   * Computes the 2nd order spot fx sensitivity of the option present value by centered finite difference and a relative shift of 10 basis points
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return Gamma
   */
  public CurrencyAmount gammaFd(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return gammaFd(optionForex, smileMulticurves, DEFAULT_GAMMA_SHIFT);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue (point, value)
   */
  public CurrencyAmount vommaFd(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double vol = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    // Bump and compute vega
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volUp, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volDown, adjointDown);
    final double vegaUp = adjointUp[4] * Math.abs(foreignAmount) * sign;
    final double vegaDown = adjointDown[4] * Math.abs(foreignAmount) * sign;

    final double vomma = (vegaUp - vegaDown) / (2 * relShift * vol);
    final Currency ccy = optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vomma);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference and a default relative shift of 1 basis point
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount vommaFd(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return vommaFd(optionForex, smileMulticurves, DEFAULT_VOMMA_SHIFT);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the price
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount vannaFd(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    return d2PriceDSpotDVolFD(optionForex, smileMulticurves, relShift);
  }

  /**
   * Computes the 2nd order volatility sensitivity of the option present value by centered finite difference and a relative shift of 10 basis points
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount vannaFd(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return vannaFd(optionForex, smileMulticurves, DEFAULT_VANNA_SHIFT);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the vega
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount dVegaDSpotFD(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double vol = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    // Bump *spot* and compute vega
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotUp, rateDomestic - rateForeign, rateDomestic, vol, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotDown, rateDomestic - rateForeign, rateDomestic, vol, adjointDown);
    final double vegaUp = adjointUp[4] * Math.abs(foreignAmount) * sign;
    final double vegaDown = adjointDown[4] * Math.abs(foreignAmount) * sign;

    final double vanna = (vegaUp - vegaDown) / (2 * relShift * vol);
    final Currency ccy = optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the delta
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount dDeltaDVolFD(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double vol = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    // Bump *vol* and compute delta
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double[] adjointUp = new double[5];
    final double[] adjointDown = new double[5];
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volUp, adjointUp);
    BARRIER_FUNCTION.getPriceAdjoint(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, rateDomestic - rateForeign, rateDomestic, volDown, adjointDown);
    final double deltaUp = adjointUp[0] * Math.abs(foreignAmount) * sign;
    final double deltaDown = adjointDown[0] * Math.abs(foreignAmount) * sign;

    final double vanna = (deltaUp - deltaDown) / (2 * relShift * spot);
    final Currency ccy = optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the price
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount d2PriceDSpotDVolFD(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double costOfCarry = rateDomestic - rateForeign;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double vol = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    // Bump spot *and* vol and compute *price*
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;
    final double pxUpUp = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, volUp);
    final double pxDownDown = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, volDown);
    final double pxUpDown = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, volDown);
    final double pxDownUp = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, volUp);

    final double vanna = (pxUpUp - pxUpDown - pxDownUp + pxDownDown) / (2 * relShift * spot) / (2 * relShift * vol) * Math.abs(foreignAmount) * sign;
    final Currency ccy = optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }

  /**
   * Computes the 2nd order cross sensitivity (to spot and vol) by centered finite difference of the price
   * @param optionForex A single barrier Forex option.
   * @param smileMulticurves The curve and smile data.
   * @param relShift The shift to the black volatility expressed relative to the input vol level
   * @return Vomma as a SurfaceValue
   */
  public CurrencyAmount d2PriceDSpotDVolFdAlt(final ForexOptionSingleBarrier optionForex, final BlackForexSmileProviderInterface smileMulticurves, final double relShift) {
    ArgumentChecker.notNull(optionForex, "Forex option");
    ArgumentChecker.notNull(smileMulticurves, "Smile");
    ArgumentChecker.isTrue(smileMulticurves.checkCurrencies(optionForex.getCurrency1(), optionForex.getCurrency2()), "Option currencies not compatible with smile data");
    final MulticurveProviderInterface multicurves = smileMulticurves.getMulticurveProvider();
    // repackage for calls to BARRIER_FUNCTION
    final double payTime = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentTime();
    final double dfDomestic = multicurves.getDiscountFactor(optionForex.getCurrency2(), payTime);
    final double dfForeign = multicurves.getDiscountFactor(optionForex.getCurrency1(), payTime);
    final double rateDomestic = -Math.log(dfDomestic) / payTime;
    final double rateForeign = -Math.log(dfForeign) / payTime;
    final double costOfCarry = rateDomestic - rateForeign;
    final double spot = multicurves.getFxRate(optionForex.getCurrency1(), optionForex.getCurrency2());
    final double forward = spot * Math.exp(-rateForeign * payTime) / Math.exp(-rateDomestic * payTime);
    final double foreignAmount = optionForex.getUnderlyingOption().getUnderlyingForex().getPaymentCurrency1().getAmount();
    final double rebateByForeignUnit = optionForex.getRebate() / Math.abs(foreignAmount);
    final double sign = (optionForex.getUnderlyingOption().isLong() ? 1.0 : -1.0);
    final double vol = smileMulticurves.getVolatility(optionForex.getCurrency1(), optionForex.getCurrency2(), optionForex.getUnderlyingOption().getTimeToExpiry(), optionForex.getUnderlyingOption()
        .getStrike(), forward);
    // Bump spot *and* vol and compute *price*
    final double volUp = (1.0 + relShift) * vol;
    final double volDown = (1.0 - relShift) * vol;
    final double spotUp = (1.0 + relShift) * spot;
    final double spotDown = (1.0 - relShift) * spot;

    final double pxBase = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, costOfCarry, rateDomestic, vol);

    final double pxUpUp = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, volUp);
    final double pxDownDown = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, volDown);

    final double pxVolUp = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, costOfCarry, rateDomestic, volUp);
    final double pxVolDown = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spot, costOfCarry, rateDomestic, volDown);

    final double pxSpotUp = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotUp, costOfCarry, rateDomestic, vol);
    final double pxSpotDown = BARRIER_FUNCTION.getPrice(optionForex.getUnderlyingOption(), optionForex.getBarrier(), rebateByForeignUnit, spotDown, costOfCarry, rateDomestic, vol);

    final double vanna = (pxUpUp - pxVolUp - pxSpotUp + 2 * pxBase + pxDownDown - pxVolDown - pxSpotDown) / (2 * relShift * spot * relShift * vol) * Math.abs(foreignAmount) * sign;
    final Currency ccy = optionForex.getUnderlyingOption().getUnderlyingForex().getCurrency2();
    return CurrencyAmount.of(ccy, vanna);
  }
}
