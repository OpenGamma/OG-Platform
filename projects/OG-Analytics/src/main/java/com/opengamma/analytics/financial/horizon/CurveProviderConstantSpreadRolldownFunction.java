/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Produces a {@link MulticurveProviderDiscount} or {@link IssuerProvider} where all yield curves have
 * been shifted forward in time without slide i.e. the curves are shifted in such a way that the rate
 * or discount factor requested for the same maturity date will be the same as for the original curves.
 * <p>
 * This class does not handle all types of {@link ParameterProviderInterface} because the type
 * hierarchy has not been refactored for easier use.
 */
public final class CurveProviderConstantSpreadRolldownFunction implements RolldownFunction<ParameterProviderInterface> {
  
  /** Rolls down a yield curve without slide */
  private static final ConstantSpreadYieldCurveRolldownFunction CURVE_ROLLDOWN_YC = 
      ConstantSpreadYieldCurveRolldownFunction.getInstance();
  /** Rolls down a price index by time shift of the price index estimation. */
  private static final ConstantTimeShiftPriceIndexCurveRolldownFunction CURVE_ROLLDOWN_PI = 
      ConstantTimeShiftPriceIndexCurveRolldownFunction.getInstance();
  /** The singleton instance */
  private static final CurveProviderConstantSpreadRolldownFunction INSTANCE = new CurveProviderConstantSpreadRolldownFunction();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static CurveProviderConstantSpreadRolldownFunction getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CurveProviderConstantSpreadRolldownFunction() {
  }

  @Override
  public ParameterProviderInterface rollDown(final ParameterProviderInterface data, final double time) {
    ArgumentChecker.notNull(data, "data");
    if (data instanceof MulticurveProviderDiscount) {
      return rollDown((MulticurveProviderDiscount) data, time);
    }
    if (data instanceof IssuerProviderDiscount) {
      return rollDown((IssuerProviderDiscount) data, time);
    }
    if (data instanceof InflationProviderDiscount) {
      return rollDown((InflationProviderDiscount) data, time);
    }
    if (data instanceof InflationIssuerProviderDiscount) {
      return rollDown((InflationIssuerProviderDiscount) data, time);
    }
    throw new IllegalArgumentException("Cannot handle data of type " + data.getClass());
  }

  /**
   * Rolls down all of the curves in a {@link MulticurveProviderDiscount}
   * @param data The data
   * @param time The time in years
   * @return A provider with all of the curves shifted by the time
   */
  private MulticurveProviderDiscount rollDown(final MulticurveProviderDiscount data, final double time) {
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : data.getDiscountingCurves().entrySet()) {
      discountingCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : data.getForwardIborCurves().entrySet()) {
      forwardIborCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : data.getForwardONCurves().entrySet()) {
      forwardONCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, data.getFxRates());
  }

  /**
   * Rolls down all of the curves in an {@link IssuerProviderDiscount}
   * @param data The data
   * @param time The time in years
   * @return A provider with all of the curves shifted by the time
   */
  private IssuerProviderDiscount rollDown(final IssuerProviderDiscount data, final double time) {
    final MulticurveProviderDiscount underlying = data.getMulticurveProvider();
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : underlying.getDiscountingCurves().entrySet()) {
      discountingCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : underlying.getForwardIborCurves().entrySet()) {
      forwardIborCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : underlying.getForwardONCurves().entrySet()) {
      forwardONCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<>();
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : data.getIssuerCurves().entrySet()) {
      issuerCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), time));
    }
    return new IssuerProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, issuerCurves, underlying.getFxRates());
  }


  /**
   * Rolls down all of the curves in a {@link InflationProviderDiscount}
   * @param data The data.
   * @param timeShift The time shift in years.
   * @return A provider with all of the curves shifted by the time
   */
  private InflationProviderDiscount rollDown(final InflationProviderDiscount data, final double timeShift) {
    ArgumentChecker.notNull(data, "inflation provider");
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : 
      data.getMulticurveProvider().getDiscountingCurves().entrySet()) {
      discountingCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : 
      data.getMulticurveProvider().getForwardIborCurves().entrySet()) {
      forwardIborCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : 
      data.getMulticurveProvider().getForwardONCurves().entrySet()) {
      forwardONCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    final Map<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexPrice, PriceIndexCurve> entry : 
      data.getPriceIndexCurves().entrySet()) {
      priceIndexCurves.put(entry.getKey(), CURVE_ROLLDOWN_PI.rollDown(entry.getValue(), timeShift));
    }
    return new InflationProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, priceIndexCurves, 
        data.getFxRates());
  }
  
  /**
   * Rolls down all of the curves in a {@link InflationIssuerProviderDiscount}
   * @param data The data.
   * @param timeShift The time shift in years.
   * @return A provider with all of the curves shifted by the time
   */
  private InflationIssuerProviderDiscount rollDown(final InflationIssuerProviderDiscount data, final double timeShift) {
    ArgumentChecker.notNull(data, "inflation provider");
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : 
      data.getMulticurveProvider().getDiscountingCurves().entrySet()) {
      discountingCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : 
      data.getMulticurveProvider().getForwardIborCurves().entrySet()) {
      forwardIborCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : 
      data.getMulticurveProvider().getForwardONCurves().entrySet()) {
      forwardONCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    final Map<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexPrice, PriceIndexCurve> entry : 
      data.getInflationProvider().getPriceIndexCurves().entrySet()) {
      priceIndexCurves.put(entry.getKey(), CURVE_ROLLDOWN_PI.rollDown(entry.getValue(), timeShift));
    }
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<>();
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : 
      data.getIssuerProvider().getIssuerCurves().entrySet()) {
      issuerCurves.put(entry.getKey(), CURVE_ROLLDOWN_YC.rollDown(entry.getValue(), timeShift));
    }
    return new InflationIssuerProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, priceIndexCurves, 
        issuerCurves, data.getFxRates());
  }
  
}
