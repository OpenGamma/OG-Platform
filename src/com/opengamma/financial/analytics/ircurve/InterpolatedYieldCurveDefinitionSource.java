/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.Instant;

import com.opengamma.financial.Currency;

/**
 * 
 *
 */
public interface InterpolatedYieldCurveDefinitionSource {
  YieldCurveDefinition getDefinition(Currency currency, String name);
  YieldCurveDefinition getDefinition(Currency currency, String name, Instant version);
}
