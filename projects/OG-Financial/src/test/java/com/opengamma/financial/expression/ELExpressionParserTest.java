/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.google.common.base.Function;

/**
 * Tests the {@link ELExpressionParser} class.
 */
@Test
public class ELExpressionParserTest {

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidExpression() {
    final ELExpressionParser parser = new ELExpressionParser();
    parser.parse("Not a valid expression");
  }

  public void testSingleExpr() {
    final ELExpressionParser parser = new ELExpressionParser();
    final UserExpression expr = parser.parse("x * 0.9");
    UserExpression.Evaluator eval = expr.evaluator();
    eval.setVariable("x", 42d);
    assertEquals(eval.evaluate(), 42d * 0.9);
    eval = expr.evaluator();
    assertEquals(eval.evaluate(), UserExpression.NA);
  }

  public void testCompositeExpr() {
    final ELExpressionParser parser = new ELExpressionParser();
    final UserExpression expr = parser.parse("x * 0.9; y + 0.5");
    UserExpression.Evaluator eval = expr.evaluator();
    eval.setVariable("x", 42d);
    assertEquals(eval.evaluate(), 42d * 0.9);
    eval = expr.evaluator();
    eval.setVariable("y", 42d);
    assertEquals(eval.evaluate(), 42d + 0.5);
    eval = expr.evaluator();
    assertEquals(eval.evaluate(), UserExpression.NA);
  }

  public void testConditional() {
    final ELExpressionParser parser = new ELExpressionParser();
    final UserExpression expr = parser.parse("if (b) 0.42");
    UserExpression.Evaluator eval = expr.evaluator();
    eval.setVariable("b", true);
    assertEquals(eval.evaluate(), 0.42);
    eval = expr.evaluator();
    assertEquals(eval.evaluate(), UserExpression.NA);
  }

  public void testMultipleConditional() {
    final ELExpressionParser parser = new ELExpressionParser();
    final UserExpression expr = parser.parse("if (b) 0.1; if (!b) 0.2");
    UserExpression.Evaluator eval = expr.evaluator();
    eval.setVariable("b", true);
    assertEquals(eval.evaluate(), 0.1);
    eval = expr.evaluator();
    eval.setVariable("b", false);
    assertEquals(eval.evaluate(), 0.2);
    eval = expr.evaluator();
    assertEquals(eval.evaluate(), UserExpression.NA);
  }

  public static final class Foo {

    private final int _bar;

    public Foo(final int v) {
      _bar = v;
    }

    public int getBar() {
      return _bar;
    }

  }

  public void testSyntheticProperties() {
    final ELExpressionParser parser = new ELExpressionParser();
    UserExpression expr = parser.parse("x.bar");
    UserExpression.Evaluator eval = expr.evaluator();
    eval.setVariable("x", new Foo(42));
    assertEquals(eval.evaluate(), 42);
    expr = parser.parse("x.foo");
    eval = expr.evaluator();
    eval.setVariable("x", new Foo(42));
    assertEquals(eval.evaluate(), UserExpression.NA);
    parser.setSynthetic(Foo.class, Integer.class, "foo", new Function<Foo, Integer>() {

      @Override
      public Integer apply(final Foo from) {
        return from.getBar() / 2;
      }

    });
    expr = parser.parse("x.foo");
    eval = expr.evaluator();
    eval.setVariable("x", new Foo(42));
    assertEquals(eval.evaluate(), 21);
  }

}
