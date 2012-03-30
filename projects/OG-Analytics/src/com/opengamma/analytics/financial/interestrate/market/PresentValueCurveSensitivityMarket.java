/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a present value curve sensitivity.
 */
public class PresentValueCurveSensitivityMarket {

  /**
   * The map containing the yield curve sensitivity. The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityYieldCurve;
  /**
   * The map containing the price index curve sensitivity. The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityPriceCurve;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public PresentValueCurveSensitivityMarket() {
    _sensitivityYieldCurve = new HashMap<String, List<DoublesPair>>();
    _sensitivityPriceCurve = new HashMap<String, List<DoublesPair>>();
  }

  /**
   * Constructor from a yield curve map of sensitivity.
   * @param sensitivityYieldCurve The map.
   */
  public PresentValueCurveSensitivityMarket(Map<String, List<DoublesPair>> sensitivityYieldCurve) {
    Validate.notNull(sensitivityYieldCurve, "sensitivity");
    _sensitivityYieldCurve = sensitivityYieldCurve;
    _sensitivityPriceCurve = new HashMap<String, List<DoublesPair>>();
  }

  /**
   * Constructor from a yield curve map and a price index curve of sensitivity.
   * @param sensitivityYieldCurve The map.
   * @param sensitivityPriceCurve The map.
   */
  public PresentValueCurveSensitivityMarket(Map<String, List<DoublesPair>> sensitivityYieldCurve, Map<String, List<DoublesPair>> sensitivityPriceCurve) {
    Validate.notNull(sensitivityYieldCurve, "Sensitivity yield curve");
    Validate.notNull(sensitivityPriceCurve, "Sensitivity price index curve");
    _sensitivityYieldCurve = sensitivityYieldCurve;
    _sensitivityPriceCurve = sensitivityPriceCurve;
  }

  /**
   * Gets the yield curve sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getYieldCurveSensitivities() {
    return _sensitivityYieldCurve;
  }

  /**
   * Gets the price index curve sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getPriceCurveSensitivities() {
    return _sensitivityPriceCurve;
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public PresentValueCurveSensitivityMarket add(PresentValueCurveSensitivityMarket other) {
    Validate.notNull(other, "sensitivity");
    final Map<String, List<DoublesPair>> resultYield = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivityYieldCurve.keySet()) {
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : _sensitivityYieldCurve.get(name)) {
        temp.add(pair);
      }
      if (other.getYieldCurveSensitivities().containsKey(name)) {
        for (final DoublesPair pair : other._sensitivityYieldCurve.get(name)) {
          temp.add(pair);
        }
      }
      resultYield.put(name, temp);
    }
    for (final String name : other.getYieldCurveSensitivities().keySet()) {
      if (!resultYield.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : other._sensitivityYieldCurve.get(name)) {
          temp.add(pair);
        }
        resultYield.put(name, temp);
      }
    }

    final Map<String, List<DoublesPair>> resultPrice = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivityPriceCurve.keySet()) {
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : _sensitivityPriceCurve.get(name)) {
        temp.add(pair);
      }
      if (other.getPriceCurveSensitivities().containsKey(name)) {
        for (final DoublesPair pair : other._sensitivityPriceCurve.get(name)) {
          temp.add(pair);
        }
      }
      resultPrice.put(name, temp);
    }
    for (final String name : other.getPriceCurveSensitivities().keySet()) {
      if (!resultPrice.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : other._sensitivityPriceCurve.get(name)) {
          temp.add(pair);
        }
        resultPrice.put(name, temp);
      }
    }
    return new PresentValueCurveSensitivityMarket(resultYield, resultPrice);
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public PresentValueCurveSensitivityMarket multiply(double factor) {
    Map<String, List<DoublesPair>> resultYield = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivityYieldCurve.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : _sensitivityYieldCurve.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      resultYield.put(name, curveSensi);
    }
    Map<String, List<DoublesPair>> resultPrice = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivityPriceCurve.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : _sensitivityPriceCurve.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      resultPrice.put(name, curveSensi);
    }
    return new PresentValueCurveSensitivityMarket(resultYield, resultPrice);
  }

  /**
   * Return a new sensitivity by sorting the times and adding the values at duplicated times.
   * @return The cleaned sensitivity.
   */
  public PresentValueCurveSensitivityMarket clean() {
    //TODO: improve the sorting algorithm.
    Map<String, List<DoublesPair>> resultYield = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivityYieldCurve.keySet()) {
      List<DoublesPair> list = _sensitivityYieldCurve.get(name);
      List<DoublesPair> listClean = new ArrayList<DoublesPair>();
      Set<Double> set = new TreeSet<Double>();
      for (final DoublesPair pair : list) {
        set.add(pair.getFirst());
      }
      for (Double time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (list.get(looplist).getFirst().doubleValue() == time.doubleValue()) {
            sensi += list.get(looplist).second;
          }
        }
        listClean.add(new DoublesPair(time, sensi));
      }
      resultYield.put(name, listClean);
    }
    Map<String, List<DoublesPair>> resultPrice = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivityPriceCurve.keySet()) {
      List<DoublesPair> list = _sensitivityPriceCurve.get(name);
      List<DoublesPair> listClean = new ArrayList<DoublesPair>();
      Set<Double> set = new TreeSet<Double>();
      for (final DoublesPair pair : list) {
        set.add(pair.getFirst());
      }
      for (Double time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (list.get(looplist).getFirst().doubleValue() == time.doubleValue()) {
            sensi += list.get(looplist).second;
          }
        }
        listClean.add(new DoublesPair(time, sensi));
      }
      resultPrice.put(name, listClean);
    }
    return new PresentValueCurveSensitivityMarket(resultYield, resultPrice);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivityPriceCurve.hashCode();
    result = prime * result + _sensitivityYieldCurve.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PresentValueCurveSensitivityMarket other = (PresentValueCurveSensitivityMarket) obj;
    if (!ObjectUtils.equals(_sensitivityPriceCurve, other._sensitivityPriceCurve)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivityYieldCurve, other._sensitivityYieldCurve)) {
      return false;
    }
    return true;
  }

}
