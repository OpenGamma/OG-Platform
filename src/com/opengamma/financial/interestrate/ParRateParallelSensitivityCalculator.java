/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ParRateParallelSensitivityCalculator {

  private final ParRateCurveSensitivityCalculator _prcsc = ParRateCurveSensitivityCalculator.getInstance();

  /**
   * Calculates the change in par rate of an instrument due to a parallel move of each yield curve the instrument is sensitive to
   * @param ird instrument 
   * @param curves bundle of relevant yield curves 
   * @return a Map between curve name and sensitivity for that curve 
   */
  public Map<String, Double> getValue(InterestRateDerivative ird, YieldCurveBundle curves) {

    Map<String, List<Pair<Double, Double>>> sense = _prcsc.getValue(ird, curves);
    Map<String, Double> res = new HashMap<String, Double>();
    Iterator<Entry<String, List<Pair<Double, Double>>>> iterator = sense.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, List<Pair<Double, Double>>> entry = iterator.next();
      String name = entry.getKey();
      double temp = sumListPair(entry.getValue());
      res.put(name, temp);
    }
    return res;
  }

  private double sumListPair(List<Pair<Double, Double>> list) {
    double sum = 0.0;
    for (Pair<Double, Double> pair : list) {
      sum += pair.getSecond();
    }
    return sum;
  }

}
