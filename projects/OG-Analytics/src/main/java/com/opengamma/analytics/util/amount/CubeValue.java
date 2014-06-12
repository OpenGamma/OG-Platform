/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.amount;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Object to represent values linked to a cube (triple of doubles) for which the values can be added or multiplied by a constant.
 * Used for different sensitivities (FX,...). The objects stored as a HashMap(DoublesPair, Double).
 */
public class CubeValue {

  /**
   * The data stored as a map. Not null.
   */
  private final HashMap<Triple<Double, Double, Double>, Double> _data;

  /**
   * Constructor. Create an empty map.
   */
  public CubeValue() {
    _data = new HashMap<>();
  }

  /**
   * Constructor from an existing map. The map is used in the new object.
   * @param map The map.
   */
  private CubeValue(final HashMap<Triple<Double, Double, Double>, Double> map) {
    ArgumentChecker.notNull(map, "Map");
    _data = new HashMap<>(map);
  }

  /**
   * Builder from on point.
   * @param point The surface point.
   * @param value The associated value.
   * @return The surface value.
   */
  public static CubeValue from(final Triple<Double, Double, Double> point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    final HashMap<Triple<Double, Double, Double>, Double> data = new HashMap<>();
    data.put(point, value);
    return new CubeValue(data);
  }

  /**
   * Builder from a map. A new map is created with the same values.
   * @param map The map.
   * @return The surface value.
   */
  public static CubeValue from(final Map<Triple<Double, Double, Double>, Double> map) {
    ArgumentChecker.notNull(map, "Map");
    final HashMap<Triple<Double, Double, Double>, Double> data = new HashMap<>();
    data.putAll(map);
    return new CubeValue(data);
  }

  /**
   * Builder from a SurfaceValue. A new map is created with the same values.
   * @param surface The SurfaceValue
   * @return The surface value.
   */
  public static CubeValue from(final CubeValue surface) {
    ArgumentChecker.notNull(surface, "Surface value");
    final HashMap<Triple<Double, Double, Double>, Double> data = new HashMap<>();
    data.putAll(surface.getMap());
    return new CubeValue(data);
  }

  /**
   * Gets the underlying map.
   * @return The map.
   */
  public HashMap<Triple<Double, Double, Double>, Double> getMap() {
    return _data;
  }

  /**
   * Add a value to the object. The existing object is modified. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param point The surface point.
   * @param value The associated value.
   */
  public void add(final Triple<Double, Double, Double> point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    if (_data.containsKey(point)) {
      _data.put(point, value + _data.get(point));
    } else {
      _data.put(point, value);
    }
  }

  /**
   * Create a new object containing the point of both initial objects. If a point is only on one surface, its value is the original value.
   * If a point is on both surfaces, the values on that point are added.
   * @param value1 The first surface value.
   * @param value2 The second surface value.
   * @return The combined/sum surface value.
   */
  public static CubeValue plus(final CubeValue value1, final CubeValue value2) {
    ArgumentChecker.notNull(value1, "Surface value 1");
    ArgumentChecker.notNull(value2, "Surface value 2");
    final HashMap<Triple<Double, Double, Double>, Double> plus = new HashMap<>(value1._data);
    for (final Triple<Double, Double, Double> p : value2._data.keySet()) {
      if (value1._data.containsKey(p)) {
        plus.put(p, value2._data.get(p) + value1._data.get(p));
      } else {
        plus.put(p, value2._data.get(p));
      }
    }
    return new CubeValue(plus);
  }

  /**
   * Create a new object containing the point of the initial object and the new point. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing point of the object, the value is added to the existing value.
   * @param surfaceValue The surface value.
   * @param point The surface point.
   * @param value The associated value.
   * @return The combined/sum surface value.
   */
  public static CubeValue plus(final CubeValue surfaceValue, final Triple<Double, Double, Double> point, final Double value) {
    ArgumentChecker.notNull(surfaceValue, "Surface value");
    ArgumentChecker.notNull(point, "Point");
    final HashMap<Triple<Double, Double, Double>, Double> plus = new HashMap<>(surfaceValue._data);
    if (surfaceValue._data.containsKey(point)) {
      plus.put(point, value + surfaceValue._data.get(point));
    } else {
      plus.put(point, value);
    }
    return new CubeValue(plus);
  }

  /**
   * Create a new object containing the point of the initial object with the all values multiplied by a given factor.
   * @param surfaceValue The surface value.
   * @param factor The multiplicative factor.
   * @return The multiplied surface.
   */
  public static CubeValue multiplyBy(final CubeValue surfaceValue, final double factor) {
    ArgumentChecker.notNull(surfaceValue, "Surface value");
    final HashMap<Triple<Double, Double, Double>, Double> multiplied = new HashMap<>();
    for (final Triple<Double, Double, Double> p : surfaceValue._data.keySet()) {
      multiplied.put(p, surfaceValue._data.get(p) * factor);
    }
    return new CubeValue(multiplied);
  }

  public static boolean compare(final CubeValue value1, final CubeValue value2, final double tolerance) {
    final Set<Triple<Double, Double, Double>> set1 = value1._data.keySet();
    final Set<Triple<Double, Double, Double>> set2 = value2._data.keySet();
    if (!set1.equals(set2)) {
      return false;
    }
    for (final Triple<Double, Double, Double> p : set1) {
      if (Math.abs(value1._data.get(p) - value2._data.get(p)) > tolerance) {
        return false;
      }
    }
    return true;
  }

  /**
   * Collapse the object to a single value. The points on which the amounts occur are ignored and the values summed.
   * @return The value.
   */
  public double toSingleValue() {
    double amount = 0;
    for (final Triple<Double, Double, Double> point : _data.keySet()) {
      amount += _data.get(point);
    }
    return amount;
  }

  @Override
  public String toString() {
    return _data.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _data.hashCode();
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
    final CubeValue other = (CubeValue) obj;
    if (!ObjectUtils.equals(_data, other._data)) {
      return false;
    }
    return true;
  }

}
