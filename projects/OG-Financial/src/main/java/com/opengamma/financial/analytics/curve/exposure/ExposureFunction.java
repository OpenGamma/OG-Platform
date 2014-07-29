/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.id.ExternalId;

/**
 * A function that returns a list of implementation specific {@link ExternalId} descriptions for a given trade, which are 
 * used by a {@link ExposureFunctions} configuration object to resolve the {@link CurveConstructionConfiguration} used 
 * for pricing.
 */
public interface ExposureFunction {
  
  /** Separator */
  String SEPARATOR = "_";
  
  /** Security identifier */
  String SECURITY_IDENTIFIER = "SecurityType";
  
  /**
   * Returns the name of the exposure function implementation.
   * @return the name of the exposure function implementation.
   */
  String getName();
  
  /**
   * Returns the identifiers, specific to the implementation of the exposure function, that the exposure function will 
   * use to determine which curve construction configuration to use.
   * @param trade the trade to retrieve identifiers from.
   * @return the identifiers used to look up the curve construction configuration.
   */
  List<ExternalId> getIds(Trade trade);
}
