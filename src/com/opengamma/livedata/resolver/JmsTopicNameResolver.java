/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * 
 *
 * @author pietari
 */
public interface JmsTopicNameResolver {
  
  /** Separator for hierarchical topic names **/
  public final static String SEPARATOR = ".";
  
  /**
   * @return Must not return null.
   * @throws IllegalArgumentException If input data is invalid
   */
  public String resolve(IdentifierBundle identifiers, NormalizationRuleSet normalizationRule);

}
