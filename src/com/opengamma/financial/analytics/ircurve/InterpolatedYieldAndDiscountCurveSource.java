/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.financial.Currency;

/**
 * 
 *
 */
public interface InterpolatedYieldAndDiscountCurveSource {
  InterpolatedYieldAndDiscountCurveDefinition getDefinition(Currency currency, String name);
}
