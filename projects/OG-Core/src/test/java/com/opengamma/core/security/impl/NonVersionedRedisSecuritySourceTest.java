/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class NonVersionedRedisSecuritySourceTest extends AbstractRedisTestCase {
  
  public void addSimpleGetByUniqueId() {
    NonVersionedRedisSecuritySource source = new NonVersionedRedisSecuritySource(getJedisPool(), getRedisPrefix());
    addSimpleSecurity(source, "1");
    addSimpleSecurity(source, "2");
    
    Security security = null;
    
    security = source.get(UniqueId.of("TEST-UNQ", "1"));
    assertNotNull(security);
    assertEquals("1", security.getExternalIdBundle().getValue(ExternalScheme.of("TEST-EXT")));

    security = source.get(UniqueId.of("TEST-UNQ", "2"));
    assertNotNull(security);
    assertEquals("2", security.getExternalIdBundle().getValue(ExternalScheme.of("TEST-EXT")));

    security = source.get(UniqueId.of("TEST-UNQ", "3"));
    assertNull(security);
  }
  
  public void addSimpleGetByExternalId() {
    NonVersionedRedisSecuritySource source = new NonVersionedRedisSecuritySource(getJedisPool(), getRedisPrefix());
    addSimpleSecurity(source, "1");
    
    Security security = null;
    
    security = source.getSingle(ExternalIdBundle.of(ExternalId.of("TEST-EXT", "1")));
    assertNotNull(security);
    assertEquals(UniqueId.of("TEST-UNQ", "1", null), security.getUniqueId());
    
    security = source.getSingle(ExternalIdBundle.of(ExternalId.of("TEST-EXT", "3")));
    assertNull(security);
  }
  
  protected void addSimpleSecurity(NonVersionedRedisSecuritySource source, String key) {
    SimpleSecurity simpleSecurity = new SimpleSecurity("FAKE TYPE");
    simpleSecurity.setUniqueId(UniqueId.of("TEST-UNQ", key));
    simpleSecurity.addExternalId(ExternalId.of("TEST-EXT", key));
    simpleSecurity.addAttribute("Attribute", key);
    simpleSecurity.setName("Name - " + key);
    source.put(simpleSecurity);
  }

}
