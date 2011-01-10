/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  public void test_getJDBCDriver() {
    assertEquals(org.postgresql.Driver.class, _helper.getJDBCDriverClass());
  }

  @Test
  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.PostgreSQLDialect.class, _helper.getHibernateDialect().getClass());
  }

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
