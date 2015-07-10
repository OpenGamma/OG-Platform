/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for Calendars
 */
public class CalendarUtils {

  public static void parseRegionCalendar(String file, HolidayMaster holidayMaster) throws IOException {

    Map<Integer, ManageableHoliday> holidays = new HashMap<>();
    Reader reader = new BufferedReader(
        new InputStreamReader(
            new ClassPathResource(file).getInputStream()
        )
    );

    try {
      CSVReader csvReader = new CSVReader(reader);
      String[] headers = csvReader.readNext();
      for (int i = 0; i < headers.length; i++) {
        ManageableHoliday manageableHoliday = new ManageableHoliday();
        manageableHoliday.setType(HolidayType.BANK);
        manageableHoliday.setRegionExternalId(ExternalSchemes.financialRegionId(headers[i]));
        holidays.put(i, manageableHoliday);
      }

      String[] line;
      while ((line = csvReader.readNext()) != null) {
        for (int i = 0; i < line.length; i++) {
          if (line[i] != null && line[i].length() > 0) {
            holidays.get(i).getHolidayDates().add(LocalDate.parse(line[i]));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (ManageableHoliday holiday: holidays.values()) {
      HolidayDocument document = new HolidayDocument(holiday);
      holidayMaster.add(document);
    }

  }
}
