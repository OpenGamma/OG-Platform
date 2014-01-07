/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the z-spread of an annuity to a curve. The z-spread is defined as
 * the flat spread over the curve that is required to make the present value
 * equal to the price.
 *
 * @param <T> The type of {@link ParameterProviderInterface} that is required
 * to calculate present value for the annuity.
 */
public class ZSpreadCalculator<T extends ParameterProviderInterface> {
  /** Brackets a root */
  private static final BracketRoot ROOT_BRACKETER = new BracketRoot();
  /** The root-finder */
  private static final RealSingleRootFinder ROOT_FINDER = new BrentSingleRootFinder();
  /** The present value calculator */
  private final InstrumentDerivativeVisitor<T, MultipleCurrencyAmount>  _pvCalculator;
  /** The present value sensitivity calculator */
  private final InstrumentDerivativeVisitor<T, MultipleCurrencyMulticurveSensitivity> _pvSensitivityCalculator;

  /**
   * @param pvCalculator The present value calculator, not null
   * @param pvSensitivityCalculator The present value sensitivity calculator, not null
   */
  public ZSpreadCalculator(final InstrumentDerivativeVisitor<T, MultipleCurrencyAmount> pvCalculator,
      final InstrumentDerivativeVisitor<T, MultipleCurrencyMulticurveSensitivity> pvSensitivityCalculator) {
    ArgumentChecker.notNull(pvCalculator, "present value calculator");
    ArgumentChecker.notNull(pvSensitivityCalculator, "curve sensitivity calculator");
    _pvCalculator = pvCalculator;
    _pvSensitivityCalculator = pvSensitivityCalculator;
  }

  /**
   * Calculates the z-spread of an annuity given curves and a price.
   * @param annuity The annuity, not null
   * @param curves The curves, not null
   * @param price The price of the annuity
   * @return The z-spread
   */
  public double calculateZSpread(final Annuity<? extends Payment> annuity, final T curves, final double price) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(curves, "curves");

    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return calculatePriceForZSpread(annuity, curves, y) - price;
      }
    };

    final double[] range = ROOT_BRACKETER.getBracketedPoints(f, 0.0, 1.2);
    return ROOT_FINDER.getRoot(f, range[0], range[1]);
  }

  /**
   * Calculates the price of an annuity given curves and a z-spread.
   * @param annuity The annuity, not null
   * @param curves The curves, not null
   * @param zSpread The z-spread of the annuity
   * @return The price of the annuity
   */
  public double calculatePriceForZSpread(final Annuity<? extends Payment> annuity, final T curves, final double zSpread) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(curves, "curves");

    double sum = 0;
    final int n = annuity.getNumberOfPayments();
    Payment payment;
    for (int i = 0; i < n; i++) {
      payment = annuity.getNthPayment(i);
      final MultipleCurrencyAmount pvs = payment.accept(_pvCalculator, curves);
      if (pvs.size() != 1) {
        throw new IllegalStateException("Had more than one currency in result: " + pvs.getCurrencyAmounts());
      }
      final double pv = Iterables.getOnlyElement(pvs).getAmount();
      sum += pv * Math.exp(-zSpread * payment.getPaymentTime());
    }
    return sum;
  }

  /**
   * Calculates the sensitivity of the price of an annuity to the z-spread.
   * @param annuity The annuity, not null
   * @param curves The curves, not null
   * @param zSpread The z-spread
   * @return The sensitivity of the price to the z-spread
   */
  public double calculatePriceSensitivityToZSpread(final Annuity<? extends Payment> annuity, final T curves, final double zSpread) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(curves, "curves");

    double sum = 0;

    final int n = annuity.getNumberOfPayments();
    Payment payment;
    for (int i = 0; i < n; i++) {
      payment = annuity.getNthPayment(i);
      final MultipleCurrencyAmount pvs = payment.accept(_pvCalculator, curves);
      if (pvs.size() != 1) {
        throw new IllegalStateException("Had more than one currency in result: " + pvs.getCurrencyAmounts());
      }
      final double pv = Iterables.getOnlyElement(pvs).getAmount();
      final double time = payment.getPaymentTime();
      sum -= time * pv * Math.exp(-zSpread * time);
    }
    return sum;
  }

  /**
   * Calculates the sensitivity of the price of an annuity to the curve.
   * @param annuity The annuity, not null
   * @param curves The curves, not null
   * @param zSpread The z-spread
   * @return The sensitivity of the price to the z-spread
   */
  public Map<String, List<DoublesPair>> calculatePriceSensitivityToCurve(final Annuity<? extends Payment> annuity, final T curves, final double zSpread) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(curves, "curves");

    final MultipleCurrencyMulticurveSensitivity pvss = annuity.accept(_pvSensitivityCalculator, curves);
    final Set<Currency> currencies = pvss.getCurrencies();
    if (currencies.size() != 1) {
      throw new IllegalStateException("Had more than one currency in result: " + pvss.getCurrencies());
    }
    final MulticurveSensitivity sensitivities = pvss.getSensitivity(Iterables.getOnlyElement(currencies));
    if (Double.doubleToLongBits(zSpread) == 0) {
      return sensitivities.getYieldDiscountingSensitivities();
    }
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivities.getYieldDiscountingSensitivities().entrySet()) {
      final List<DoublesPair> unadjusted = entry.getValue();
      final ArrayList<DoublesPair> adjusted = new ArrayList<>(unadjusted.size());
      for (final DoublesPair pair : unadjusted) {
        final DoublesPair newPair = DoublesPair.of(pair.first, pair.second * Math.exp(-zSpread * pair.first));
        adjusted.add(newPair);
      }
      result.put(entry.getKey(), adjusted);
    }
    return result;
  }

  /**
   * Calculates the sensitivity of the z-spread of an annuity to the curves.
   * @param annuity The annuity, not null
   * @param curves The curves, not null
   * @param zSpread The z-spread
   * @return The sensitivity of the price to the z-spread
   */
  public Map<String, List<DoublesPair>> calculateZSpreadSensitivityToCurve(final Annuity<? extends Payment> annuity, final T curves, final double zSpread) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(curves, "curves");

    final double dPricedZ = calculatePriceSensitivityToZSpread(annuity, curves, zSpread);
    ArgumentChecker.isTrue(Double.doubleToLongBits(dPricedZ) != 0, "Price Sensitivity To ZSpread is zero");

    final MultipleCurrencyMulticurveSensitivity pvss = annuity.accept(_pvSensitivityCalculator, curves);
    final Set<Currency> currencies = pvss.getCurrencies();
    if (currencies.size() != 1) {
      throw new IllegalStateException("Had more than one currency in result: " + pvss.getCurrencies());
    }
    final MulticurveSensitivity sensitivities = pvss.getSensitivity(Iterables.getOnlyElement(currencies));
    if (Double.doubleToLongBits(zSpread) == 0) {
      return sensitivities.getYieldDiscountingSensitivities();
    }
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivities.getYieldDiscountingSensitivities().entrySet()) {
      final List<DoublesPair> unadjusted = entry.getValue();
      final ArrayList<DoublesPair> adjusted = new ArrayList<>(unadjusted.size());
      for (final DoublesPair pair : unadjusted) {
        final DoublesPair newPair = DoublesPair.of(pair.first, -pair.second * Math.exp(-zSpread * pair.first) / dPricedZ);
        adjusted.add(newPair);
      }
      result.put(entry.getKey(), adjusted);
    }
    return result;
  }
}
