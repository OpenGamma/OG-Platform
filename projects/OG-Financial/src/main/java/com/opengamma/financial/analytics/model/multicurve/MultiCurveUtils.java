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

/**
 *
 */
public class MultiCurveUtils {

  public static DoubleLabelledMatrix1D getLabelledMatrix(final DoubleMatrix1D sensitivities, final CurveDefinition definition) {
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
        name = node.getClass().getSimpleName() + " " + node.getResolvedMaturity().getPeriod();
      }
      labels[i] = name;
    }
    return new DoubleLabelledMatrix1D(keys, labels, values);
  }
}
