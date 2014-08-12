/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Abstraction for the vector function $f: \mathbb{R}^m \to \mathbb{R}^n \quad x \mapsto f(x)$ where the 
 * Jacobian $j : \mathbb{R}^m \to \mathbb{R}^{n\times m} \quad x \mapsto j(x)$ is also provided 
 */
public abstract class VectorFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * Calculate the Jacobian at a point $\mathbf{x}$. For a function 
   * $f: \mathbb{R}^m \to \mathbb{R}^n \quad x \mapsto f(x)$, the Jacobain is a n by m matrix
   * @param x The input vector $\mathbf{x}$
   * @return The Jacobian $\mathbf{J}$
   */
  public abstract DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x);

  /**
   * The size of the input vector $\mathbf{x}$
   * @return size of domain 
   */
  public abstract int getSizeOfDomain();

  /**
   * The size of the output vector $\mathbf{y}$
   * @return size of range 
   */
  public abstract int getSizeOfRange();
}
