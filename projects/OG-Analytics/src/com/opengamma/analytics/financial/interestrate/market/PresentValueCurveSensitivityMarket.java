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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a present value curve sensitivity.
 */
public class PresentValueCurveSensitivityMarket {

  /**
   * The map containing the sensitivity to the yield (continuously compounded) (for discounting and issuer specific curves). 
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityYieldDiscounting;
  /**
   * The map containing the sensitivity to forward curve. The sensitivity will depend on the way the curve is described (discount factor curve, forward rate, ...) 
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityForward;
  /**
   * The map containing the sensitivity to the price index. 
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityPriceCurve;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public PresentValueCurveSensitivityMarket() {
    _sensitivityYieldDiscounting = new HashMap<String, List<DoublesPair>>();
    _sensitivityForward = new HashMap<String, List<DoublesPair>>();
    _sensitivityPriceCurve = new HashMap<String, List<DoublesPair>>();
  }

  /**
   * Constructor from a yield discounting map, a forward map and a price index curve of sensitivity. The map are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @param sensitivityPriceCurve The map.
   */
  public PresentValueCurveSensitivityMarket(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<DoublesPair>> sensitivityForward,
      Map<String, List<DoublesPair>> sensitivityPriceCurve) {
    Validate.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    Validate.notNull(sensitivityForward, "Sensitivity forward");
    Validate.notNull(sensitivityPriceCurve, "Sensitivity price index curve");
    _sensitivityYieldDiscounting = sensitivityYieldDiscounting;
    _sensitivityForward = sensitivityForward;
    _sensitivityPriceCurve = sensitivityPriceCurve;
  }

  /**
   * Constructor from a yield discounting map of sensitivity.
   * @param sensitivityYieldDiscounting The map.
   * @return The sensitivity.
   */
  public static PresentValueCurveSensitivityMarket fromYieldDiscounting(Map<String, List<DoublesPair>> sensitivityYieldDiscounting) {
    return new PresentValueCurveSensitivityMarket(sensitivityYieldDiscounting, new HashMap<String, List<DoublesPair>>(), new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a forward map.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static PresentValueCurveSensitivityMarket fromYieldDiscountingAndForward(Map<String, List<DoublesPair>> sensitivityYieldDiscounting, Map<String, List<DoublesPair>> sensitivityForward) {
    return new PresentValueCurveSensitivityMarket(sensitivityYieldDiscounting, sensitivityForward, new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a price map.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityPriceCurve The map.
   * @return The sensitivity.
   */
  public static PresentValueCurveSensitivityMarket fromYieldDiscountingAndPrice(Map<String, List<DoublesPair>> sensitivityYieldDiscounting, Map<String, List<DoublesPair>> sensitivityPriceCurve) {
    return new PresentValueCurveSensitivityMarket(sensitivityYieldDiscounting, new HashMap<String, List<DoublesPair>>(), sensitivityPriceCurve);
  }

  /**
   * Gets the discounting curve sensitivities.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getYieldDiscountingSensitivities() {
    return _sensitivityYieldDiscounting;
  }

  /**
   * Gets the forward curve sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getForwardSensitivities() {
    return _sensitivityForward;
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
  public PresentValueCurveSensitivityMarket plus(PresentValueCurveSensitivityMarket other) {
    ArgumentChecker.notNull(other, "sensitivity");
    Map<String, List<DoublesPair>> resultDsc = plus(_sensitivityYieldDiscounting, other._sensitivityYieldDiscounting);
    Map<String, List<DoublesPair>> resultFwd = plus(_sensitivityForward, other._sensitivityForward);
    Map<String, List<DoublesPair>> resultPrice = plus(_sensitivityPriceCurve, other._sensitivityPriceCurve);
    return new PresentValueCurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  private static Map<String, List<DoublesPair>> plus(Map<String, List<DoublesPair>> map1, Map<String, List<DoublesPair>> map2) {
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : map1.keySet()) {
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : map1.get(name)) {
        temp.add(pair);
      }
      if (map2.containsKey(name)) {
        for (final DoublesPair pair : map2.get(name)) {
          temp.add(pair);
        }
      }
      result.put(name, temp);
    }
    for (final String name : map2.keySet()) {
      if (!result.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : map2.get(name)) {
          temp.add(pair);
        }
        result.put(name, temp);
      }
    }
    return result;
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public PresentValueCurveSensitivityMarket multiply(double factor) {
    Map<String, List<DoublesPair>> resultDsc = multiply(_sensitivityYieldDiscounting, factor);
    Map<String, List<DoublesPair>> resultFwd = multiply(_sensitivityForward, factor);
    Map<String, List<DoublesPair>> resultPrice = multiply(_sensitivityPriceCurve, factor);
    return new PresentValueCurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  private static Map<String, List<DoublesPair>> multiply(Map<String, List<DoublesPair>> map, double factor) {
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : map.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : map.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      result.put(name, curveSensi);
    }
    return result;
  }

  /**
   * Return a new sensitivity by sorting the times and adding the values at duplicated times.
   * @return The cleaned sensitivity.
   */
  public PresentValueCurveSensitivityMarket cleaned() {
    Map<String, List<DoublesPair>> resultDsc = cleaned(_sensitivityYieldDiscounting);
    Map<String, List<DoublesPair>> resultFwd = cleaned(_sensitivityForward);
    Map<String, List<DoublesPair>> resultPrice = cleaned(_sensitivityPriceCurve);
    return new PresentValueCurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  private static Map<String, List<DoublesPair>> cleaned(Map<String, List<DoublesPair>> map) {
    //TODO: improve the sorting algorithm.
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : map.keySet()) {
      List<DoublesPair> list = map.get(name);
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
      result.put(name, listClean);
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivityForward.hashCode();
    result = prime * result + _sensitivityPriceCurve.hashCode();
    result = prime * result + _sensitivityYieldDiscounting.hashCode();
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
    if (!ObjectUtils.equals(_sensitivityForward, other._sensitivityForward)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivityPriceCurve, other._sensitivityPriceCurve)) {
      return false;
    }
    if (!ObjectUtils.equals(_sensitivityYieldDiscounting, other._sensitivityYieldDiscounting)) {
      return false;
    }
    return true;
  }

}
