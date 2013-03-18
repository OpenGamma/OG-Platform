/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;


import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimpleSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleSecurityTest {

  private static final UniqueId UID = UniqueId.of("P", "A");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");

  public void test_constructor() {
    SimpleSecurity test = new SimpleSecurity(UID, BUNDLE, "Type", "Name");
    assertEquals(UID, test.getUniqueId());
    assertEquals(BUNDLE, test.getExternalIdBundle());
    assertEquals("Type", test.getSecurityType());
    assertEquals("Name", test.getName());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullBundle() {
    new SimpleSecurity(UID, null, "Type", "Name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullType() {
    new SimpleSecurity(UID, BUNDLE, null, "Name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullName() {
    new SimpleSecurity(UID, BUNDLE, "Type", null);
  }

}
