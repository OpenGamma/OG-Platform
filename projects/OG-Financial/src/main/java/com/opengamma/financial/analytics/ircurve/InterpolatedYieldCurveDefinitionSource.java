/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * A source of yield curve definitions.
 * <p>
 * This interface provides a simple view of yield curve definitions.
 * This may be backed by a full-featured master, or by a much simpler data structure.
 */
public interface InterpolatedYieldCurveDefinitionSource extends ChangeProvider {

  /**
   * Gets a yield curve definition for a currency and name.
   * 
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @return the definition, null if not found
   */
  YieldCurveDefinition getDefinition(Currency currency, String name);

  /**
   * Gets a yield curve definition for a currency, name and version.
   * 
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param versionCorrection  the version-correction, null for latest
   * @return the definition, null if not found
   */
  YieldCurveDefinition getDefinition(Currency currency, String name, VersionCorrection versionCorrection);

}
