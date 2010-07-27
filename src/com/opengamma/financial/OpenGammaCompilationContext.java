/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 *
 * @author kirk
 */
public final class OpenGammaCompilationContext {
  /**
   * Name under which an instance of {@link InterpolatedYieldCurveDefinitionSource} will be bound
   * at runtime.
   */
  public static final String DISCOUNT_CURVE_SOURCE_NAME = "discountCurveSource";
  public static final String YIELD_CURVE_SPECIFICATION_BUILDER_NAME = "yieldCurveSpecificationBuilder"; 

  private OpenGammaCompilationContext() {
  }
  
  public static InterpolatedYieldCurveDefinitionSource getDiscountCurveSource(FunctionCompilationContext compilationContext) {
    return (InterpolatedYieldCurveDefinitionSource) compilationContext.get(DISCOUNT_CURVE_SOURCE_NAME);
  }
  
  public static InterpolatedYieldCurveSpecificationBuilder getYieldCurveSpecificationBuilder(FunctionCompilationContext compilationContext) {
    return (InterpolatedYieldCurveSpecificationBuilder) compilationContext.get(YIELD_CURVE_SPECIFICATION_BUILDER_NAME);
  }
}
