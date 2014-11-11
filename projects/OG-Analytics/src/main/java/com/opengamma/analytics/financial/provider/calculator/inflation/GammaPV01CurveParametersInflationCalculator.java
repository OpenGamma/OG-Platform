/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurveUtils;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;


/**
 * Returns the change in PV01 of an instrument due to a parallel 1bp move of <b>all</b> the interest rate curves to 
 * which the instrument is sensitive. The inflation curves are unchanged.
 * @param <T> The type of the multi-curve provider
 */
public final class GammaPV01CurveParametersInflationCalculator<T extends ParameterInflationProviderInterface> 
  extends InstrumentDerivativeVisitorSameMethodAdapter<T, Double> {
  
  /** The size of the scaling: 1 basis point. */
  private static final double BP1 = 1.0E-4;
  /** The PV01 calculator. */
  private final InstrumentDerivativeVisitor<T, ReferenceAmount<Pair<String, Currency>>> _pv01Calculator;

  /**
   * Constructs a PV01 calculator that uses a particular sensitivity calculator.
   * @param curveSensitivityCalculator The curve sensitivity calculator, not null
   */
  public GammaPV01CurveParametersInflationCalculator(
      final InstrumentDerivativeVisitor<T, MultipleCurrencyInflationSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "curve sensitivity calculator");
    _pv01Calculator = new PV01CurveParametersInflationCalculator<>(curveSensitivityCalculator);
  }

  /**
   * Calculates the change in PV01 of an instrument due to a parallel move of each interest rate curve the instrument 
   * is sensitive to, scaled so that the move is 1bp. There is no inflation impact.
   * The provider should be of the type InflationProviderDiscount or InflationIssuerProviderDiscount 
   * and each curve should be a YieldCurve.
   * @param ird The instrument, not null
   * @param inflation The multi-curves provider, not null
   * @return The scaled sensitivity for each curve/currency.
   */
  @Override
  public Double visit(final InstrumentDerivative ird, final T inflation) {
    ArgumentChecker.notNull(ird, "derivative");
    ArgumentChecker.notNull(inflation, "multicurves");
    // Get the names of inflation curves
    Set<IndexPrice> priceIndexes = inflation.getInflationProvider().getPriceIndexes();
    Set<String> inflationCurvesNames = new HashSet<>();
    for (IndexPrice index : priceIndexes) {
      inflationCurvesNames.add(inflation.getInflationProvider().getName(index));
    }
    final T bumped = getBumpedProvider(inflation);
    final ReferenceAmount<Pair<String, Currency>> pv01 = ird.accept(_pv01Calculator, inflation);
    final ReferenceAmount<Pair<String, Currency>> up = ird.accept(_pv01Calculator, bumped);
    double gammaPV01 = 0;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01.getMap().entrySet()) {
      if (!inflationCurvesNames.contains(entry.getKey().getFirst())) { // Only non inflation curves impacts
        final Pair<String, Currency> bumpedNameCurrency =
            Pairs.of(entry.getKey().getFirst() + YieldCurveUtils.PARALLEL_SHIFT_NAME, entry.getKey().getSecond());
        if (!(up.getMap().containsKey(bumpedNameCurrency))) {
          throw new IllegalStateException("Have bumped PV01 for curve / currency pair " + entry.getKey() + " but no PV01");
        }
        gammaPV01 += (up.getMap().get(bumpedNameCurrency) - entry.getValue()) / BP1;
      }
    }
    return gammaPV01;
  }

  @Override
  public Double visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curve data");
  }

  /**
   * Bumps every curve in a provider. This method will be replaced when the curve providers
   * have been refactored.
   * @param inflation The curves
   * @return A provider with each curve bumped by +1 bp
   */
  @SuppressWarnings("unchecked")
  private T getBumpedProvider(final T inflation) {
    if (inflation instanceof InflationProviderDiscount) {
      final MulticurveProviderDiscount discount = ((MulticurveProviderDiscount) inflation.getMulticurveProvider());
      final MulticurveProviderDiscount bumped = new MulticurveProviderDiscount(discount.getFxRates());
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : discount.getDiscountingCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : discount.getForwardIborCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : discount.getForwardONCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumped.setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      InflationProviderDiscount bumpedInflation = new InflationProviderDiscount(bumped, ((InflationProviderDiscount) inflation).getPriceIndexCurves());
      return (T) bumpedInflation;
    }
    if (inflation instanceof InflationIssuerProviderDiscount) {
      InflationIssuerProviderDiscount inflationIssuer = (InflationIssuerProviderDiscount) inflation;
      final IssuerProviderDiscount discount = inflationIssuer.getIssuerProvider();
      final MulticurveProviderDiscount multicurveProvider = discount.getMulticurveProvider();
      final IssuerProviderDiscount bumpedIssuer = new IssuerProviderDiscount(new MulticurveProviderDiscount(multicurveProvider.getFxRates()));
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : multicurveProvider.getDiscountingCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumpedIssuer.getMulticurveProvider().setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : multicurveProvider.getForwardIborCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumpedIssuer.getMulticurveProvider().setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : multicurveProvider.getForwardONCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumpedIssuer.getMulticurveProvider().setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : discount.getIssuerCurves().entrySet()) {
        if (!(entry.getValue() instanceof YieldCurve)) {
          throw new IllegalArgumentException("Can only bump YieldCurves");
        }
        bumpedIssuer.setCurve(entry.getKey(), YieldCurveUtils.withParallelShift((YieldCurve) entry.getValue(), BP1, ShiftType.ABSOLUTE));
      }
      InflationProviderDiscount bumpedInflation = new InflationProviderDiscount(bumpedIssuer.getMulticurveProvider(), 
          inflationIssuer.getInflationProvider().getPriceIndexCurves());
      InflationIssuerProviderDiscount bumpedTotal = new InflationIssuerProviderDiscount(bumpedInflation, bumpedIssuer);
      return (T) bumpedTotal;
    }
    throw new UnsupportedOperationException("Cannot bump curves of type " + inflation.getClass());
  }
}
