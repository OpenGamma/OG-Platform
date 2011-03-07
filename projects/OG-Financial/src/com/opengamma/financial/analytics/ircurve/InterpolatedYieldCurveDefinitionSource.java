/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.InstantProvider;

import com.opengamma.core.common.CurrencyUnit;

/**
 * A source of yield curve definitions.
 * <p>
 * This interface provides a simple view of yield curve definitions.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface InterpolatedYieldCurveDefinitionSource {

  /**
   * Gets a yield curve definition for a currency and name.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @return the definition, null if not found
   */
  YieldCurveDefinition getDefinition(CurrencyUnit currency, String name);

  /**
   * Gets a yield curve definition for a currency, name and version.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param version  the version instant, not null
   * @return the definition, null if not found
   */
  YieldCurveDefinition getDefinition(CurrencyUnit currency, String name, InstantProvider version);

}
