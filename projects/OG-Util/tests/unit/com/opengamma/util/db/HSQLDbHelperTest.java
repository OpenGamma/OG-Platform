/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test HSQLDbHelper.
 */
public class HSQLDbHelperTest extends DbHelperTest {

  public HSQLDbHelperTest() {
    _helper = HSQLDbHelper.INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getJDBCDriver() {
    assertEquals(org.hsqldb.jdbcDriver.class, _helper.getJDBCDriverClass());
  }

  @Test
  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.HSQLDialect.class, _helper.getHibernateDialect().getClass());
  }

  @Test
  public void test_getName() {
    assertEquals("HSQL", _helper.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("CALL NEXT VALUE FOR MySeq", _helper.sqlNextSequenceValueSelect("MySeq"));
  }

}
