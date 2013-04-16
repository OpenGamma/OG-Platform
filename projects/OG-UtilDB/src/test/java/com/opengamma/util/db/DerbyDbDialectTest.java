/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class DerbyDbDialectTest extends DbDialectTest {

  public DerbyDbDialectTest() {
    _dialect = DerbyDbDialect.INSTANCE;
  }

  //-------------------------------------------------------------------------
  public void test_getJDBCDriver() {
    assertEquals(org.apache.derby.jdbc.EmbeddedDriver.class, _dialect.getJDBCDriverClass());
  }

  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.DerbyDialect.class, _dialect.getHibernateDialect().getClass());
  }

  public void test_getName() {
    assertEquals("Derby", _dialect.getName());
  }

  //-------------------------------------------------------------------------
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("SELECT NEXT VALUE FOR MySeq FROM sysibm.sysdummy1", _dialect.sqlNextSequenceValueSelect("MySeq"));
  }

}
