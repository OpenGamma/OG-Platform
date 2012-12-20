/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.building;

import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Class describing a block of curves build together.
 */
public class CurveBuildingBlock {

  /**
   * The list of curve in the block as a map: Name to a pair of integers: 
   *   1) Start of the curve parameters in the array of all parameters of the block. 2) Number of parameters in the curve
   */
  private final LinkedHashMap<String, Pair<Integer, Integer>> _unit;

  /**
   * Constructor from a map. A new map is created.
   * @param block The curve block. Not null.
   */
  public CurveBuildingBlock(LinkedHashMap<String, Pair<Integer, Integer>> block) {
    ArgumentChecker.notNull(block, "Curve building block");
    _unit = new LinkedHashMap<String, Pair<Integer, Integer>>();
    _unit.putAll(block);
  }

  /**
   * Returns the start index of the given string in the array of all parameters.
   * @param name The string name.
   * @return The start index.
   */
  public Integer getStart(final String name) {
    return _unit.get(name).getFirst();
  }

  /**
   * Returns the number of parameters for the curve.
   * @param name The curve name.
   * @return The number of parameters.
   */
  public Integer getNbParameters(final String name) {
    return _unit.get(name).getSecond();
  }

  /**
   * Returns a set with all the curve names.
   * @return The set of names.
   */
  public Set<String> getAllNames() {
    return _unit.keySet();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _unit.hashCode();
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
    CurveBuildingBlock other = (CurveBuildingBlock) obj;
    if (!ObjectUtils.equals(_unit, other._unit)) {
      return false;
    }
    return true;
  }

}
