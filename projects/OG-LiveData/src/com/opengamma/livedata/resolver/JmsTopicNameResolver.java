/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * 
 *
 */
public interface JmsTopicNameResolver {
  
  /** Separator for hierarchical topic names **/
  String SEPARATOR = ".";
  
  /**
   * @param marketDataUniqueId What market data the server is going to publish
   * @param normalizationRule What normalization rule will be applied to the raw market data
   * @return Must not return null.
   * @throws IllegalArgumentException If input data is invalid
   */
  String resolve(Identifier marketDataUniqueId, NormalizationRuleSet normalizationRule);

}
