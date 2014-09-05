/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class Oracle11gDbDialectTest extends DbDialectTest {

  public Oracle11gDbDialectTest() {
    _dialect = Oracle11gDbDialect.INSTANCE;
  }

  //-------------------------------------------------------------------------
  public void test_toDatabaseString() {
    assertEquals(null, _dialect.toDatabaseString(null));
    assertEquals(" ", _dialect.toDatabaseString(""));
    assertEquals("   ", _dialect.toDatabaseString("  "));
    assertEquals("A", _dialect.toDatabaseString("A"));
  }

  public void test_fromDatabaseString() {
    assertEquals(null, _dialect.fromDatabaseString(null));
    assertEquals("", _dialect.fromDatabaseString(""));
    assertEquals("", _dialect.fromDatabaseString(" "));
    assertEquals("  ", _dialect.fromDatabaseString("   "));
    assertEquals("A", _dialect.fromDatabaseString("A"));
  }

}
