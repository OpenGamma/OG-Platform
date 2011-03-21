/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import java.util.Collection;
import java.util.Collections;

/**
 * 
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
