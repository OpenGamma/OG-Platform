/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.livedata.normalization.NormalizationRuleSet;

/**
 * Gets {@link NormalizationRuleSet} by ID.  
 */
public interface NormalizationRuleResolver {
  
  /**
   * Gets {@link NormalizationRuleSet} by ID.
   * 
   * @param ruleSetId rule set ID to resolve
   * @return the rule set corresponding to the given ID. Null if not found.
   */
  NormalizationRuleSet resolve(String ruleSetId);

}
