/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collection;

import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class StandardRuleResolver implements NormalizationRuleResolver {
  
  private final Collection<NormalizationRuleSet> _rules;
  
  public StandardRuleResolver(Collection<NormalizationRuleSet> rules) {
    ArgumentChecker.checkNotNull(rules, "Supported rules");
    _rules = rules;
  }

  @Override
  public NormalizationRuleSet resolve(String ruleSetId) {
    ArgumentChecker.checkNotNull(ruleSetId, "Rule set ID");
    
    for (NormalizationRuleSet normalizationRuleSet : _rules) {
      if (ruleSetId.equals(normalizationRuleSet.getId())) {
        return normalizationRuleSet;        
      } 
    }
    
    return null;
  }
  
}
