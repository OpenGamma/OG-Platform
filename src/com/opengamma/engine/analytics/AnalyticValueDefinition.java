/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.io.Serializable;
import java.util.Set;


/**
 * Allows specification of the types of data that an {@link AnalyticFunction} can
 * produce.
 * Examples of concrete {@code AnalyticValueDefinition} instances might include:
 * <ul>
 *   <li>The last price for CSCO on the Nasdaq exchange.</li>
 *   <li>The per-unit delta for a particular option on CSCO.</li>
 *   <li>The yield curve for USD.</li>
 *   <li>The yield curve for USD using a specified set of strips using a particular
 *       bootstrapping algorithm and a particular interpolation algorithm.</li>
 * </ul>
 *
 * @author kirk
 */
public interface AnalyticValueDefinition extends Serializable {
  Set<String> getKeys();
  
  Set<Object> getValues(String key);
  
  Object getValue(String key);
}
