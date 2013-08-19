/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Returns the change in present value of an instrument due to a parallel move of the yield curve, scaled so that the move is 1bp.
 * This calculator is Forex instrument-specific, and assumes that the list of sensitivities are in the currency appropriate for
 * the yield curve.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public final class PV01ForexCalculator extends InstrumentDerivativeVisitorAdapter<Map<String, List<DoublesPair>>, Map<String, Double>> {
  private static final PV01ForexCalculator INSTANCE = new PV01ForexCalculator();
  private static final double ONE_BASIS_POINT = 1.0E-4;

  public static PV01ForexCalculator getInstance() {
    return INSTANCE;
  }

  private PV01ForexCalculator() {
  }

  @Override
  public Map<String, Double> visitForex(final Forex forex, final Map<String, List<DoublesPair>> curveSensitivities) {
    return visit(curveSensitivities);
  }

  @Override
  public Map<String, Double> visitForexOptionDigital(final ForexOptionDigital option, final Map<String, List<DoublesPair>> curveSensitivities) {
    return visit(curveSensitivities);
  }

  @Override
  public Map<String, Double> visitForexOptionVanilla(final ForexOptionVanilla option, final Map<String, List<DoublesPair>> curveSensitivities) {
    return visit(curveSensitivities);
  }

  @Override
  public Map<String, Double> visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final Map<String, List<DoublesPair>> curveSensitivities) {
    return visit(curveSensitivities);
  }

  private Map<String, Double> visit(final Map<String, List<DoublesPair>> curveSensitivities) {
    ArgumentChecker.notNull(curveSensitivities, "curve sensitivities");
    final Map<String, Double> result = Maps.newHashMapWithExpectedSize(curveSensitivities.size());
    for (final Map.Entry<String, List<DoublesPair>> entry : curveSensitivities.entrySet()) {
      final String name = entry.getKey();
      final double sum = sumListPair(entry.getValue()) * ONE_BASIS_POINT;
      result.put(name, sum);
    }
    return result;
  }

  private double sumListPair(final List<DoublesPair> list) {
    double sum = 0.0;
    for (final DoublesPair pair : list) {
      sum += pair.getSecond();
    }
    return sum;
  }
}
