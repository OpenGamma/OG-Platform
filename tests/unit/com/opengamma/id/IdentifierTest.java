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

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;

/**
 * A pure unit test for {@link Identifier}. 
 *
 * @author kirk
 */
public class IdentifierTest {
  
  @Test(expected=NullPointerException.class)
  public void noDomainConstruction() {
    new Identifier((IdentificationScheme)null, "foo");
  }

  @Test(expected=NullPointerException.class)
  public void noDomainNameConstruction() {
    new Identifier((String)null, "foo");
  }

  @Test(expected=NullPointerException.class)
  public void noValueConstruction() {
    new Identifier(new IdentificationScheme("Bloomberg"), null);
  }
  
  @Test
  public void domainNameOnlyConstruction() {
    assertEquals(new IdentificationScheme("Bloomberg"), (new Identifier("Bloomberg", "foo")).getDomain());
  }
  
  @Test
  public void equality() {
    IdentificationScheme d1 = new IdentificationScheme("d1");
    IdentificationScheme d2 = new IdentificationScheme("d2");
    
    assertTrue(new Identifier(d1, "v1").equals(new Identifier(d1, "v1")));
    assertFalse(new Identifier(d1, "v1").equals(new Identifier(d1, "v2")));
    assertFalse(new Identifier(d1, "v1").equals(new Identifier(d2, "v1")));
  }
  
  @Test
  public void hashing() {
    IdentificationScheme d1 = new IdentificationScheme("d1");
    IdentificationScheme d2 = new IdentificationScheme("d2");
    
    assertTrue(new Identifier(d1, "v1").hashCode() == new Identifier(d1, "v1").hashCode());
    assertFalse(new Identifier(d1, "v1").hashCode() == new Identifier(d1, "v2").hashCode());
    assertFalse(new Identifier(d1, "v1").hashCode() == new Identifier(d2, "v1").hashCode());
  }
  
  @Test
  public void cloning() {
    Identifier id = new Identifier(new IdentificationScheme("domain"), "value");
    assertEquals(id, id.clone());
    assertNotSame(id, id.clone());
  }

}
