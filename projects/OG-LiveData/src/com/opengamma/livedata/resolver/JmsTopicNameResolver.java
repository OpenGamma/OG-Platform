/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.Collection;
import java.util.Map;

import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * Produces a JMS topic name where to send market data.  
 *
 */
public interface JmsTopicNameResolver extends Resolver<JmsTopicNameResolveRequest, String> {
  
  /** Separator for hierarchical topic names **/
  String SEPARATOR = ".";
  
  /**
   * Gets a JMS topic name.
   * 
   * @param request what market data will be published  
   * @return null if the JMS topic name could not be built, a valid JMS topic name otherwise.
   */
  String resolve(JmsTopicNameResolveRequest request);
  
  /**
   * Same as calling {@link #resolve(Identifier, NormalizationRuleSet)} 
   * individually, but since it works in bulk, may be more efficient. 
   * 
   * @param requests 
   * @return map from request to result.  
   * For each input request, there must be an entry in the map.
   */
  Map<JmsTopicNameResolveRequest, String> resolve(Collection<JmsTopicNameResolveRequest> requests);

}
