/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.time.calendar.LocalDate;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;

/**
 * Test InMemoryHolidayMaster.
 */
public class InMemoryHolidayMasterTest {

  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final CurrencyUnit GBP = CurrencyUnit.GBP;

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
    HolidayDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(Identifier.of("MemExg", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

}
