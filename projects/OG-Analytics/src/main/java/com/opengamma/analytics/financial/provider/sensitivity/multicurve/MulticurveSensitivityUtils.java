/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Utilities to manipulate present value sensitivities.
 * <p>
 * This is a thread-safe static utility class.
 */
public class MulticurveSensitivityUtils {

  /**
   * Restricted constructor.
   */
  protected MulticurveSensitivityUtils() {
    super();
  }

  /**
   * Clean a map by sorting the times and adding the values at duplicated times.
   * @param map The map.
   * @return The cleaned map.
   */
  public static Map<String, List<DoublesPair>> cleaned(final Map<String, List<DoublesPair>> map) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : map.entrySet()) {
      final List<DoublesPair> list = entry.getValue();
      final List<DoublesPair> listClean = new ArrayList<>();
      final Set<Double> set = new TreeSet<>();
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
        listClean.add(DoublesPair.of(time.doubleValue(), sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  /**
   * Clean a map by sorting the times and adding the values at duplicated times. The total value below the tolerance threshold are removed.
   * @param map The map.
   * @param tolerance The tolerance.
   * @return The cleaned map.
   */
  public static Map<String, List<DoublesPair>> cleaned(final Map<String, List<DoublesPair>> map, final double tolerance) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : map.entrySet()) {
      final List<DoublesPair> list = entry.getValue();
      final List<DoublesPair> listClean = new ArrayList<>();
      final Set<Double> set = new TreeSet<>();
      for (final DoublesPair pair : list) {
        set.add(pair.getFirst());
      }
      for (final Double time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (Double.doubleToLongBits(list.get(looplist).getFirstDouble()) == Double.doubleToLongBits(time)) {
            sensi += list.get(looplist).second;
          }
        }
        if (Math.abs(sensi) > tolerance) {
          listClean.add(DoublesPair.of(time.doubleValue(), sensi));
        }
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  public static Map<String, List<ForwardSensitivity>> cleanedFwd(final Map<String, List<ForwardSensitivity>> map) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<ForwardSensitivity>> result = new HashMap<>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map.entrySet()) {
      final List<ForwardSensitivity> list = entry.getValue();
      final List<ForwardSensitivity> listClean = new ArrayList<>();
      final Set<Triple<Double, Double, Double>> set = new TreeSet<>();
      for (final ForwardSensitivity pair : list) {
        set.add(Triple.of(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor()));
      }
      for (final Triple<Double, Double, Double> time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          final ForwardSensitivity fwdSensitivity = list.get(looplist);
          final Triple<Double, Double, Double> triple = Triple.of(fwdSensitivity.getStartTime(), fwdSensitivity.getEndTime(), fwdSensitivity.getAccrualFactor());
          if (triple.equals(time)) {
            sensi += list.get(looplist).getValue();
          }
        }
        listClean.add(new SimplyCompoundedForwardSensitivity(time.getFirst(), time.getSecond(), time.getThird(), sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  public static Map<String, List<ForwardSensitivity>> cleanedFwd(final Map<String, List<ForwardSensitivity>> map, final double tolerance) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<ForwardSensitivity>> result = new HashMap<>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map.entrySet()) {
      final List<ForwardSensitivity> list = entry.getValue();
      final List<ForwardSensitivity> listClean = new ArrayList<>();
      final Set<Triple<Double, Double, Double>> set = new TreeSet<>();
      for (final ForwardSensitivity pair : list) {
        set.add(Triple.of(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor()));
      }
      for (final Triple<Double, Double, Double> time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          final ForwardSensitivity fwdSensitivity = list.get(looplist);
          final Triple<Double, Double, Double> triple = Triple.of(fwdSensitivity.getStartTime(), fwdSensitivity.getEndTime(), fwdSensitivity.getAccrualFactor());
          if (triple.equals(time)) {
            sensi += list.get(looplist).getValue();
          }
        }
        if (Math.abs(sensi) > tolerance) {
          listClean.add(new SimplyCompoundedForwardSensitivity(time.getFirst(), time.getSecond(), time.getThird(), sensi));
        }
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  /**
   * Add two list representing sensitivities into one. No attempt is made to net off sensitivities occurring at the same time - Use clean()
   * to do this
   * @param sensi1 First list of sensitivities
   * @param sensi2 Second list of sensitivities
   * @return combined list
   */
  public static List<DoublesPair> plus(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2) {
    final List<DoublesPair> result = new ArrayList<>(sensi1);
    result.addAll(sensi2);
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Add two maps representing sensitivities into one.
   * 
   * @param sensi1  the first sensitivity, not null
   * @param sensi2  the second sensitivity, not null
   * @return the total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> plus(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2) {
    ArgumentChecker.notNull(sensi1, "sensitivity");
    ArgumentChecker.notNull(sensi2, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        result.put(name, plus(entry.getValue(), sensi2.get(name)));
      } else {
        result.put(name, entry.getValue());
      }
    }
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        result.put(name, entry.getValue());
      }
    }
    return result;
  }

  /**
   * Add the list representing the sensitivity to one curve to the map of sensitivities to several curves.
   * @param sensi The multi-curves sensitivity. Not null.
   * @param curveName  The name of the curve the sensitivity of which is added. Not null.
   * @param list The sensitivity as a list. Not null.
   * @return The total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> plus(final Map<String, List<DoublesPair>> sensi, final String curveName, final List<DoublesPair> list) {
    ArgumentChecker.notNull(sensi, "sensitivity");
    ArgumentChecker.notNull(list, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi.entrySet()) {
      final String name = entry.getKey();
      if (name.equals(curveName)) {
        result.put(name, plus(entry.getValue(), list));
      } else {
        result.put(name, entry.getValue());
      }
    }
    if (!result.containsKey(curveName)) {
      result.put(curveName, list);
    }
    return result;
  }

  /**
   * Add two maps links to forward curves.
   * @param map1 The first map.
   * @param map2 The second map.
   * @return The sum.
   */
  public static Map<String, List<ForwardSensitivity>> plusFwd(final Map<String, List<ForwardSensitivity>> map1, final Map<String, List<ForwardSensitivity>> map2) {
    final Map<String, List<ForwardSensitivity>> result = new HashMap<>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map1.entrySet()) {
      final List<ForwardSensitivity> temp = new ArrayList<>();
      final String name = entry.getKey();
      for (final ForwardSensitivity pair : entry.getValue()) {
        temp.add(pair);
      }
      if (map2.containsKey(name)) {
        for (final ForwardSensitivity pair : map2.get(name)) {
          temp.add(pair);
        }
      }
      result.put(name, temp);
    }
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        final List<ForwardSensitivity> temp = new ArrayList<>();
        for (final ForwardSensitivity pair : entry.getValue()) {
          temp.add(pair);
        }
        result.put(name, temp);
      }
    }
    return result;
  }

  /**
   * Multiply a sensitivity map by a common factor.
   * 
   * @param sensitivity  the original sensitivity, not null
   * @param factor  the multiplicative factor, not null
   * @return the multiplied sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> multipliedBy(final Map<String, List<DoublesPair>> sensitivity, final double factor) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivity.entrySet()) {
      result.put(entry.getKey(), multipliedBy(entry.getValue(), factor));
    }
    return result;
  }

  public static List<DoublesPair> multipliedBy(final List<DoublesPair> sensitivity, final double factor) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    final List<DoublesPair> curveSensi = new ArrayList<>();
    for (final DoublesPair pair : sensitivity) {
      curveSensi.add(DoublesPair.of(pair.first, pair.second * factor));
    }
    return curveSensi;
  }

  /**
   * Product of two sensitivities
   * 
   * @param sensi1  the original sensitivity, not null
   * @param sensi2  the other sensitivity, not null
   * @return the product sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> productOf(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2) {
    ArgumentChecker.notNull(sensi1, "sensitivity");
    ArgumentChecker.notNull(sensi2, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        result.put(name, productOf(entry.getValue(), sensi2.get(name)));
      }
    }
    return result;
  }

  public static List<DoublesPair> productOf(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2) {
    final List<DoublesPair> curveSensi = new ArrayList<>();
    final int length2 = sensi2.size();
    for (final DoublesPair pair : sensi1) {
      for (int i = 0; i < length2; ++i) {
        if (pair.first == sensi2.get(i).first) {
          curveSensi.add(DoublesPair.of(pair.first, pair.second * sensi2.get(i).second));
        }
      }
    }
    return curveSensi;
  }

  public static Map<String, List<ForwardSensitivity>> multipliedByFwd(final Map<String, List<ForwardSensitivity>> map, final double factor) {
    final Map<String, List<ForwardSensitivity>> result = new HashMap<>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map.entrySet()) {
      final List<ForwardSensitivity> curveSensi = new ArrayList<>();
      for (final ForwardSensitivity pair : entry.getValue()) {
        curveSensi.add(new SimplyCompoundedForwardSensitivity(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor(), pair.getValue() * factor));
      }
      result.put(entry.getKey(), curveSensi);
    }
    return result;
  }

  /**
   * Product of two sensitivities
   * @param map1 the original sensitivity
   * @param map2 the other sensitivity
   * @return the new sensitivity
   */
  public static Map<String, List<ForwardSensitivity>> productOfFwd(final Map<String, List<ForwardSensitivity>> map1, final Map<String, List<ForwardSensitivity>> map2) {
    final Map<String, List<ForwardSensitivity>> result = new HashMap<>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map1.entrySet()) {
      final List<ForwardSensitivity> curveSensi = new ArrayList<>();
      final String name = entry.getKey();
      if (map2.containsKey(name)) {
        final int length2 = map2.size();
        for (final ForwardSensitivity pair1 : entry.getValue()) {
          for (int i = 0; i < length2; ++i) {
            if (pair1.getStartTime() == map2.get(name).get(i).getStartTime() && pair1.getEndTime() == map2.get(name).get(i).getEndTime()) {
              curveSensi.add(new SimplyCompoundedForwardSensitivity(pair1.getStartTime(), pair1.getEndTime(), pair1.getAccrualFactor(), pair1.getValue() * map2.get(name).get(i).getValue()));
            }
          }
        }
      }
      result.put(entry.getKey(), curveSensi);
    }
    return result;
  }

}
