/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * A pure unit test for {@link SecurityIdentificationDomain}. In general,
 * I wouldn't bother doing a test at this level, but this serves as a dummy
 * test case so that I can delete the old dummy classes when this repository
 * was setup.
 *
 * @author kirk
 */
public class SecurityIdentificationDomainTest {

  @Test
  public void comparisons() {
    SecurityIdentificationDomain d1 = new SecurityIdentificationDomain("d1");
    SecurityIdentificationDomain d2 = new SecurityIdentificationDomain("d2");
    
    assertTrue(d1.equals(d1));
    assertFalse(d1.equals("d1"));
    assertFalse(d1.equals(d2));
    d2 = new SecurityIdentificationDomain(d1.getDomainName());
    assertTrue(d1.equals(d2));
    assertEquals(d1.hashCode(), d2.hashCode());
  }
  
  @Test(expected=NullPointerException.class)
  public void noNameProvided() {
    new SecurityIdentificationDomain(null);
  }
}
