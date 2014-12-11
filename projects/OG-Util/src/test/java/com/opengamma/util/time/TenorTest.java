/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor.BusinessDayTenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class TenorTest {

  @Test
  public void testPeriodStaticConstructor() {
    assertEquals(Tenor.ONE_DAY, Tenor.of(Period.ofDays(1)));
    assertEquals(Tenor.ONE_WEEK, Tenor.of(Period.ofDays(7)));
    assertEquals(Tenor.ONE_MONTH, Tenor.of(Period.ofMonths(1)));
    assertEquals(Tenor.ONE_YEAR, Tenor.of(Period.ofYears(1)));
    assertEquals(Tenor.ONE_DAY, Tenor.ofDays(1));
    assertEquals(Tenor.ONE_WEEK, Tenor.ofDays(7));
    assertEquals(Tenor.ONE_MONTH, Tenor.ofMonths(1));
    assertEquals(Tenor.ONE_YEAR, Tenor.ofYears(1));
  }
  
  @Test
  public void testBusinessDayStaticConstructor() {
    assertEquals(Tenor.ON, Tenor.of(BusinessDayTenor.OVERNIGHT));
    assertEquals(Tenor.SN, Tenor.of(BusinessDayTenor.SPOT_NEXT));
    assertEquals(Tenor.TN, Tenor.of(BusinessDayTenor.TOM_NEXT));
    assertEquals(Tenor.ON, Tenor.ofBusinessDay(BusinessDayTenor.OVERNIGHT));
    assertEquals(Tenor.SN, Tenor.ofBusinessDay(BusinessDayTenor.SPOT_NEXT));
    assertEquals(Tenor.TN, Tenor.ofBusinessDay(BusinessDayTenor.TOM_NEXT));
    assertEquals(Tenor.ON, Tenor.ofBusinessDay("OVERNIGHT"));
    assertEquals(Tenor.SN, Tenor.ofBusinessDay("SPOT_NEXT"));
    assertEquals(Tenor.TN, Tenor.ofBusinessDay("TOM_NEXT"));
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseFailure() {
    Tenor.parse("0D");
  }
  
  @Test
  public void testParsePeriod() {
    assertEquals(Tenor.TWO_DAYS, Tenor.parse("P2D"));
    assertEquals(Tenor.TWO_WEEKS, Tenor.parse("P14D"));
    assertEquals(Tenor.TWO_MONTHS, Tenor.parse("P2M"));
    assertEquals(Tenor.TWO_YEARS, Tenor.parse("P2Y"));    
  }
  
  @Test
  public void testParseBusinessDay() {
    assertEquals(Tenor.ON, Tenor.parse("OVERNIGHT"));
    assertEquals(Tenor.SN, Tenor.parse("SPOT_NEXT"));
    assertEquals(Tenor.TN, Tenor.parse("TOM_NEXT"));
  }
  
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetPeriod() {
    assertEquals(Period.ofDays(3), Tenor.THREE_DAYS.getPeriod());
    assertEquals(Period.ofDays(21), Tenor.THREE_WEEKS.getPeriod());
    assertEquals(Period.ofMonths(3), Tenor.THREE_MONTHS.getPeriod());
    assertEquals(Period.ofYears(3), Tenor.THREE_YEARS.getPeriod());
    Tenor.ON.getPeriod(); // no period in business day tenors
  }
  
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetBusinessDays() {
    assertEquals(BusinessDayTenor.OVERNIGHT, Tenor.ON.getBusinessDayTenor());
    assertEquals(BusinessDayTenor.SPOT_NEXT, Tenor.SN.getBusinessDayTenor());
    assertEquals(BusinessDayTenor.TOM_NEXT, Tenor.TN.getBusinessDayTenor());
    Tenor.DAY.getBusinessDayTenor(); // no business days in period tenors
  }
  
  @Test
  public void testToFormattedString() {
    Tenor tenor = Tenor.FOUR_MONTHS;
    assertEquals("P4M", tenor.toFormattedString());
    tenor = Tenor.FOUR_YEARS;
    assertEquals("P4Y", tenor.toFormattedString());
    tenor = Tenor.ON;
    assertEquals("OVERNIGHT", tenor.toFormattedString());
    tenor = Tenor.SN;
    assertEquals("SPOT_NEXT", tenor.toFormattedString());
  }

  @Test
  public void testToString() {
    Tenor tenor = Tenor.FOUR_MONTHS;
    assertEquals("Tenor[P4M]", tenor.toString());
    tenor = Tenor.FOUR_YEARS;
    assertEquals("Tenor[P4Y]", tenor.toString());
    tenor = Tenor.ON;
    assertEquals("Tenor[OVERNIGHT]", tenor.toString());
    tenor = Tenor.SN;
    assertEquals("Tenor[SPOT_NEXT]", tenor.toString());
  }
  
  @Test
  public void testComparator() {
    final List<Tenor> tenors = Arrays.asList(Tenor.ON, Tenor.TN, Tenor.SN, Tenor.WORKING_WEEK, Tenor.ONE_MONTH, Tenor.ONE_YEAR, Tenor.ONE_MONTH, Tenor.ON);
    final List<Tenor> shuffled = new ArrayList<>(tenors);
    Collections.shuffle(shuffled);
    final Set<Tenor> tenorSet = new HashSet<>(shuffled);
    assertEquals(tenors.size() - 2, tenorSet.size());
    assertTrue(tenors.containsAll(tenorSet));
    final List<Tenor> expectedSortedTenors = tenors.subList(0, tenors.size() - 2);
    final Set<Tenor> sortedTenors = new TreeSet<>(shuffled);
    assertArrayEquals(expectedSortedTenors.toArray(new Tenor[expectedSortedTenors.size()]), sortedTenors.toArray(new Tenor[expectedSortedTenors.size()]));
  }
}
