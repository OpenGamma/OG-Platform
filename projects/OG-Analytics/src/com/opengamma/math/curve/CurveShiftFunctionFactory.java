/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class CurveShiftFunctionFactory {
  /** */
  public static final ConstantCurveShiftFunction CONSTANT = new ConstantCurveShiftFunction();
  /** */
  public static final FunctionalCurveShiftFunction FUNCTIONAL = new FunctionalCurveShiftFunction();
  /** */
  public static final InterpolatedCurveShiftFunction INTERPOLATED = new InterpolatedCurveShiftFunction();
  /** */
  public static final NodalCurveShiftFunction NODAL = new NodalCurveShiftFunction();
  /** */
  public static final SpreadCurveShiftFunction SPREAD = new SpreadCurveShiftFunction();
  private static final Map<Class<?>, CurveShiftFunction<?>> s_instances = new HashMap<Class<?>, CurveShiftFunction<?>>();

  static {
    s_instances.put(ConstantCurveShiftFunction.class, CONSTANT);
    s_instances.put(FunctionalCurveShiftFunction.class, FUNCTIONAL);
    s_instances.put(InterpolatedCurveShiftFunction.class, INTERPOLATED);
    s_instances.put(NodalCurveShiftFunction.class, NODAL);
    s_instances.put(SpreadCurveShiftFunction.class, SPREAD);
  }

  public static CurveShiftFunction<?> getFunction(final Class<?> clazz) {
    final CurveShiftFunction<?> f = s_instances.get(clazz);
    if (f == null) {
      throw new IllegalArgumentException("Could not get function for " + clazz.getName());
    }
    return f;
  }
}
