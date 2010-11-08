/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * Produces a JMS topic name where to send market data.  
 *
 */
public interface JmsTopicNameResolver {
  
  /** Separator for hierarchical topic names **/
  String SEPARATOR = ".";
  
  /**
   * Gets a JMS topic name.
   * 
   * @param marketDataUniqueId what market data the server is going to publish
   * @param normalizationRule what normalization rule will be applied to the raw market data
   * @return must not return null.
   * @throws IllegalArgumentException if input data is invalid
   */
  String resolve(Identifier marketDataUniqueId, NormalizationRuleSet normalizationRule);

}
