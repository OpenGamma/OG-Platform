/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test PostgreSQLDbHelper.
 */
public class PostgreSQLDbHelperTest extends DbHelperTest {

  public PostgreSQLDbHelperTest() {
    _helper = PostgreSQLDbHelper.INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getName() {
    assertEquals("PostgreSQL", _helper.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("SELECT nextval('MySeq')", _helper.sqlNextSequenceValueSelect("MySeq"));
  }

  @Test
  public void test_sqlNextSequenceValueInline() {
    assertEquals("nextval('MySeq')", _helper.sqlNextSequenceValueInline("MySeq"));
  }

}
