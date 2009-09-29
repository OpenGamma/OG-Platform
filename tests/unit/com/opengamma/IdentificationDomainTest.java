/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.IdentificationDomain;

/**
 * A pure unit test for {@link IdentificationDomain}.
 *
 * @author kirk
 */
public class IdentificationDomainTest {

  @Test
  public void comparisons() {
    IdentificationDomain d1 = new IdentificationDomain("d1");
    IdentificationDomain d2 = new IdentificationDomain("d2");
    
    assertTrue(d1.equals(d1));
    assertFalse(d1.equals("d1"));
    assertFalse(d1.equals(d2));
    d2 = new IdentificationDomain(d1.getDomainName());
    assertTrue(d1.equals(d2));
    assertEquals(d1.hashCode(), d2.hashCode());
    
    assertEquals(d1, d1.clone());
    assertNotSame(d1, d1.clone());
  }
  
  @Test(expected=NullPointerException.class)
  public void noNameProvided() {
    new IdentificationDomain(null);
  }
}
