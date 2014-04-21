/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import org.threeten.bp.LocalDate;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;

/**
 * Example code to load some basic holiday data.
 * <p>
 * This code is kept deliberately as simple as possible to demonstrate pushing data into the holiday master, and to allow basic operations on it by other parts of the system. We have loaders for data
 * available from third party data providers. Please contact us for more information.
 */
@Scriptable
public class ExampleHolidayLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleHolidayLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a simple holiday calendar directly and stores it into the master.
   * <p>
   * A more typical loader would open a file (e.g. CSV or XML) and use that to create the {@link Holiday} instances to be stored.
   */
  @Override
  protected void doRun() {
    final ManageableHoliday calendar = new ManageableHoliday();
    calendar.setType(HolidayType.CURRENCY);
    calendar.setCurrency(Currency.GBP);
    calendar.getHolidayDates().add(LocalDate.of(2010, 1, 1));
    calendar.getHolidayDates().add(LocalDate.of(2010, 4, 2));
    calendar.getHolidayDates().add(LocalDate.of(2010, 4, 5));
    calendar.getHolidayDates().add(LocalDate.of(2010, 5, 3));
    calendar.getHolidayDates().add(LocalDate.of(2010, 5, 31));
    calendar.getHolidayDates().add(LocalDate.of(2010, 8, 30));
    calendar.getHolidayDates().add(LocalDate.of(2010, 12, 27));
    calendar.getHolidayDates().add(LocalDate.of(2010, 12, 28));
    storeHolidays(calendar);
  }

  /**
   * Stores the holiday calendar in the holiday master. If there is already a calendar with that name it is updated.
   *
   * @param calendar the calendar to add
   */
  private void storeHolidays(final ManageableHoliday calendar) {
    final HolidayMaster master = getToolContext().getHolidayMaster();
    final HolidaySearchRequest request = new HolidaySearchRequest();
    request.setType(calendar.getType());
    switch (calendar.getType()) {
      case CURRENCY:
        request.setCurrency(calendar.getCurrency());
        break;
      default:
        throw new UnsupportedOperationException(calendar.getType().toString());
    }
    final HolidaySearchResult result = master.search(request);
    if (result.getFirstDocument() != null) {
      //System.out.println("Updating " + calendar.getType());
      final HolidayDocument document = result.getFirstDocument();
      document.setHoliday(calendar);
      master.update(document);
    } else {
      //System.out.println("Adding " + calendar.getType());
      master.add(new HolidayDocument(calendar));
    }
  }

}
