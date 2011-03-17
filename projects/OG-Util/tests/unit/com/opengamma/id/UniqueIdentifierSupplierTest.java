/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test UniqueIdentifierSupplier. 
 */
@Test
public class UniqueIdentifierSupplierTest {

  public void test_basics() {
    UniqueIdentifierSupplier test = new UniqueIdentifierSupplier("Scheme");
    assertEquals(UniqueIdentifier.parse("Scheme::1"), test.get());
    assertEquals(UniqueIdentifier.parse("Scheme::2"), test.get());
    assertEquals(UniqueIdentifier.parse("Scheme::3"), test.get());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullScheme() {
    new UniqueIdentifierSupplier((String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_emptyScheme() {
    new UniqueIdentifierSupplier("");
  }

  public void test_prefix() {
    UniqueIdentifierSupplier test = new UniqueIdentifierSupplier("Prefixing");
    assertEquals(UniqueIdentifier.parse("Prefixing::A-1"), test.getWithValuePrefix("A-"));
    assertEquals(UniqueIdentifier.parse("Prefixing::A-2"), test.getWithValuePrefix("A-"));
    assertEquals(UniqueIdentifier.parse("Prefixing::B-3"), test.getWithValuePrefix("B-"));
  }

}
