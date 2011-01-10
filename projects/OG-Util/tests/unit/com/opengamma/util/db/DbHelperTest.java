/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test DbUtils.
 */
public class DbHelperTest {

  protected DbHelper _helper = new MockDbHelper();

  //-------------------------------------------------------------------------
  @Test
  public void test_isWildcard() {
    assertEquals(true, _helper.isWildcard("a*"));
    assertEquals(true, _helper.isWildcard("a?"));
    assertEquals(true, _helper.isWildcard("a*b"));
    assertEquals(true, _helper.isWildcard("a?b"));
    assertEquals(true, _helper.isWildcard("*b"));
    assertEquals(true, _helper.isWildcard("?b"));
    
    assertEquals(false, _helper.isWildcard("a"));
    assertEquals(false, _helper.isWildcard(""));
    assertEquals(false, _helper.isWildcard(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlWildcardOperator() {
    assertEquals("LIKE", _helper.sqlWildcardOperator("a*"));
    assertEquals("LIKE", _helper.sqlWildcardOperator("a?"));
    assertEquals("LIKE", _helper.sqlWildcardOperator("a*b"));
    assertEquals("LIKE", _helper.sqlWildcardOperator("a?b"));
    assertEquals("LIKE", _helper.sqlWildcardOperator("*b"));
    assertEquals("LIKE", _helper.sqlWildcardOperator("?b"));
    
    assertEquals("=", _helper.sqlWildcardOperator("a"));
    assertEquals("=", _helper.sqlWildcardOperator(""));
    assertEquals("=", _helper.sqlWildcardOperator(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlWildcardAdjustValue() {
    assertEquals("a%", _helper.sqlWildcardAdjustValue("a*"));
    assertEquals("a_", _helper.sqlWildcardAdjustValue("a?"));
    assertEquals("a%b", _helper.sqlWildcardAdjustValue("a*b"));
    assertEquals("a_b", _helper.sqlWildcardAdjustValue("a?b"));
    assertEquals("%b", _helper.sqlWildcardAdjustValue("*b"));
    assertEquals("_b", _helper.sqlWildcardAdjustValue("?b"));
    
    assertEquals("a", _helper.sqlWildcardAdjustValue("a"));
    assertEquals("", _helper.sqlWildcardAdjustValue(""));
    assertEquals(null, _helper.sqlWildcardAdjustValue(null));
    
    assertEquals("a%b\\%c", _helper.sqlWildcardAdjustValue("a*b%c"));
    assertEquals("a_b\\_c", _helper.sqlWildcardAdjustValue("a?b_c"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlWildcardQuery() {
    assertEquals("AND col LIKE :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "a*"));
    assertEquals("AND col LIKE :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "a?"));
    assertEquals("AND col LIKE :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "a*b"));
    assertEquals("AND col LIKE :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "a?b"));
    assertEquals("AND col LIKE :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "*b"));
    assertEquals("AND col LIKE :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "?b"));
    
    assertEquals("AND col = :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", "a"));
    assertEquals("AND col = :arg ", _helper.sqlWildcardQuery("AND col ", ":arg", ""));
    assertEquals("", _helper.sqlWildcardQuery("AND col ", ":arg", null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlApplyPaging_noPaging() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo ",
        _helper.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", null));
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo ",
        _helper.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", PagingRequest.ALL));
  }

  @Test
  public void test_sqlApplyPaging_limit() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo FETCH FIRST 20 ROWS ONLY ",
        _helper.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", new PagingRequest(1, 20)));
  }

  @Test
  public void test_sqlApplyPaging_offsetLimit() {
    assertEquals(
        "SELECT foo FROM bar WHERE TRUE ORDER BY foo OFFSET 40 ROWS FETCH NEXT 20 ROWS ONLY ",
        _helper.sqlApplyPaging("SELECT foo FROM bar WHERE TRUE ", "ORDER BY foo ", new PagingRequest(3, 20)));
  }

}
