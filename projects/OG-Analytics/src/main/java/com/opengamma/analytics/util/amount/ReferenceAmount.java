/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.amount;

import java.util.HashMap;
import java.util.Map.Entry;

import com.opengamma.util.ArgumentChecker;

/**
 * Object to represent values linked to a reference for which the values can be added or multiplied by a constant.
 * Used for different sensitivities (parallel curve sensitivity,...). The objects stored as a HashMap(REFERENCE, Double).
 * @param <REFERENCE> The reference object.
 */
public class ReferenceAmount<REFERENCE> {

  /**
   * The data stored as a map. Not null.
   */
  private final HashMap<REFERENCE, Double> _data;

  /**
   * Constructor. Create an empty map.
   */
  public ReferenceAmount() {
    _data = new HashMap<>();
  }

  /**
   * Constructor from an existing map. The map is used in the new object (no new map is created).
   * @param map The map.
   */
  private ReferenceAmount(final HashMap<REFERENCE, Double> map) {
    _data = map;
  }

  /**
   * Gets the underlying map.
   * @return The map.
   */
  public HashMap<REFERENCE, Double> getMap() {
    return _data;
  }

  /**
   * Add a value to the object. The existing object is modified. If the point is not in the existing points of the object, it is put in the map.
   * If a point is already in the existing points of the object, the value is added to the existing value.
   * @param point The surface point.
   * @param value The associated value.
   */
  public void add(final REFERENCE point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    if (_data.containsKey(point)) {
      _data.put(point, value + _data.get(point));
    } else {
      _data.put(point, value);
    }
  }

  /**
   * Create a new object containing the points of the initial object plus the points of another object. If two points <REFERENCE> are identical, the values are added.
   * @param other The other ReferenceAmount.
   * @return The total.
   */
  public ReferenceAmount<REFERENCE> plus(final ReferenceAmount<REFERENCE> other) {
    final HashMap<REFERENCE, Double> plusMap = new HashMap<>(_data);
    final ReferenceAmount<REFERENCE> plus = new ReferenceAmount<>(plusMap);
    for (final Entry<REFERENCE, Double> p : other._data.entrySet()) {
      plus.add(p.getKey(), p.getValue());
    }
    return plus;
  }

  /**
   * Create a new object containing the point of the initial object with the all values multiplied by a given factor.
   * @param factor The multiplicative factor.
   * @return The multiplied surface.
   */
  public ReferenceAmount<REFERENCE> multiplyBy(final double factor) {
    final HashMap<REFERENCE, Double> multiplied = new HashMap<>();
    for (final REFERENCE p : _data.keySet()) {
      multiplied.put(p, _data.get(p) * factor);
    }
    return new ReferenceAmount<>(multiplied);
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
    @SuppressWarnings("unchecked")
    final ReferenceAmount<REFERENCE> other = (ReferenceAmount<REFERENCE>) obj;
    if (_data == null) {
      if (other._data != null) {
        return false;
      }
    } else if (!_data.equals(other._data)) {
      return false;
    }
    return true;
  }

}
