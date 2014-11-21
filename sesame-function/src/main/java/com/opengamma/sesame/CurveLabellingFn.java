/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import java.util.Map;
import java.util.Set;

import com.opengamma.util.result.Result;

/**
 * Function allowing the retrieval of labellers for a
 * set of curves.
 * <p>
 * The labellers allow the nodes in a curve to be given a name
 * (generally a tenor) rather than a double value. These can
 * then be used to label various objects, for example sensitivity
 * matrices.
 */
public interface CurveLabellingFn {

  /**
   * Retrieve the labels for a set of curves.
   * <p>
   * If a labeller could not be found for any of the specified
   * curve names, then a failure result will be returned.
   *
   * @param curveNames  the set of curves names to get labellers for
   * @return a result containing a map with a curve labeller for
   *   each of the requested curves, a failure result otherwise
   */
  Result<Map<String, CurveMatrixLabeller>> getCurveLabellers(Set<String> curveNames);
}
