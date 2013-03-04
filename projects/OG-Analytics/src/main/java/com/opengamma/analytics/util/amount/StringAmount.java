/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.amount;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Object to represent values linked to strings for which the values can be added or multiplied by a constant.
 * Used for different sensitivities (parallel curve sensitivity,...). The objects stored as a HashMap(String, Double).
 */
public class StringAmount {

  /**
   * The data stored as a map. Not null.
   */
  private final HashMap<String, Double> _data;

  /**
   * Constructor. Create an empty map.
   */
  public StringAmount() {
    _data = new HashMap<>();
  }

  /**
   * Constructor from an existing map. The map is used in the new object (no new map is created).
   * @param map The map.
   */
  private StringAmount(final HashMap<String, Double> map) {
    ArgumentChecker.notNull(map, "Map");
    _data = new HashMap<>(map);
  }

  /**
   * Builder from on point.
   * @param point The surface point.
   * @param value The associated value.
   * @return The surface value.
   */
  public static StringAmount from(final String point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    final HashMap<String, Double> data = new HashMap<>();
    data.put(point, value);
    return new StringAmount(data);
  }

  /**
   * Builder from a map. A new map is created with the same values.
   * @param map The map.
   * @return The surface value.
   */
  public static StringAmount from(final Map<String, Double> map) {
    ArgumentChecker.notNull(map, "Map");
    final HashMap<String, Double> data = new HashMap<>();
    data.putAll(map);
    return new StringAmount(data);
  }

  /**
   * Builder from a StringValue. A new map is created with the same values.
   * @param surface The StringValue
   * @return The surface value.
   */
  public static StringAmount from(final StringAmount surface) {
    ArgumentChecker.notNull(surface, "Surface value");
    final HashMap<String, Double> data = new HashMap<>();
    data.putAll(surface.getMap());
    return new StringAmount(data);
  }

  /**
   * Gets the underlying map.
   * @return The map.
   */
  public HashMap<String, Double> getMap() {
    return _data;
  }

  /**
   * Add a value to the object. The existing object is modified. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param point The surface point.
   * @param value The associated value.
   */
  public void add(final String point, final Double value) {
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
   * @param value1 The first "string value".
   * @param value2 The second "string value".
   * @return The combined/sum "string value".
   */
  public static StringAmount plus(final StringAmount value1, final StringAmount value2) {
    ArgumentChecker.notNull(value1, "Surface value 1");
    ArgumentChecker.notNull(value2, "Surface value 2");
    final HashMap<String, Double> plus = new HashMap<>(value1._data);
    for (final String p : value2._data.keySet()) {
      if (value1._data.containsKey(p)) {
        plus.put(p, value2._data.get(p) + value1._data.get(p));
      } else {
        plus.put(p, value2._data.get(p));
      }
    }
    return new StringAmount(plus);
  }

  /**
   * Create a new object containing the point of the initial object and the new point. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing point of the object, the value is added to the existing value.
   * @param stringValue The surface value.
   * @param point The surface point.
   * @param value The associated value.
   * @return The combined/sum surface value.
   */
  public static StringAmount plus(final StringAmount stringValue, final String point, final Double value) {
    ArgumentChecker.notNull(stringValue, "Surface value");
    ArgumentChecker.notNull(point, "Point");
    final HashMap<String, Double> plus = new HashMap<>(stringValue._data);
    if (stringValue._data.containsKey(point)) {
      plus.put(point, value + stringValue._data.get(point));
    } else {
      plus.put(point, value);
    }
    return new StringAmount(plus);
  }

  /**
   * Create a new object containing the point of the initial object with the all values multiplied by a given factor.
   * @param stringValue The surface value.
   * @param factor The multiplicative factor.
   * @return The multiplied surface.
   */
  public static StringAmount multiplyBy(final StringAmount stringValue, final double factor) {
    ArgumentChecker.notNull(stringValue, "Surface value");
    final HashMap<String, Double> multiplied = new HashMap<>();
    for (final String p : stringValue._data.keySet()) {
      multiplied.put(p, stringValue._data.get(p) * factor);
    }
    return new StringAmount(multiplied);
  }

  /**
   * Compare the values in two objects. The result is true if the list of strings are the same in both maps and the differences between the values associated of each of those strings are
   * less than the tolerance.
   * @param value1 The first "string value".
   * @param value2 The second "string value".
   * @param tolerance The tolerance.
   * @return The comparison flag.
   */
  public static boolean compare(final StringAmount value1, final StringAmount value2, final double tolerance) {
    final Set<String> set1 = value1._data.keySet();
    final Set<String> set2 = value2._data.keySet();
    if (!set1.equals(set2)) {
      return false;
    }
    for (final String p : set1) {
      if (Math.abs(value1._data.get(p) - value2._data.get(p)) > tolerance) {
        return false;
      }
    }
    return true;
  }

}
