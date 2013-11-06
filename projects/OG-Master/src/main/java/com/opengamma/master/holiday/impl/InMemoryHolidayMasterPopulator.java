/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.ResourceBundle;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ResourceUtils;

/**
 *  Populate a holiday master with holidays - can load from a csv in the classpath.
 */
public class InMemoryHolidayMasterPopulator {

  private static final DateTimeFormatter US_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

  public static void populate(final HolidayMaster holidayMaster, Map<String, ManageableHoliday> holidays) {
    for (Map.Entry<String, ManageableHoliday> entry : holidays.entrySet()) {
      HolidayDocument doc = new HolidayDocument();
      doc.setName(entry.getKey());
      doc.setHoliday(entry.getValue());
      holidayMaster.add(doc);
    }
  }

  public static Map<String, ManageableHoliday> load(final String resourceLocation, final String regionScheme) {
    final ResourceBundle holidayProperties = ResourceBundle.getBundle(resourceLocation);
    final Map<String, ManageableHoliday> holidays = Maps.newHashMapWithExpectedSize(holidayProperties.keySet().size());
    CSVReader csvReader;
    for (final String regionCode : holidayProperties.keySet()) {
      final String file = holidayProperties.getString(regionCode);

      ManageableHoliday holiday = holidays.get(regionCode);
      if (holiday == null) {
        //// old style with region
        holiday = new ManageableHoliday();
        //holiday.setType(HolidayType.BANK);
        //holiday.setRegionExternalId(ExternalId.of(regionScheme, regionCode));
        //holidays.put(regionCode, holiday);
        holiday.setType(HolidayType.CUSTOM);
        holiday.setCustomExternalId(ExternalId.of(regionScheme, regionCode));
        holidays.put(regionCode, holiday);
      }

      if (file.trim().isEmpty()) {
        continue; // no holidays, will use standard weekday calendar
      }

      try {
        final File filepath = ResourceUtils.createResource(file).getFile();
        csvReader = new CSVReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filepath))));
      } catch (FileNotFoundException ex) {
        throw new OpenGammaRuntimeException("file not found: " + file);
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("IO Exception: " + ex);
      }
      String[] currLine;

      // Throw away the header line.
      //csvReader.readNext();
      try {
        while ((currLine = csvReader.readNext()) != null) {
          String dateInUSFormat = currLine[0].trim();
          if (dateInUSFormat.startsWith("#")) {
            continue;
          }

          LocalDate date = LocalDate.parse(dateInUSFormat, US_FORMATTER);
          holiday.getHolidayDates().add(date);
        }
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("IOError: " + ex);
      } finally {
        try {
          csvReader.close();
        } catch (IOException ex) {
          throw new OpenGammaRuntimeException("IOError on closing: " + ex);
        }
      }
    }
    return holidays;
  }


}
