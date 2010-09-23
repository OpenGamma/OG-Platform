/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * 
 *
 */
public interface NormalizationRuleResolver {
  
  /**
   * @param ruleSetId Rule set ID to resolve
   * @return The rule set corresponding to the given ID. Null if not found.
   */
  NormalizationRuleSet resolve(String ruleSetId);

}
