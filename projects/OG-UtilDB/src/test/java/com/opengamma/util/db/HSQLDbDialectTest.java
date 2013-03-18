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
public class HSQLDbDialectTest extends DbDialectTest {

  public HSQLDbDialectTest() {
    _dialect = HSQLDbDialect.INSTANCE;
  }

  //-------------------------------------------------------------------------
  public void test_getJDBCDriver() {
    assertEquals(org.hsqldb.jdbcDriver.class, _dialect.getJDBCDriverClass());
  }

  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.HSQLDialect.class, _dialect.getHibernateDialect().getClass());
  }

  public void test_getName() {
    assertEquals("HSQL", _dialect.getName());
  }

  //-------------------------------------------------------------------------
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("CALL NEXT VALUE FOR MySeq", _dialect.sqlNextSequenceValueSelect("MySeq"));
  }
  
  //-------------------------------------------------------------------------
  public void test_sqlWildcardQuery() {
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a*"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a?"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a*b"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a?b"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", _dialect.sqlWildcardQuery("AND col ", ":arg", "*b"));
    assertEquals("AND col LIKE :arg ESCAPE '\\' ", _dialect.sqlWildcardQuery("AND col ", ":arg", "?b"));
    
    assertEquals("AND col = :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a"));
    assertEquals("AND col = :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", ""));
    assertEquals("", _dialect.sqlWildcardQuery("AND col ", ":arg", null));
  }

}
