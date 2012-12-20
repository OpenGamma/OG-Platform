/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.building;

import java.util.LinkedHashMap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Bundle of curves names and the curve building block associated.
 */
// TODO: REVIEW: should this be part of the YieldCurveBundle?
public class CurveBuildingBlockBundle {

  /**
   * The map with the bundle of curves to pairs of curve building blocks and the relevant part of the inverse Jacobian matrix.
   */
  private final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> _bundle;

  /**
   * Constructor.
   * An empty map is created.
   */
  public CurveBuildingBlockBundle() {
    _bundle = new LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>>();
  }

  /**
   * Constructor from a map. A new map is created.
   * @param bundle A map of string to curve building units and Jacobian.
   */
  public CurveBuildingBlockBundle(LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundle) {
    _bundle = new LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>>();
    _bundle.putAll(bundle);
  }

  /**
   * Add the curves and building block from another bundle. 
   * The existing map is changed. If the curves already existed in the bundle, the new value is used.
   * @param other The other bundle.
   */
  public void addAll(final CurveBuildingBlockBundle other) {
    _bundle.putAll(other._bundle);
  }

  /**
   * Returns the building block and inverse Jacobian matrix associated to a curve.
   * @param name The curve name.
   * @return The building block/matrix pair.
   */
  public Pair<CurveBuildingBlock, DoubleMatrix2D> getBlock(final String name) {
    return _bundle.get(name);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _bundle.hashCode();
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
    CurveBuildingBlockBundle other = (CurveBuildingBlockBundle) obj;
    if (!ObjectUtils.equals(_bundle, other._bundle)) {
      return false;
    }
    return true;
  }

}
