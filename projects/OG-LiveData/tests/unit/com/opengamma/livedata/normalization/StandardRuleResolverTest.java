/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

/**
 * 
 *
 * @author pietari
 */
public class StandardRuleResolverTest {
  
  @Test
  public void resolve() {
    Collection<NormalizationRuleSet> supportedRules = Collections.singleton(StandardRules.getNoNormalization());    
    StandardRuleResolver resolver = new StandardRuleResolver(supportedRules);
    
    NormalizationRuleSet rules = resolver.resolve("No Normalization");
    assertNotNull(rules);
    
    rules = resolver.resolve("Nonexistent");
    assertNull(rules);
  }
  
}
