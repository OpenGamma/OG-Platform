/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.IdentificationDomain;

/**
 * A pure unit test for {@link SecurityIdentifier}. 
 *
 * @author kirk
 */
public class SecurityIdentifierTest {
  
  @Test(expected=NullPointerException.class)
  public void noDomainConstruction() {
    new SecurityIdentifier(null, "foo");
  }

  @Test(expected=NullPointerException.class)
  public void noValueConstruction() {
    new SecurityIdentifier(new IdentificationDomain("Bloomberg"), null);
  }
  
  @Test
  public void equality() {
    IdentificationDomain d1 = new IdentificationDomain("d1");
    IdentificationDomain d2 = new IdentificationDomain("d2");
    
    assertTrue(new SecurityIdentifier(d1, "v1").equals(new SecurityIdentifier(d1, "v1")));
    assertFalse(new SecurityIdentifier(d1, "v1").equals(new SecurityIdentifier(d1, "v2")));
    assertFalse(new SecurityIdentifier(d1, "v1").equals(new SecurityIdentifier(d2, "v1")));
  }
  
  @Test
  public void hashing() {
    IdentificationDomain d1 = new IdentificationDomain("d1");
    IdentificationDomain d2 = new IdentificationDomain("d2");
    
    assertTrue(new SecurityIdentifier(d1, "v1").hashCode() == new SecurityIdentifier(d1, "v1").hashCode());
    assertFalse(new SecurityIdentifier(d1, "v1").hashCode() == new SecurityIdentifier(d1, "v2").hashCode());
    assertFalse(new SecurityIdentifier(d1, "v1").hashCode() == new SecurityIdentifier(d2, "v1").hashCode());
  }
  
  @Test
  public void cloning() {
    SecurityIdentifier id = new SecurityIdentifier(new IdentificationDomain("domain"), "value");
    assertEquals(id, id.clone());
    assertNotSame(id, id.clone());
  }

}
