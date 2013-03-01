/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.surface;

import java.util.HashMap;

import com.opengamma.util.ArgumentChecker;

/**
 * Object to represent values linked to a reference for which the values can be added or multiplied by a constant.
 * Used for different sensitivities (parallel curve sensitivity,...). The objects stored as a HashMap(String, Double).
 * @param <REFERENCE> The reference object.
 */
public class ReferenceValue<REFERENCE> {

  /**
   * The data stored as a map. Not null.
   */
  private final HashMap<REFERENCE, Double> _data;

  /**
   * Constructor. Create an empty map.
   */
  public ReferenceValue() {
    _data = new HashMap<>();
  }

  /**
   * Constructor from an existing map. The map is used in the new object (no new map is created).
   * @param map The map.
   */
  private ReferenceValue(final HashMap<REFERENCE, Double> map) {
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
  // TODO: do we need this?
  public void add(final REFERENCE point, final Double value) {
    ArgumentChecker.notNull(point, "Point");
    if (_data.containsKey(point)) {
      _data.put(point, value + _data.get(point));
    } else {
      _data.put(point, value);
    }
  }

  // TODO: ReferenceValue<REFERENCE> plus(final ReferenceValue<REFERENCE> other)

  /**
   * Create a new object containing the point of the initial object with the all values multiplied by a given factor.
   * @param factor The multiplicative factor.
   * @return The multiplied surface.
   */
  public ReferenceValue<REFERENCE> multiplyBy(final double factor) {
    final HashMap<REFERENCE, Double> multiplied = new HashMap<>();
    for (final REFERENCE p : _data.keySet()) {
      multiplied.put(p, _data.get(p) * factor);
    }
    return new ReferenceValue<REFERENCE>(multiplied);
  }

}
