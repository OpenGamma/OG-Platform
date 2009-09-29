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

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * A pure unit test for {@link DomainSpecificIdentifier}. 
 *
 * @author kirk
 */
public class DomainSpecificIdentifierTest {
  
  @Test(expected=NullPointerException.class)
  public void noDomainConstruction() {
    new DomainSpecificIdentifier(null, "foo");
  }

  @Test(expected=NullPointerException.class)
  public void noValueConstruction() {
    new DomainSpecificIdentifier(new IdentificationDomain("Bloomberg"), null);
  }
  
  @Test
  public void equality() {
    IdentificationDomain d1 = new IdentificationDomain("d1");
    IdentificationDomain d2 = new IdentificationDomain("d2");
    
    assertTrue(new DomainSpecificIdentifier(d1, "v1").equals(new DomainSpecificIdentifier(d1, "v1")));
    assertFalse(new DomainSpecificIdentifier(d1, "v1").equals(new DomainSpecificIdentifier(d1, "v2")));
    assertFalse(new DomainSpecificIdentifier(d1, "v1").equals(new DomainSpecificIdentifier(d2, "v1")));
  }
  
  @Test
  public void hashing() {
    IdentificationDomain d1 = new IdentificationDomain("d1");
    IdentificationDomain d2 = new IdentificationDomain("d2");
    
    assertTrue(new DomainSpecificIdentifier(d1, "v1").hashCode() == new DomainSpecificIdentifier(d1, "v1").hashCode());
    assertFalse(new DomainSpecificIdentifier(d1, "v1").hashCode() == new DomainSpecificIdentifier(d1, "v2").hashCode());
    assertFalse(new DomainSpecificIdentifier(d1, "v1").hashCode() == new DomainSpecificIdentifier(d2, "v1").hashCode());
  }
  
  @Test
  public void cloning() {
    DomainSpecificIdentifier id = new DomainSpecificIdentifier(new IdentificationDomain("domain"), "value");
    assertEquals(id, id.clone());
    assertNotSame(id, id.clone());
  }

}
