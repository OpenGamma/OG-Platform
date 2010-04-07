/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class StandardRuleResolver implements NormalizationRuleResolver {

  @Override
  public NormalizationRuleSet resolve(String ruleSetId) {
    ArgumentChecker.checkNotNull(ruleSetId, "Rule set ID");
    
    for (NormalizationRuleSet normalizationRuleSet : StandardRules.getAll()) {
      if (ruleSetId.equals(normalizationRuleSet.getId())) {
        return normalizationRuleSet;        
      } 
    }
    
    return null;
  }
  
}
