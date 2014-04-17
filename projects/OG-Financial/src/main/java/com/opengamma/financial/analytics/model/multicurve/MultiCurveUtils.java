/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve;

import java.util.Iterator;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for results calculated using multi-curves.
 */
public class MultiCurveUtils {

  /**
   * Converts a {@link DoubleMatrix1D} of yield curve node sensitivities to a {@link DoubleLabelledMatrix1D},
   * where the labels are the names of the {@link CurveNode} or the node class name and maturity tenor if
   * this value is null
   * @param sensitivities The matrix of sensitivities, not null
   * @param definition The curve definition, not null
   * @return A labelled matrix
   */
  public static DoubleLabelledMatrix1D getLabelledMatrix(final DoubleMatrix1D sensitivities, final CurveDefinition definition) {
    ArgumentChecker.notNull(sensitivities, "sensitivities");
    ArgumentChecker.notNull(definition, "definition");
    final int n = sensitivities.getNumberOfElements();
    if (n != definition.getNodes().size()) {
      throw new OpenGammaRuntimeException("Did not have a sensitivity for each curve node");
    }
    final Double[] keys = new Double[n];
    final double[] values = new double[n];
    final Object[] labels = new Object[n];
    final Iterator<CurveNode> iter = definition.getNodes().iterator();
    for (int i = 0; i < n; i++) {
      keys[i] = Double.valueOf(i);
      values[i] = sensitivities.getEntry(i);
      final CurveNode node = iter.next();
      String name = node.getName();
      if (name == null) {
        name = node.getClass().getSimpleName() + " " + node.getResolvedMaturity().toFormattedString();
      }
      labels[i] = name;
    }
    return new DoubleLabelledMatrix1D(keys, labels, values);
  }
}
