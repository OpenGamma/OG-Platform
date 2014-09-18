/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbDialect.
 */
@Test(groups = TestGroup.UNIT)
public class DbDialectTest {

  protected DbDialect _dialect = new MockDbDialect();

  //-------------------------------------------------------------------------
  public void test_isWildcard() {
    assertEquals(true, _dialect.isWildcard("a*"));
    assertEquals(true, _dialect.isWildcard("a?"));
    assertEquals(true, _dialect.isWildcard("a*b"));
    assertEquals(true, _dialect.isWildcard("a?b"));
    assertEquals(true, _dialect.isWildcard("*b"));
    assertEquals(true, _dialect.isWildcard("?b"));
    
    assertEquals(false, _dialect.isWildcard("a"));
    assertEquals(false, _dialect.isWildcard(""));
    assertEquals(false, _dialect.isWildcard(null));
  }

  //-------------------------------------------------------------------------
  public void test_sqlWildcardOperator() {
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a*"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a?"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a*b"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("a?b"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("*b"));
    assertEquals("LIKE", _dialect.sqlWildcardOperator("?b"));
    
    assertEquals("=", _dialect.sqlWildcardOperator("a"));
    assertEquals("=", _dialect.sqlWildcardOperator(""));
    assertEquals("=", _dialect.sqlWildcardOperator(null));
  }

  //-------------------------------------------------------------------------
  public void test_sqlWildcardAdjustValue() {
    assertEquals("a%", _dialect.sqlWildcardAdjustValue("a*"));
    assertEquals("a_", _dialect.sqlWildcardAdjustValue("a?"));
    assertEquals("a%b", _dialect.sqlWildcardAdjustValue("a*b"));
    assertEquals("a_b", _dialect.sqlWildcardAdjustValue("a?b"));
    assertEquals("%b", _dialect.sqlWildcardAdjustValue("*b"));
    assertEquals("_b", _dialect.sqlWildcardAdjustValue("?b"));
    
    assertEquals("a", _dialect.sqlWildcardAdjustValue("a"));
    assertEquals("", _dialect.sqlWildcardAdjustValue(""));
    assertEquals(null, _dialect.sqlWildcardAdjustValue(null));
    
    assertEquals("a%b\\%c", _dialect.sqlWildcardAdjustValue("a*b%c"));
    assertEquals("a_b\\_c", _dialect.sqlWildcardAdjustValue("a?b_c"));
  }

  //-------------------------------------------------------------------------
  public void test_sqlWildcardQuery() {
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a*"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a?"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a*b"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a?b"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "*b"));
    assertEquals("AND col LIKE :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "?b"));
    
    assertEquals("AND col = :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", "a"));
    assertEquals("AND col = :arg ", _dialect.sqlWildcardQuery("AND col ", ":arg", ""));
    assertEquals("", _dialect.sqlWildcardQuery("AND col ", ":arg", null));
  }

  //-------------------------------------------------------------------------
  public void test_toDatabaseString() {
    assertEquals(null, _dialect.toDatabaseString(null));
    assertEquals("", _dialect.toDatabaseString(""));
    assertEquals("  ", _dialect.toDatabaseString("  "));
    assertEquals("A", _dialect.toDatabaseString("A"));
  }

  public void test_fromDatabaseString() {
    assertEquals(null, _dialect.fromDatabaseString(null));
    assertEquals("", _dialect.fromDatabaseString(""));
    assertEquals("  ", _dialect.fromDatabaseString("  "));
    assertEquals("A", _dialect.fromDatabaseString("A"));
  }

  //-------------------------------------------------------------------------
  public void test_sqlApplyPaging_noPaging() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", null));
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ALL));
  }

  public void test_sqlApplyPaging_limit() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo FETCH FIRST 20 ROWS ONLY ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ofPage(1, 20)));
  }

  public void test_sqlApplyPaging_offsetLimit() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo OFFSET 40 ROWS FETCH NEXT 20 ROWS ONLY ",
        _dialect.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ofPage(3, 20)));
  }

  //-------------------------------------------------------------------------
  public void test_sqlNullDefault() {
    assertEquals("COALESCE(a, b)", _dialect.sqlNullDefault("a", "b"));
  }

}
