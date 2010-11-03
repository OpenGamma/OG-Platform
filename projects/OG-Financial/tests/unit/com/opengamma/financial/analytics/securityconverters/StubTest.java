/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.securityconverters;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.analytics.securityconverters.StubCalculator.StubType;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class StubTest {
  private static final ZonedDateTime[] NO_STUB1 = new ZonedDateTime[] {DateUtil.getUTCDate(2008, 1, 1), DateUtil.getUTCDate(2008, 4, 1), DateUtil.getUTCDate(2008, 7, 1),
      DateUtil.getUTCDate(2008, 10, 1), DateUtil.getUTCDate(2009, 1, 1), DateUtil.getUTCDate(2009, 4, 1), DateUtil.getUTCDate(2009, 7, 1), DateUtil.getUTCDate(2009, 10, 1),
      DateUtil.getUTCDate(2010, 1, 1), DateUtil.getUTCDate(2010, 4, 1), DateUtil.getUTCDate(2010, 7, 1), DateUtil.getUTCDate(2010, 10, 1), DateUtil.getUTCDate(2011, 1, 1),
      DateUtil.getUTCDate(2011, 4, 1), DateUtil.getUTCDate(2011, 7, 1), DateUtil.getUTCDate(2011, 10, 1)};
  private static final ZonedDateTime[] SHORT_START_STUB1;
  private static final ZonedDateTime[] LONG_START_STUB1;
  private static final ZonedDateTime[] SHORT_END_STUB1;
  private static final ZonedDateTime[] LONG_END_STUB1;
  private static final LocalDate[] NO_STUB2;
  private static final LocalDate[] SHORT_START_STUB2;
  private static final LocalDate[] LONG_START_STUB2;
  private static final LocalDate[] SHORT_END_STUB2;
  private static final LocalDate[] LONG_END_STUB2;
  private static final ZonedDateTime[] NO_STUB3 = new ZonedDateTime[] {DateUtil.getUTCDate(2008, 1, 31), DateUtil.getUTCDate(2008, 4, 30), DateUtil.getUTCDate(2008, 7, 31),
      DateUtil.getUTCDate(2008, 10, 31), DateUtil.getUTCDate(2009, 1, 31), DateUtil.getUTCDate(2009, 4, 30), DateUtil.getUTCDate(2009, 7, 31), DateUtil.getUTCDate(2009, 10, 31),
      DateUtil.getUTCDate(2010, 1, 31), DateUtil.getUTCDate(2010, 4, 30), DateUtil.getUTCDate(2010, 7, 31), DateUtil.getUTCDate(2010, 10, 31), DateUtil.getUTCDate(2011, 1, 31),
      DateUtil.getUTCDate(2011, 4, 30), DateUtil.getUTCDate(2011, 7, 31), DateUtil.getUTCDate(2011, 10, 31)};
  private static final ZonedDateTime[] SHORT_START_STUB3;
  private static final ZonedDateTime[] LONG_START_STUB3;
  private static final ZonedDateTime[] SHORT_END_STUB3;
  private static final ZonedDateTime[] LONG_END_STUB3;
  private static final LocalDate[] NO_STUB4;
  private static final LocalDate[] SHORT_START_STUB4;
  private static final LocalDate[] LONG_START_STUB4;
  private static final LocalDate[] SHORT_END_STUB4;
  private static final LocalDate[] LONG_END_STUB4;

  static {
    final int n = NO_STUB1.length;
    SHORT_START_STUB1 = Arrays.copyOf(NO_STUB1, n);
    LONG_START_STUB1 = Arrays.copyOf(NO_STUB1, n);
    SHORT_END_STUB1 = Arrays.copyOf(NO_STUB1, n);
    LONG_END_STUB1 = Arrays.copyOf(NO_STUB1, n);
    SHORT_START_STUB3 = Arrays.copyOf(NO_STUB3, n);
    LONG_START_STUB3 = Arrays.copyOf(NO_STUB3, n);
    SHORT_END_STUB3 = Arrays.copyOf(NO_STUB3, n);
    LONG_END_STUB3 = Arrays.copyOf(NO_STUB3, n);
    SHORT_START_STUB1[0] = DateUtil.getUTCDate(2008, 1, 30);
    LONG_START_STUB1[0] = DateUtil.getUTCDate(2007, 12, 1);
    SHORT_END_STUB1[n - 1] = DateUtil.getUTCDate(2011, 9, 4);
    LONG_END_STUB1[n - 1] = DateUtil.getUTCDate(2011, 11, 3);
    SHORT_START_STUB3[0] = DateUtil.getUTCDate(2008, 2, 27);
    LONG_START_STUB3[0] = DateUtil.getUTCDate(2008, 1, 27);
    SHORT_END_STUB3[n - 1] = DateUtil.getUTCDate(2011, 9, 4);
    LONG_END_STUB3[n - 1] = DateUtil.getUTCDate(2011, 11, 4);
    NO_STUB2 = new LocalDate[n];
    SHORT_START_STUB2 = new LocalDate[n];
    LONG_START_STUB2 = new LocalDate[n];
    SHORT_END_STUB2 = new LocalDate[n];
    LONG_END_STUB2 = new LocalDate[n];
    NO_STUB4 = new LocalDate[n];
    SHORT_START_STUB4 = new LocalDate[n];
    LONG_START_STUB4 = new LocalDate[n];
    SHORT_END_STUB4 = new LocalDate[n];
    LONG_END_STUB4 = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      NO_STUB2[i] = NO_STUB1[i].toLocalDate();
      SHORT_START_STUB4[i] = SHORT_START_STUB3[i].toLocalDate();
      LONG_START_STUB4[i] = LONG_START_STUB3[i].toLocalDate();
      SHORT_END_STUB4[i] = SHORT_END_STUB3[i].toLocalDate();
      LONG_END_STUB4[i] = LONG_END_STUB3[i].toLocalDate();
      NO_STUB4[i] = NO_STUB3[i].toLocalDate();
      SHORT_START_STUB2[i] = SHORT_START_STUB1[i].toLocalDate();
      LONG_START_STUB2[i] = LONG_START_STUB1[i].toLocalDate();
      SHORT_END_STUB2[i] = SHORT_END_STUB1[i].toLocalDate();
      LONG_END_STUB2[i] = LONG_END_STUB1[i].toLocalDate();
    }
  }

  @Test
  public void testStartType() {
    assertEquals(StubCalculator.getStartStubType(NO_STUB1, 4), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(NO_STUB2, 4), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(SHORT_START_STUB1, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_START_STUB2, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(LONG_START_STUB1, 4), StubType.LONG_START);
    assertEquals(StubCalculator.getStartStubType(LONG_START_STUB2, 4), StubType.LONG_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_END_STUB1, 4), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(SHORT_END_STUB2, 4), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(LONG_END_STUB1, 4), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(LONG_END_STUB2, 4), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(NO_STUB3, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(NO_STUB4, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_START_STUB3, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_START_STUB4, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(LONG_START_STUB3, 4), StubType.LONG_START);
    assertEquals(StubCalculator.getStartStubType(LONG_START_STUB4, 4), StubType.LONG_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_END_STUB3, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_END_STUB4, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(LONG_END_STUB3, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(LONG_END_STUB4, 4), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(NO_STUB3, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(NO_STUB4, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(SHORT_START_STUB3, 4, true), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_START_STUB4, 4, true), StubType.SHORT_START);
    assertEquals(StubCalculator.getStartStubType(LONG_START_STUB3, 4, true), StubType.LONG_START);
    assertEquals(StubCalculator.getStartStubType(LONG_START_STUB4, 4, true), StubType.LONG_START);
    assertEquals(StubCalculator.getStartStubType(SHORT_END_STUB3, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(SHORT_END_STUB4, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(LONG_END_STUB3, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getStartStubType(LONG_END_STUB4, 4, true), StubType.NONE);
  }

  @Test
  public void testEndType() {
    assertEquals(StubCalculator.getEndStubType(NO_STUB1, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(NO_STUB2, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_START_STUB1, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_START_STUB2, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(LONG_START_STUB1, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(LONG_START_STUB2, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_END_STUB1, 4), StubType.SHORT_END);
    assertEquals(StubCalculator.getEndStubType(SHORT_END_STUB2, 4), StubType.SHORT_END);
    assertEquals(StubCalculator.getEndStubType(LONG_END_STUB1, 4), StubType.LONG_END);
    assertEquals(StubCalculator.getEndStubType(LONG_END_STUB2, 4), StubType.LONG_END);
    assertEquals(StubCalculator.getEndStubType(NO_STUB3, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(NO_STUB4, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_START_STUB3, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_START_STUB4, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(LONG_START_STUB3, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(LONG_START_STUB4, 4), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_END_STUB3, 4), StubType.SHORT_END);
    assertEquals(StubCalculator.getEndStubType(SHORT_END_STUB4, 4), StubType.SHORT_END);
    assertEquals(StubCalculator.getEndStubType(LONG_END_STUB3, 4), StubType.LONG_END);
    assertEquals(StubCalculator.getEndStubType(LONG_END_STUB4, 4), StubType.LONG_END);
    assertEquals(StubCalculator.getEndStubType(NO_STUB3, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(NO_STUB4, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_START_STUB3, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_START_STUB4, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(LONG_START_STUB3, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(LONG_START_STUB4, 4, true), StubType.NONE);
    assertEquals(StubCalculator.getEndStubType(SHORT_END_STUB3, 4, true), StubType.SHORT_END);
    assertEquals(StubCalculator.getEndStubType(SHORT_END_STUB4, 4, true), StubType.SHORT_END);
    assertEquals(StubCalculator.getEndStubType(LONG_END_STUB3, 4, true), StubType.LONG_END);
    assertEquals(StubCalculator.getEndStubType(LONG_END_STUB4, 4, true), StubType.LONG_END);

  }
}
