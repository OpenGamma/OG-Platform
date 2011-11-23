/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Returns the change in present value of an instrument due to a parallel move of the yield curve, scaled so that the move is 1bp.
 *  
 */
public final class PV01Calculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, Double>> {
  private static final PV01Calculator s_instance = new PV01Calculator();
  private final PresentValueCurveSensitivityCalculator _pvsc = PresentValueCurveSensitivityCalculator.getInstance();

  public static PV01Calculator getInstance() {
    return s_instance;
  }

  private PV01Calculator() {
  }

  /**
   * Calculates the change in present value of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param ird instrument 
   * @param curves bundle of relevant yield curves 
   * @return a Map between curve name and PV01 for that curve 
   */
  @Override
  public Map<String, Double> visit(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> sense = _pvsc.visit(ird, curves);
    final Map<String, Double> res = new HashMap<String, Double>();
    final Iterator<Entry<String, List<DoublesPair>>> iterator = sense.entrySet().iterator();
    while (iterator.hasNext()) {
      final Entry<String, List<DoublesPair>> entry = iterator.next();
      final String name = entry.getKey();
      final double pv01 = sumListPair(entry.getValue()) / 10000.;
      res.put(name, pv01);
    }

    return res;

  }

  private double sumListPair(final List<DoublesPair> list) {
    double sum = 0.0;
    for (final DoublesPair pair : list) {
      sum += pair.getSecond();
    }
    return sum;
  }

}
