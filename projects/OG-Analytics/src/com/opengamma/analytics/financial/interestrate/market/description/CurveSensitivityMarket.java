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
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
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
   * Constructor from a yield discounting map, a forward map and a price index curve of sensitivity. The map are used directly.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @param sensitivityPriceCurve The map.
   */
  public CurveSensitivityMarket(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<MarketForwardSensitivity>> sensitivityForward,
      final Map<String, List<DoublesPair>> sensitivityPriceCurve) {
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
  public static CurveSensitivityMarket fromYieldDiscounting(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting) {
    return new CurveSensitivityMarket(sensitivityYieldDiscounting, new HashMap<String, List<MarketForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a forward map.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket fromForward(final Map<String, List<MarketForwardSensitivity>> sensitivityForward) {
    return new CurveSensitivityMarket(new HashMap<String, List<DoublesPair>>(), sensitivityForward, new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a forward map.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityForward The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket fromYieldDiscountingAndForward(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting,
      final Map<String, List<MarketForwardSensitivity>> sensitivityForward) {
    return new CurveSensitivityMarket(sensitivityYieldDiscounting, sensitivityForward, new HashMap<String, List<DoublesPair>>());
  }

  /**
   * Constructor from a yield discounting map and a price map.
   * @param sensitivityYieldDiscounting The map.
   * @param sensitivityPriceCurve The map.
   * @return The sensitivity.
   */
  public static CurveSensitivityMarket fromYieldDiscountingAndPrice(final Map<String, List<DoublesPair>> sensitivityYieldDiscounting, final Map<String, List<DoublesPair>> sensitivityPriceCurve) {
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
  public CurveSensitivityMarket multiply(final double factor) {
    final Map<String, List<DoublesPair>> resultDsc = multiply(_sensitivityYieldDiscounting, factor);
    final Map<String, List<MarketForwardSensitivity>> resultFwd = multiplyFwd(_sensitivityForward, factor);
    final Map<String, List<DoublesPair>> resultPrice = multiply(_sensitivityPriceCurve, factor);
    return new CurveSensitivityMarket(resultDsc, resultFwd, resultPrice);
  }

  private static Map<String, List<DoublesPair>> multiply(final Map<String, List<DoublesPair>> map, final double factor) {
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

  private static Map<String, List<MarketForwardSensitivity>> multiplyFwd(final Map<String, List<MarketForwardSensitivity>> map, final double factor) {
    final Map<String, List<MarketForwardSensitivity>> result = new HashMap<String, List<MarketForwardSensitivity>>();
    for (final String name : map.keySet()) {
      final List<MarketForwardSensitivity> curveSensi = new ArrayList<MarketForwardSensitivity>();
      for (final MarketForwardSensitivity pair : map.get(name)) {
        curveSensi.add(new MarketForwardSensitivity(pair.getPoint(), pair.getValue() * factor));
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
        set.add(pair.getPoint());
      }
      for (final Triple<Double, Double, Double> time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (list.get(looplist).getPoint().equals(time)) {
            sensi += list.get(looplist).getValue();
          }
        }
        listClean.add(new MarketForwardSensitivity(time, sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * The comparison is done on the discounting curve and forward curves sensitivities.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean compare(final CurveSensitivityMarket sensi1, final CurveSensitivityMarket sensi2, final double tolerance) {
    if (!InterestRateCurveSensitivityUtils.compare(sensi1.getYieldDiscountingSensitivities(), sensi2.getYieldDiscountingSensitivities(), tolerance)) {
      return false;
    }
    if (!compareFwd(sensi1.getForwardSensitivities(), sensi2.getForwardSensitivities(), tolerance)) {
      return false;
    }
    return true;
  }

  /**
   * Compare two maps of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a map).
   * @param sensi2 The second sensitivity (as a map).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  private static boolean compareFwd(final Map<String, List<MarketForwardSensitivity>> sensi1, final Map<String, List<MarketForwardSensitivity>> sensi2, final double tolerance) {
    Validate.notNull(sensi1, "sensitivity");
    Validate.notNull(sensi2, "sensitivity");
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        if (!compareFwd(entry.getValue(), sensi2.get(name), tolerance)) {
          return false;
        }
      } else {
        return false;
      }
    }
    for (final Map.Entry<String, List<MarketForwardSensitivity>> entry : sensi2.entrySet()) {
      if (!(sensi1.containsKey(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare two lists of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a list).
   * @param sensi2 The second sensitivity (as a list).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not.
   */
  private static boolean compareFwd(final List<MarketForwardSensitivity> sensi1, final List<MarketForwardSensitivity> sensi2, final double tolerance) {
    for (int looptime = 0; looptime < sensi1.size(); looptime++) {
      if ((Math.abs(sensi1.get(looptime).getPoint().getFirst() - sensi2.get(looptime).getPoint().getFirst()) > tolerance)
          || (Math.abs(sensi1.get(looptime).getValue() - sensi2.get(looptime).getValue()) > tolerance)) {
        return false;
      }
    }
    return true;
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
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
