/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the change in par rate of an instrument (the exact meaning of par rate depends on the instrument - for swaps it is the par swap rate) due to a parallel move of each yield curve
 * that the instrument is sensitive to - dPar/dR where dR is a movement of the whole curve. The return format is a Map with curve names (String) as keys and a sensitivities as values. 
 */
public final class ParRateParallelSensitivityCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, Double>> {
  private static final ParRateParallelSensitivityCalculator s_instance = new ParRateParallelSensitivityCalculator();
  private final ParRateCurveSensitivityCalculator _prcsc = ParRateCurveSensitivityCalculator.getInstance();

  public static ParRateParallelSensitivityCalculator getInstance() {
    return s_instance;
  }

  private ParRateParallelSensitivityCalculator() {
  }

  /**
   * Calculates the change in par rate of an instrument due to a parallel move of each yield curve the instrument is sensitive to
   * @param ird instrument 
   * @param curves bundle of relevant yield curves 
   * @return a Map between curve name and sensitivity for that curve 
   */
  @Override
  public Map<String, Double> visit(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> sensitivities = _prcsc.visit(ird, curves);
    final Map<String, Double> result = new HashMap<String, Double>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivities.entrySet()) {
      final String name = entry.getKey();
      final double temp = sumListPair(entry.getValue());
      result.put(name, temp);
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
