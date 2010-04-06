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
 * @author pietari
 */
public interface NormalizationRuleResolver {
  
  /**
   * @return The rule set corresponding to the given ID. Null if not found.
   */
  public NormalizationRuleSet resolve(String ruleSetId);

}
