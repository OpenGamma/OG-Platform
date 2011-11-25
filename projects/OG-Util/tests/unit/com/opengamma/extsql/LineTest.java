/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.extsql.ExtSqlParser.Line;

/**
 * Test.
 */
@Test
public class LineTest {

  public void test_simple() {
    Line line = new Line("LINE", 1);
    assertEquals("LINE", line.line());
    assertEquals("LINE", line.lineTrimmed());
    assertEquals(1, line.lineNumber());
    assertEquals(false, line.containsTab());
    assertEquals(false, line.isComment());
    assertEquals(0, line.indent());
  }

  public void test_simple_indent() {
    Line line = new Line("  LINE", 2);
    assertEquals("  LINE", line.line());
    assertEquals("LINE", line.lineTrimmed());
    assertEquals(2, line.lineNumber());
    assertEquals(false, line.containsTab());
    assertEquals(false, line.isComment());
    assertEquals(2, line.indent());
  }

  public void test_comment() {
    Line line = new Line("--", 1);
    assertEquals("--", line.line());
    assertEquals("--", line.lineTrimmed());
    assertEquals(1, line.lineNumber());
    assertEquals(false, line.containsTab());
    assertEquals(true, line.isComment());
    assertEquals(0, line.indent());
  }

  public void test_comment_indent() {
    Line line = new Line("  -- comment", 2);
    assertEquals("  -- comment", line.line());
    assertEquals("-- comment", line.lineTrimmed());
    assertEquals(2, line.lineNumber());
    assertEquals(false, line.containsTab());
    assertEquals(true, line.isComment());
    assertEquals(2, line.indent());
  }

  public void test_tab() {
    Line line = new Line("\t@ADD(:Test)", 2);
    assertEquals("\t@ADD(:Test)", line.line());
    assertEquals("@ADD(:Test)", line.lineTrimmed());
    assertEquals(2, line.lineNumber());
    assertEquals(true, line.containsTab());
    assertEquals(false, line.isComment());
    assertEquals(0, line.indent());
  }

}
