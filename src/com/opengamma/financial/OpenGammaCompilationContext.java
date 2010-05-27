/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.DiscountCurveSource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 *
 * @author kirk
 */
public final class OpenGammaCompilationContext {
  /**
   * Name under which an instance of {@link DiscountCurveSource} will be bound
   * at runtime.
   */
  public static final String DISCOUNT_CURVE_SOURCE_NAME = "discountCurveSource"; 

  private OpenGammaCompilationContext() {
  }
  
  public static DiscountCurveSource getDiscountCurveSource(FunctionCompilationContext compilationContext) {
    return (DiscountCurveSource) compilationContext.get(DISCOUNT_CURVE_SOURCE_NAME);
  }
}
