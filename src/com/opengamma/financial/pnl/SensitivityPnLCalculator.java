/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import java.util.Map;

import com.opengamma.financial.greeks.FirstOrder;
import com.opengamma.financial.greeks.MixedSecondOrder;
import com.opengamma.financial.greeks.Order;
import com.opengamma.financial.greeks.SecondOrder;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * @author emcleod
 *
 */
public class SensitivityPnLCalculator extends Function1D<PnLDataBundle, Double[]> {
  private final MatrixAlgebra _algebra;

  public SensitivityPnLCalculator(final MatrixAlgebra algebra) {
    if (algebra == null)
      throw new IllegalArgumentException("Matrix algebra calculator was null");
    _algebra = algebra;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double[] evaluate(final PnLDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("PnL data bundle was null");
    final Map<Underlying, DoubleMatrix1D[]> underlyings = data.getUnderlyingData();
    final Map<Sensitivity, Matrix<?>> matrices = data.getMatrices();
    final int n = data.getLength();
    final Double[] pnl = new Double[n];
    double sum;
    Sensitivity s;
    Matrix<?> m;
    Order o;
    DoubleMatrix1D v1, v2;
    for (int i = 0; i < n; i++) {
      sum = 0;
      for (final Map.Entry<Sensitivity, Matrix<?>> entry : matrices.entrySet()) {
        s = entry.getKey();
        m = entry.getValue();
        o = s.getOrder();
        if (o instanceof FirstOrder) {
          sum += _algebra.getInnerProduct(m, underlyings.get(((FirstOrder) o).getVariable())[i]);
        } else if (o instanceof SecondOrder) {
          v1 = underlyings.get(((SecondOrder) o).getVariable())[i];
          sum += 0.5 * _algebra.getInnerProduct(v1, _algebra.multiply(m, v1));
        } else if (o instanceof MixedSecondOrder) {
          v1 = underlyings.get(((MixedSecondOrder) o).getFirstVariable().getVariable())[i];
          v2 = underlyings.get(((MixedSecondOrder) o).getSecondVariable().getVariable())[i];
          sum += _algebra.getInnerProduct(v1, _algebra.multiply(m, v2));
        } else {
          throw new UnsupportedOperationException("Can only handle first, mixed-second and second order sensitivities");
        }
      }
      pnl[i] = sum;
    }
    return pnl;
  }
}
