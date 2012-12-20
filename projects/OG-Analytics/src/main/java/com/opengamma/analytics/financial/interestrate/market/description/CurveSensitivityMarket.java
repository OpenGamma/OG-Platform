/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing a present value curve sensitivity.
 */
public class CurveSensitivityMarket {

  /**
   * The map containing the sensitivity to the yield (continuously compounded) (for discounting and issuer specific curves). 
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityYieldDiscounting;
  /**
   * The map containing the sensitivity to forward curve. The sensitivity will depend on the way the curve is described (discount factor curve, forward rate, ...) 
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<MarketForwardSensitivity>> _sensitivityForward;
  /**
   * The map containing the sensitivity to the price index. 
   * The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivityPriceCurve;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public CurveSensitivityMarket() {
    _sensitivityYieldDiscounting = new HashMap<String, List<DoublesPair>>();
    _sensitivityForward = new HashMap<String, List<MarketForwardSensitivity>>();
    _sensitivityPriceCurve = new HashMap<String, List<DoublesPair>>();
  }

  /**
   * Constructor from a yield discounting map, a forward map and a price index curve of sensitivity. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @param sensitivityPriceCurve The map.
   */
  private CurveSensitivityMarket(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<MarketForwardSensitivity>> sensitivityForward,
      final Map<String, List<DoublesPair>> sensitivityPriceCurve) {
    _sensitivityYieldDiscounting = sensitivityYieldDiscounting;
    _sensitivityForward = sensitivityForward;
    _sensitivityPriceCurve = sensitivityPriceCurve;
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @param sensitivityPriceCurve The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket of(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<MarketForwardSensitivity>> sensitivityForward,
      final Map<String, List<DoublesPair>> sensitivityPriceCurve) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    ArgumentChecker.notNull(sensitivityForward, "Sensitivity forward");
    ArgumentChecker.notNull(sensitivityPriceCurve, "Sensitivity price index curve");
    return new CurveSensitivityMarket(sensitivityYieldDiscounting, sensitivityForward, sensitivityPriceCurve);
  }

