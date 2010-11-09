/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.memory;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.time.calendar.LocalDate;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.common.Currency;
import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.financial.world.holiday.master.ManageableHoliday;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test InMemoryHolidayMaster.
 */
public class InMemoryHolidayMasterTest {

  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final Currency GBP = Currency.getInstance("GBP");

  private InMemoryHolidayMaster master;
  private HolidayDocument addedDoc;

  @Before
  public void setUp() {
    master = new InMemoryHolidayMaster();
    ManageableHoliday inputHoliday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidayDocument inputDoc = new HolidayDocument(inputHoliday);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueIdentifier.of("A", "B"));
  }

  public void test_get_match() {
    HolidayDocument result = master.get(addedDoc.getHolidayId());
    assertEquals(Identifier.of("MemExg", "1"), result.getHolidayId());
    assertEquals(addedDoc, result);
  }

}
