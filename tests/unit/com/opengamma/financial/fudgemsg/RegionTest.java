/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.financial.Region;

public class RegionTest extends FinancialTestBase {

  private Region getRef() {
    return getRegionRepository().getHierarchyNode(LocalDate.now(Clock.system(TimeZone.UTC)), "Political",
        "United Kingdom");
  }

  @Test
  public void testCycle() {
    final Region ref = getRef();
    assertEquals(ref, cycleObject(Region.class, ref));
  }

}
