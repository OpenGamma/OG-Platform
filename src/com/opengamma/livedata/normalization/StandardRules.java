/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 
 *
 * @author pietari
 */
public class StandardRules {
  
  private static final NormalizationRuleSet NO_NORMALIZATION;
  private static final NormalizationRuleSet OPENGAMMA;
  
  private static final Collection<NormalizationRuleSet> ALL;
  
  static {
    NO_NORMALIZATION = new NormalizationRuleSet("No Normalization", 
        "Raw",
        Collections.<NormalizationRule>emptyList());
    
    // TODO
    List<NormalizationRule> rules = new ArrayList<NormalizationRule>();
    OPENGAMMA = new NormalizationRuleSet("OpenGamma",
        "",
        rules);
    
    ALL = new ArrayList<NormalizationRuleSet>();
    ALL.add(NO_NORMALIZATION);
    ALL.add(OPENGAMMA);
  }
  
  public static Collection<NormalizationRuleSet> getAll() {
    return ALL; 
  }
  
  public static NormalizationRuleSet getOpenGamma() {
    return OPENGAMMA;
  }
  
  public static NormalizationRuleSet getNoNormalization() {
    return NO_NORMALIZATION;
  }
  
}
