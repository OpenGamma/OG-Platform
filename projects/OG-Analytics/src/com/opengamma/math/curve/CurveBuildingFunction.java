/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.LinkedHashMap;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public abstract class CurveBuildingFunction<T> extends Function1D<DoubleMatrix1D, LinkedHashMap<String, Curve<Double, T>>> {

}
