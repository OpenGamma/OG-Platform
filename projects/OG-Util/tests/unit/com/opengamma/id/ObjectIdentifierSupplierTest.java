/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test ObjectIdentifierSupplier. 
 */
@Test
public class ObjectIdentifierSupplierTest {

  public void test_basics() {
    ObjectIdentifierSupplier test = new ObjectIdentifierSupplier("Scheme");
    assertEquals(ObjectIdentifier.parse("Scheme::1"), test.get());
    assertEquals(ObjectIdentifier.parse("Scheme::2"), test.get());
    assertEquals(ObjectIdentifier.parse("Scheme::3"), test.get());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullScheme() {
    new ObjectIdentifierSupplier((String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_emptyScheme() {
    new ObjectIdentifierSupplier("");
  }

  public void test_prefix() {
    ObjectIdentifierSupplier test = new ObjectIdentifierSupplier("Prefixing");
    assertEquals(ObjectIdentifier.parse("Prefixing::A-1"), test.getWithValuePrefix("A-"));
    assertEquals(ObjectIdentifier.parse("Prefixing::A-2"), test.getWithValuePrefix("A-"));
    assertEquals(ObjectIdentifier.parse("Prefixing::B-3"), test.getWithValuePrefix("B-"));
  }

}
