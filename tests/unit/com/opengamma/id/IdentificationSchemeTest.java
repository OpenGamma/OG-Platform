/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.id.IdentificationScheme;

/**
 * A pure unit test for {@link IdentificationScheme}.
 *
 * @author kirk
 */
public class IdentificationSchemeTest {

  @Test
  public void comparisons() {
    IdentificationScheme d1 = new IdentificationScheme("d1");
    IdentificationScheme d2 = new IdentificationScheme("d2");
    
    assertTrue(d1.equals(d1));
    assertFalse(d1.equals("d1"));
    assertFalse(d1.equals(d2));
    d2 = new IdentificationScheme(d1.getDomainName());
    assertTrue(d1.equals(d2));
    assertEquals(d1.hashCode(), d2.hashCode());
    
    assertEquals(d1, d1.clone());
    assertNotSame(d1, d1.clone());
  }
  
  @Test(expected=NullPointerException.class)
  public void noNameProvided() {
    new IdentificationScheme(null);
  }
}
