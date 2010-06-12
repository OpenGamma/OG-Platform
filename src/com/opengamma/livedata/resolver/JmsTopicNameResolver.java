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
 * @author pietari
 */
public interface JmsTopicNameResolver {
  
  /** Separator for hierarchical topic names **/
  static final String SEPARATOR = ".";
  
  /**
   * @return Must not return null.
   * @throws IllegalArgumentException If input data is invalid
   */
  String resolve(Identifier marketDataUniqueId, NormalizationRuleSet normalizationRule);

}
