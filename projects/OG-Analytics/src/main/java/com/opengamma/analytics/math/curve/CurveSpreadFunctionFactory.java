/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.AbstractNamedInstanceFactory;

/**
 *
 */
public final class CurveSpreadFunctionFactory extends AbstractNamedInstanceFactory<CurveSpreadFunction> {

  /**
   * Singleton instance of {@code CurveSpreadFunctionFactory}.
   */
  public static final CurveSpreadFunctionFactory INSTANCE = new CurveSpreadFunctionFactory();

  /**
   * Finds a spread function by name, ignoring case
   * @param name The name of the instance to find, not null
   * @return The instance
   */
  @FromString
  public static CurveSpreadFunction of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Adding spread function instances
   */
  private CurveSpreadFunctionFactory() {
    super(CurveSpreadFunction.class);
    addInstance(AddCurveSpreadFunction.getInstance(), AddCurveSpreadFunction.NAME, "add", "plus");
    addInstance(DivideCurveSpreadFunction.getInstance(), DivideCurveSpreadFunction.NAME, "divide");
    addInstance(MultiplyCurveSpreadFunction.getInstance(), MultiplyCurveSpreadFunction.NAME, "multiply", "times");
    addInstance(SubtractCurveSpreadFunction.getInstance(), SubtractCurveSpreadFunction.NAME, "subtract");
  }
}