  /**
   * Constructor from a yield discounting map of sensitivity. The map is used directly.
   * @param sensitivityYieldDiscounting The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket ofYieldDiscounting(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    return new CurveSensitivityMarket(sensitivityYieldDiscounting, new HashMap<String, List<MarketForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a forward map. The map is used directly.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket ofForward(final Map<String, List<MarketForwardSensitivity>> sensitivityForward) {
    ArgumentChecker.notNull(sensitivityForward, "Sensitivity forward");
    return new CurveSensitivityMarket(new HashMap<String, List<DoublesPair>>(), sensitivityForward, new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a forward map. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket ofYieldDiscountingAndForward(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting,
      final Map<String, List<MarketForwardSensitivity>> sensitivityForward) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    ArgumentChecker.notNull(sensitivityForward, "Sensitivity forward");
    return new CurveSensitivityMarket(sensitivityYieldDiscounting, sensitivityForward, new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a price map. The maps are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityPriceCurve The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket ofYieldDiscountingAndPrice(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<DoublesPair>> sensitivityPriceCurve) {
    ArgumentChecker.notNull(sensitivityYieldDiscounting, "Sensitivity yield curve");
    ArgumentChecker.notNull(sensitivityPriceCurve, "Sensitivity price index curve");
    return new CurveSensitivityMarket(sensitivityYieldDiscounting, new HashMap<String, List<MarketForwardSensitivity>>(), sensitivityPriceCurve);
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
  public Map<String, List<MarketForwardSensitivity>> getForwardSensitivities() {
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
  public CurveSensitivityMarket plus(final CurveSensitivityMarket other) {
    ArgumentChecker.notNull(other, "sensitivity");
    final Map<String, List<DoublesPair>> resultDsc = plus(_sensitivityYieldDiscounting, other._sensitivityYieldDiscounting);
    final Map<String, List<MarketForwardSensitivity>> resultFwd = plusFwd(_sensitivityForward, other._sensitivityForward);
    final Map<String, List<DoublesPair>> resultPrice = plus(_sensitivityPriceCurve, other._sensitivityPriceCurve);
    return new CurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  /**
   * Add two maps together.
   * @param map1 The first map.
   * @param map2 The second map.
   * @return The sum.
   */
  private static Map<String, List<DoublesPair>> plus(final Map<String, List<DoublesPair>> map1, final Map<String, List<DoublesPair>> map2) {
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final Map.Entry<String, List<DoublesPair>> entry : map1.entrySet()) {
      final String name = entry.getKey();
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : entry.getValue()) {
        temp.add(pair);
      }
      if (map2.containsKey(name)) {
        for (final DoublesPair pair : map2.get(name)) {
          temp.add(pair);
        }
      }
      result.put(name, temp);
    }
    for (final Map.Entry<String, List<DoublesPair>> entry : map2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : entry.getValue()) {
          temp.add(pair);
        }
        result.put(name, temp);
      }
    }
    return result;
  }

  /**
   * Add two maps links to forward curves.
   * @param map1 The first map.
   * @param map2 The second map.
   * @return The sum.
   */
  private static Map<String, List<MarketForwardSensitivity>> plusFwd(final Map<String, List<MarketForwardSensitivity>> map1, final Map<String, List<MarketForwardSensitivity>> map2) {
    final Map<String, List<MarketForwardSensitivity>> result = new HashMap<String, List<MarketForwardSensitivity>>();
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : map1.entrySet()) {
      final List<MarketForwardSensitivity> temp = new ArrayList<MarketForwardSensitivity>();
      final String name = entry.getKey();
      for (final MarketForwardSensitivity pair : entry.getValue()) {
        temp.add(pair);
      }
      if (map2.containsKey(name)) {
        for (final MarketForwardSensitivity pair : map2.get(name)) {
          temp.add(pair);
        }
      }
      result.put(name, temp);
    }
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : map2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        final List<MarketForwardSensitivity> temp = new ArrayList<MarketForwardSensitivity>();
        for (final MarketForwardSensitivity pair : entry.getValue()) {
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
  public CurveSensitivityMarket multipliedBy(final double factor) {
    final Map<String, List<DoublesPair>> resultDsc = multipliedBy(_sensitivityYieldDiscounting, factor);
    final Map<String, List<MarketForwardSensitivity>> resultFwd = multipliedByFwd(_sensitivityForward, factor);
    final Map<String, List<DoublesPair>> resultPrice = multipliedBy(_sensitivityPriceCurve, factor);
    return new CurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  private static Map<String, List<DoublesPair>> multipliedBy(final Map<String, List<DoublesPair>> map, final double factor) {
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : map.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : map.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      result.put(name, curveSensi);
    }
    return result;
  }

  private static Map<String, List<MarketForwardSensitivity>> multipliedByFwd(final Map<String, List<MarketForwardSensitivity>> map, final double factor) {
    final Map<String, List<MarketForwardSensitivity>> result = new HashMap<String, List<MarketForwardSensitivity>>();
    for (final String name : map.keySet()) {
      final List<MarketForwardSensitivity> curveSensi = new ArrayList<MarketForwardSensitivity>();
      for (final MarketForwardSensitivity pair : map.get(name)) {
        curveSensi.add(new MarketForwardSensitivity(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor(), pair.getValue() * factor));
      }
      result.put(name, curveSensi);
    }
    return result;
  }

  /**
   * Return a new sensitivity by sorting the times and adding the values at duplicated times.
   * @return The cleaned sensitivity.
   */
  public CurveSensitivityMarket cleaned() {
    final Map<String, List<DoublesPair>> resultDsc = cleaned(_sensitivityYieldDiscounting);
    final Map<String, List<MarketForwardSensitivity>> resultFwd = cleanedFwd(_sensitivityForward);
    final Map<String, List<DoublesPair>> resultPrice = cleaned(_sensitivityPriceCurve);
    return new CurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  /**
   * Clean a map by sorting the times and adding the values at duplicated times.
   * @param map The map.
   * @return The cleaned map.
   */
  private static Map<String, List<DoublesPair>> cleaned(final Map<String, List<DoublesPair>> map) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final Map.Entry<String, List<DoublesPair>> entry : map.entrySet()) {
      final List<DoublesPair> list = entry.getValue();
      final List<DoublesPair> listClean = new ArrayList<DoublesPair>();
      final Set<Double> set = new TreeSet<Double>();
      for (final DoublesPair pair : list) {
        set.add(pair.getFirst());
      }
      for (final Double time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (Double.doubleToLongBits(list.get(looplist).getFirst()) == Double.doubleToLongBits(time)) {
            sensi += list.get(looplist).second;
          }
        }
        listClean.add(new DoublesPair(time, sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  private static Map<String, List<MarketForwardSensitivity>> cleanedFwd(final Map<String, List<MarketForwardSensitivity>> map) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<MarketForwardSensitivity>> result = new HashMap<String, List<MarketForwardSensitivity>>();
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : map.entrySet()) {
      final List<MarketForwardSensitivity> list = entry.getValue();
      final List<MarketForwardSensitivity> listClean = new ArrayList<MarketForwardSensitivity>();
      final Set<Triple<Double, Double, Double>> set = new TreeSet<Triple<Double, Double, Double>>();
      for (final MarketForwardSensitivity pair : list) {
        set.add(new Triple<Double, Double, Double>(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor()));
      }
      for (final Triple<Double, Double, Double> time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          final MarketForwardSensitivity fwdSensitivity = list.get(looplist);
          final Triple<Double, Double, Double> triple = new Triple<Double, Double, Double>(fwdSensitivity.getStartTime(), fwdSensitivity.getEndTime(), fwdSensitivity.getAccrualFactor());
          if (triple.equals(time)) {
            sensi += list.get(looplist).getValue();
          }
        }
        listClean.add(new MarketForwardSensitivity(time.getFirst(), time.getSecond(), time.getThird(), sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  @Override
  public String toString() {
    return _sensitivityYieldDiscounting.toString() + "\n" + _sensitivityForward.toString() + "\n" + _sensitivityPriceCurve.toString();
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CurveSensitivityMarket)) {
      return false;
    }
    final CurveSensitivityMarket other = (CurveSensitivityMarket) obj;
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
