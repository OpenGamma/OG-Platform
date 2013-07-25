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
 * Test {@link VersionedUniqueIdSupplier}. 
 */
@Test(groups = TestGroup.UNIT)
public class VersionedUniqueIdSupplierTest {

  public void test_constructor_ObjectId() {
    VersionedUniqueIdSupplier supplier = new VersionedUniqueIdSupplier(ObjectId.of("A", "B"));
    UniqueId test1 = supplier.get();
    UniqueId test2 = supplier.get();
    UniqueId test3 = supplier.get();
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test1.equals(test3));
    assertEquals(ObjectId.of("A", "B"), test1.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test2.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test3.getObjectId());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_ObjectId_null() {
    new VersionedUniqueIdSupplier((ObjectId) null);
  }

  //-------------------------------------------------------------------------
  public void test_constructor_StringString() {
    VersionedUniqueIdSupplier supplier = new VersionedUniqueIdSupplier("A", "B");
    UniqueId test1 = supplier.get();
    UniqueId test2 = supplier.get();
    UniqueId test3 = supplier.get();
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test1.equals(test3));
    assertEquals(ObjectId.of("A", "B"), test1.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test2.getObjectId());
    assertEquals(ObjectId.of("A", "B"), test3.getObjectId());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_StringString_nullScheme() {
    new VersionedUniqueIdSupplier(null, "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_StringString_nullValue() {
    new VersionedUniqueIdSupplier("A", null);
  }

}
