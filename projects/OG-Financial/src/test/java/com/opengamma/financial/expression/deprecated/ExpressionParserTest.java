/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression.deprecated;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.expression.UserExpression;

/**
 * Test the {@link ExpressionParser} class.
 */
public class ExpressionParserTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ExpressionParserTest.class);

  private static void parse(final String str, final String expected) {
    s_logger.debug("Parsing {}", str);
    final UserExpression expr = new ExpressionParser().parse(str);
    assertNotNull(expr);
    assertEquals(expr.toString(), expected);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testEmptyString() {
    final UserExpression expr = new ExpressionParser().parse("");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testIllegalString() {
    final UserExpression expr = new ExpressionParser().parse("Foo less \"Bar\"");
  }

  @Test
  public void testAttributeEquality() {
    parse("Foo=\"Bar\"", "`EQ (`IDENTIFIER Foo) (`LITERAL Bar)");
  }

  @Test
  public void testAttributeLessThan() {
    parse("Foo<\"B\\\"ar\"", "`LT (`IDENTIFIER Foo) (`LITERAL B\"ar)");
  }

  @Test
  public void testAttributeGreaterThan() {
    parse("Foo>\"Bar\"", "`GT (`IDENTIFIER Foo) (`LITERAL Bar)");
  }

  @Test
  public void testAttributeNotEqual() {
    parse("Foo<>\"Bar\"", "`NEQ (`IDENTIFIER Foo) (`LITERAL Bar)");
  }

  @Test
  public void testNot() {
    parse("NOT Foo=\"Bar\"", "`NOT (`EQ (`IDENTIFIER Foo) (`LITERAL Bar))");
  }

  @Test
  public void testAnd() {
    parse("Foo=\"Bar\" AND Bar=\"Foo\" AND Cow=\"Foo\"", "`AND (`EQ (`IDENTIFIER Foo) (`LITERAL Bar)) (`AND (`EQ (`IDENTIFIER Bar) (`LITERAL Foo)) (`EQ (`IDENTIFIER Cow) (`LITERAL Foo)))");
  }

  @Test
  public void testOr() {
    parse("Foo=\"Bar\" OR Foo=\"Cow\" OR Bar=\"Cow\"", "`OR (`EQ (`IDENTIFIER Foo) (`LITERAL Bar)) (`OR (`EQ (`IDENTIFIER Foo) (`LITERAL Cow)) (`EQ (`IDENTIFIER Bar) (`LITERAL Cow)))");
  }

  @Test
  public void testBrackets() {
    parse("(((Foo=\"Bar\") AND (Bar=\"Foo\")) OR ((Foo=\"Bar\") AND (Bar=\"Cow\")))",
        "`OR (`AND (`EQ (`IDENTIFIER Foo) (`LITERAL Bar)) (`EQ (`IDENTIFIER Bar) (`LITERAL Foo))) (`AND (`EQ (`IDENTIFIER Foo) (`LITERAL Bar)) (`EQ (`IDENTIFIER Bar) (`LITERAL Cow)))");
  }

}
