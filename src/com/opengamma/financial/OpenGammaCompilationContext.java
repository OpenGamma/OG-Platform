/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.DiscountCurveDefinition;
import com.opengamma.financial.analytics.ircurve.DiscountCurveSource;

/**
 * Utility methods to pull standard objects out of a {@link FunctionCompilationContext}.
 *
 * @author kirk
 */
public final class OpenGammaCompilationContext {
  //public static final string 

  private OpenGammaCompilationContext() {
  }

  public static DiscountCurveDefinition getDiscountCurveDefinition(FunctionCompilationContext compilationContext, Currency currency, String name) {
    DiscountCurveSource curveSource = (DiscountCurveSource) compilationContext.get("discountCurveSource");
    return curveSource.getDefinition(currency, name);
  }
}
