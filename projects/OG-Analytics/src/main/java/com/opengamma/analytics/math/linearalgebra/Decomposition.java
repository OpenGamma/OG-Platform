/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import java.io.Serializable;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Base class for matrix decompositions (e.g. SVD, LU etc).
 * @param <S> The type of the decomposition result
 */

public abstract class Decomposition<S extends DecompositionResult>
    extends Function1D<DoubleMatrix2D, S>
    implements Serializable {

}
