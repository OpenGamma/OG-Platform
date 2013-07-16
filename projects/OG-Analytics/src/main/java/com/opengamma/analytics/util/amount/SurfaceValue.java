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
import com.opengamma.util.tuple.DoublesPair;

/**
 * Object to represent values linked to a surface (pair of doubles) for which the values can be added or multiplied by a constant.
 * Used for different sensitivities (SABR, FX,...). The objects stored as a HashMap(DoublesPair, Double).
 */
public class SurfaceValue {

  /**
   * The data stored as a map. Not null.
   */
  private final HashMap<DoublesPair, Double> _data;

  /**
   * Constructor. Create an empty map.
   */
  public SurfaceValue() {
    _data = new HashMap<>();
  }

  /**
   * Constructor from an existing map. The map is used in the new object.
   * @param map The map.
   */
  private SurfaceValue(final HashMap<DoublesPair, Double> map) {
    ArgumentChecker.notNull(map, "Map");
    _data = new HashMap<>(map);
  }

  /**
   * Builder from on point.
   * @param point The surface point.
   * @param value The associated value.
   * @return The surface value.
   */
  public static SurfaceValue from(final DoublesPair point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    final HashMap<DoublesPair, Double> data = new HashMap<>();
    data.put(point, value);
    return new SurfaceValue(data);
  }

  /**
   * Builder from a map. A new map is created with the same values.
   * @param map The map.
   * @return The surface value.
   */
  public static SurfaceValue from(final Map<DoublesPair, Double> map) {
    ArgumentChecker.notNull(map, "Map");
    final HashMap<DoublesPair, Double> data = new HashMap<>();
    data.putAll(map);
    return new SurfaceValue(data);
  }

  /**
   * Builder from a SurfaceValue. A new map is created with the same values.
   * @param surface The SurfaceValue
   * @return The surface value.
   */
  public static SurfaceValue from(final SurfaceValue surface) {
    ArgumentChecker.notNull(surface, "Surface value");
    final HashMap<DoublesPair, Double> data = new HashMap<>();
    data.putAll(surface.getMap());
    return new SurfaceValue(data);
  }

  /**
   * Gets the underlying map.
   * @return The map.
   */
  public HashMap<DoublesPair, Double> getMap() {
    return _data;
  }

  /**
   * Add a value to the object. The existing object is modified. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param point The surface point.
   * @param value The associated value.
   */
  public void add(final DoublesPair point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    if (_data.containsKey(point)) {
      _data.put(point, value + _data.get(point));
    } else {
      _data.put(point, value);
    }
  }

  /**
   * Gets the number of elements in the map.
   * @return The number of elements.
   */
  public int getNumberOfElements() {
    return _data.size();
  }

  /**
   * Create a new object containing the point of both initial objects. If a point is only on one surface, its value is the original value.
   * If a point is on both surfaces, the values on that point are added.
   * @param value1 The first surface value.
   * @param value2 The second surface value.
   * @return The combined/sum surface value.
   */
  public static SurfaceValue plus(final SurfaceValue value1, final SurfaceValue value2) {
    ArgumentChecker.notNull(value1, "Surface value 1");
    ArgumentChecker.notNull(value2, "Surface value 2");
    final HashMap<DoublesPair, Double> plus = new HashMap<>(value1._data);
    for (final DoublesPair p : value2._data.keySet()) {
      if (value1._data.containsKey(p)) {
        plus.put(p, value2._data.get(p) + value1._data.get(p));
      } else {
        plus.put(p, value2._data.get(p));
      }
    }
    return new SurfaceValue(plus);
  }

  /**
   * Create a new object containing the point of the initial object and the new point. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing point of the object, the value is added to the existing value.
   * @param surfaceValue The surface value.
   * @param point The surface point.
   * @param value The associated value.
   * @return The combined/sum surface value.
   */
  public static SurfaceValue plus(final SurfaceValue surfaceValue, final DoublesPair point, final Double value) {
    ArgumentChecker.notNull(surfaceValue, "Surface value");
    ArgumentChecker.notNull(point, "Point");
    final HashMap<DoublesPair, Double> plus = new HashMap<>(surfaceValue._data);
    if (surfaceValue._data.containsKey(point)) {
      plus.put(point, value + surfaceValue._data.get(point));
    } else {
      plus.put(point, value);
    }
    return new SurfaceValue(plus);
  }

  /**
   * Create a new object containing the point of the initial object with the all values multiplied by a given factor.
   * @param surfaceValue The surface value.
   * @param factor The multiplicative factor.
   * @return The multiplied surface.
   */
  public static SurfaceValue multiplyBy(final SurfaceValue surfaceValue, final double factor) {
    ArgumentChecker.notNull(surfaceValue, "Surface value");
    final HashMap<DoublesPair, Double> multiplied = new HashMap<>();
    for (final DoublesPair p : surfaceValue._data.keySet()) {
      multiplied.put(p, surfaceValue._data.get(p) * factor);
    }
    return new SurfaceValue(multiplied);
  }

  /**
   * Compare two objects with a given tolerance. Return "true" if all the values are within the tolerance.
   * @param value1 The first object.
   * @param value2 The second object.
   * @param tolerance The tolerance.
   * @return The comparison flag.
   */
  public static boolean compare(final SurfaceValue value1, final SurfaceValue value2, final double tolerance) {
    final Set<DoublesPair> set1 = value1._data.keySet();
    final Set<DoublesPair> set2 = value2._data.keySet();
    for (final DoublesPair p : set1) {
      if (value2._data.get(p) == null && Math.abs(value1._data.get(p)) > tolerance) {
        return false;
      }
      if (value2._data.get(p) != null) {
        if (Math.abs(value1._data.get(p) - value2._data.get(p)) > tolerance) {
          return false;
        }
      }
    }
    for (final DoublesPair p : set2) {
      if (value1._data.get(p) == null && Math.abs(value2._data.get(p)) > tolerance) {
        return false;
      }
      if (value1._data.get(p) != null) {
        if (Math.abs(value2._data.get(p) - value1._data.get(p)) > tolerance) {
          return false;
        }
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
    for (final DoublesPair point : _data.keySet()) {
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
    final SurfaceValue other = (SurfaceValue) obj;
    if (!ObjectUtils.equals(_data, other._data)) {
      return false;
    }
    return true;
  }

}
