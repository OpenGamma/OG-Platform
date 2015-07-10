/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Bundle of curves names and the curve building block associated.
 */
// TODO: [PLAT-5768] Should this be part of the MulticurveProvider?
public class CurveBuildingBlockBundle {

  /**
   * The map with the bundle of curves names to pairs of curve building blocks and the relevant part of the inverse Jacobian matrix.
   * The inverse Jacobian matrix is the derivative of the curve parameters with respect to the market quotes.
   */
  private final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> _bundle;

  /**
   * Constructor.
   * An empty map is created.
   */
  public CurveBuildingBlockBundle() {
    _bundle = new LinkedHashMap<>();
  }

  /**
   * Constructor from a map. A new map is created.
   * @param bundle A map of curve names to curve building units and Jacobian.
   */
  public CurveBuildingBlockBundle(final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundle) {
    _bundle = new LinkedHashMap<>();
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
   * @param name The name of the curve
   * @param block The block
   * @param mat The inverse Jacobian matrix
   */
  public void add(final String name, final CurveBuildingBlock block, final DoubleMatrix2D mat) {
    _bundle.put(name, Pairs.of(block, mat));
  }

  /**
   * Returns the building block and inverse Jacobian matrix associated to a curve.
   * @param name The curve name.
   * @return The building block/matrix pair.
   */
  public Pair<CurveBuildingBlock, DoubleMatrix2D> getBlock(final String name) {
    final Pair<CurveBuildingBlock, DoubleMatrix2D> block = _bundle.get(name);
    if (!(block == null)) {
      return block;
    }
    throw new IllegalArgumentException("There is no block for the curve: " + name);

  }

  /**
   * Gets all of the underlying data.
   * @return The data wrapped in an unmodifiable map
   */
  public Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> getData() {
    return Collections.unmodifiableMap(_bundle);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _bundle.hashCode();
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
    final CurveBuildingBlockBundle other = (CurveBuildingBlockBundle) obj;
    if (!ObjectUtils.equals(_bundle, other._bundle)) {
      return false;
    }
    return true;
  }

}
