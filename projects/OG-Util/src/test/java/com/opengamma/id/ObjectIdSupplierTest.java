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
 * Test {@link ObjectIdSupplier}. 
 */
@Test(groups = TestGroup.UNIT)
public class ObjectIdSupplierTest {

  public void test_basics() {
    ObjectIdSupplier test = new ObjectIdSupplier("Scheme");
    assertEquals(ObjectId.parse("Scheme~1"), test.get());
    assertEquals(ObjectId.parse("Scheme~2"), test.get());
    assertEquals(ObjectId.parse("Scheme~3"), test.get());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullScheme() {
    new ObjectIdSupplier((String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_emptyScheme() {
    new ObjectIdSupplier("");
  }

  public void test_prefix() {
    ObjectIdSupplier test = new ObjectIdSupplier("Prefixing");
    assertEquals(ObjectId.parse("Prefixing~A-1"), test.getWithValuePrefix("A-"));
    assertEquals(ObjectId.parse("Prefixing~A-2"), test.getWithValuePrefix("A-"));
    assertEquals(ObjectId.parse("Prefixing~B-3"), test.getWithValuePrefix("B-"));
  }

  public void test_toString() {
    ObjectIdSupplier test = new ObjectIdSupplier("Prefixing");
    assertEquals(true, test.toString().contains("Prefixing"));
  }

}
