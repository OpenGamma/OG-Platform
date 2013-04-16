/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DateConstraintTest {

  private final LocalDate referenceDate = LocalDate.of(2010, 4, 23);
  private final LocalDate nowDate = LocalDate.of(2013, 3, 5);

  private FunctionExecutionContext mockExecutionContext() {
    final FunctionExecutionContext context = new FunctionExecutionContext();
    final ZoneId zone = ZoneId.of("UTC");
    context.setValuationClock(Clock.fixed(nowDate.atStartOfDay(zone).toInstant(), zone));
    return context;
  }

  public void testEmptyString() {
    assertNull(DateConstraint.parse(""));
    assertNull(DateConstraint.evaluate(mockExecutionContext(), ""));
  }

  private void testConstructionParseAndEval(final DateConstraint constructed, final String string, final LocalDate evaluated) {
    assertEquals(constructed.toString(), string);
    assertEquals(DateConstraint.parse(string), constructed);
    assertEquals(DateConstraint.evaluate(mockExecutionContext(), string), evaluated);
  }

  public void testConstructionParseAndEval() {
    testConstructionParseAndEval(DateConstraint.of(referenceDate), "2010-04-23", referenceDate);
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME, "Now", nowDate);
    testConstructionParseAndEval(DateConstraint.NULL, "Null", null);
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.previousWeekDay(), "PreviousWeekDay", nowDate.minusDays(1));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.previousWeekDay().previousWeekDay(), "PreviousWeekDay(PreviousWeekDay)", nowDate.minusDays(4));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.nextWeekDay(), "NextWeekDay", nowDate.plusDays(1));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.nextWeekDay().nextWeekDay(), "NextWeekDay(NextWeekDay)", nowDate.plusDays(2));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)), "-P1M", nowDate.minusMonths(1));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.plus(Period.ofMonths(1)), "+P1M", nowDate.plusMonths(1));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), "PreviousWeekDay(-P1M)", nowDate.minusMonths(1).minusDays(1));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay().plus(Period.ofDays(7)), "PreviousWeekDay(-P1M)+P7D", nowDate.minusMonths(1).plusDays(6));
    testConstructionParseAndEval(DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).nextWeekDay().plus(Period.ofDays(7)).nextWeekDay(),
        "NextWeekDay(NextWeekDay(-P1M)+P7D)", nowDate.minusMonths(1).plusDays(9));
  }

  private void testParseAndEval(final DateConstraint constructed, final String string, final LocalDate evaluated) {
    assertEquals(DateConstraint.parse(string), constructed);
    assertEquals(DateConstraint.evaluate(mockExecutionContext(), string), evaluated);
  }

  public void testParseAndEval() {
    testParseAndEval(DateConstraint.VALUATION_TIME.minus(Period.ofDays(1)), "Now-P1D", nowDate.minusDays(1));
    testParseAndEval(DateConstraint.VALUATION_TIME.plus(Period.ofDays(1)), "Now+P1D", nowDate.plusDays(1));
    testParseAndEval(DateConstraint.VALUATION_TIME.previousWeekDay(), "PreviousWeekDay(Now)", nowDate.minusDays(1));
    testParseAndEval(DateConstraint.VALUATION_TIME.nextWeekDay(), "NextWeekDay(Now)", nowDate.plusDays(1));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNull_previousWeekDay() {
    DateConstraint.NULL.previousWeekDay();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNull_nextWeekDay() {
    DateConstraint.NULL.nextWeekDay();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNull_plus() {
    DateConstraint.NULL.plus(Period.ofDays(1));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNull_minus() {
    DateConstraint.NULL.minus(Period.ofDays(1));
  }

  public void testLiteral() {
    final DateConstraint x = DateConstraint.of(referenceDate);
    assertEquals(x.previousWeekDay(), DateConstraint.of(referenceDate.minusDays(1)));
    assertEquals(x.nextWeekDay(), DateConstraint.of(referenceDate.plusDays(3)));
    assertEquals(x.plus(Period.ofDays(1)), DateConstraint.of(referenceDate.plusDays(1)));
    assertEquals(x.minus(Period.ofDays(1)), DateConstraint.of(referenceDate.minusDays(1)));
    assertEquals(x.periodUntil(DateConstraint.of(nowDate)), Period.of(2, 10, 10));
  }

  private void testPlusMinus(final DateConstraint base) {
    assertEquals(base.plus(Period.ofDays(7)).plus(Period.ofDays(7)), base.plus(Period.ofDays(14)));
    assertEquals(base.minus(Period.ofDays(7)).minus(Period.ofDays(7)), base.minus(Period.ofDays(14)));
    assertEquals(base.plus(Period.ofDays(7)).minus(Period.ofDays(7)), base);
    assertEquals(base.minus(Period.ofDays(7)).plus(Period.ofDays(7)), base);
    assertEquals(base.periodUntil(base.plus(Period.ofMonths(1))), Period.ofMonths(1));
    assertEquals(base.plus(Period.ofMonths(1)).periodUntil(base), Period.ofMonths(-1));
    assertEquals(base.periodUntil(base), Period.ZERO);
  }

  public void testPlusMinus() {
    testPlusMinus(DateConstraint.VALUATION_TIME);
    testPlusMinus(DateConstraint.VALUATION_TIME.previousWeekDay());
  }

  private void testWeekDay(final DateConstraint base) {
    assertEquals(base.previousWeekDay().nextWeekDay(), base);
    assertEquals(base.nextWeekDay().previousWeekDay(), base);
  }

  public void testWeekDay() {
    testWeekDay(DateConstraint.VALUATION_TIME);
    testWeekDay(DateConstraint.VALUATION_TIME.plus(Period.ofDays(7)));
  }

}
