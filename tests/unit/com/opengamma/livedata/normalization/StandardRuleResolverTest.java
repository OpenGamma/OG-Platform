/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 
 *
 * @author pietari
 */
public class StandardRuleResolverTest {
  
  @Test
  public void resolve() {
    StandardRuleResolver resolver = new StandardRuleResolver();
    
    NormalizationRuleSet rules = resolver.resolve("OpenGamma");
    assertNotNull(rules);
    
    rules = resolver.resolve("No Normalization");
    assertNotNull(rules);
    
    rules = resolver.resolve("Nonexistent");
    assertNull(rules);
  }
  
}
