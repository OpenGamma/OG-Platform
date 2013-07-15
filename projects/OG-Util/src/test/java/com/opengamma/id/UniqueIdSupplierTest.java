/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link UniqueIdSupplier}. 
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdSupplierTest {

  public void test_basics() {
    UniqueIdSupplier test = new UniqueIdSupplier("Scheme");
    assertEquals(UniqueId.parse("Scheme~1"), test.get());
    assertEquals(UniqueId.parse("Scheme~2"), test.get());
    assertEquals(UniqueId.parse("Scheme~3"), test.get());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullScheme() {
    new UniqueIdSupplier((String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_emptyScheme() {
    new UniqueIdSupplier("");
  }

  public void test_prefix() {
    UniqueIdSupplier test = new UniqueIdSupplier("Prefixing");
    assertEquals(UniqueId.parse("Prefixing~A-1"), test.getWithValuePrefix("A-"));
    assertEquals(UniqueId.parse("Prefixing~A-2"), test.getWithValuePrefix("A-"));
    assertEquals(UniqueId.parse("Prefixing~B-3"), test.getWithValuePrefix("B-"));
  }

  public void test_toString() {
    UniqueIdSupplier test = new UniqueIdSupplier("Prefixing");
    assertEquals(true, test.toString().contains("Prefixing"));
  }

}
