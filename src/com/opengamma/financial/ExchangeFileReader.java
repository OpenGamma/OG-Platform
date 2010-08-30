/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 */
public class ExchangeFileReader {
  private static final DateTimeFormatter s_timeFormat = DateTimeFormatters.pattern("HH:mm:ss");
  private static final DateTimeFormatter s_dateFormat = DateTimeFormatters.pattern("dd-MMM-yyyy");
  private ExchangeRepository _exchangeMaster;
  private ExchangeSource _exchangeSource;
  
  public ExchangeFileReader(ExchangeRepository exchangeMaster) {
    _exchangeMaster = exchangeMaster;
    _exchangeSource = new DefaultExchangeSource(exchangeMaster);
  }
  
  public static ExchangeSource createPopulatedExchangeSource() {
    ExchangeRepository exchangeMaster = new InMemoryExchangeRepository();
    ExchangeFileReader fileReader = new ExchangeFileReader(exchangeMaster);
    fileReader.readFile(new File(CoppClarkFileReader.EXCHANGE_HOLIDAYS_REPOST_FILE_PATH));
    return new DefaultExchangeSource(exchangeMaster);
  }
  
  public void readFile(File file) {
    try {
      CSVReader reader = new CSVReader(new BufferedReader(new FileReader(file)));
      String[] line = reader.readNext();
      line = reader.readNext(); // deliberate, headers...
      while (line != null) {
        readLine(line);
        line = reader.readNext(); // deliberate
      }
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Could not file or read exchange file, see chained exception", ioe);
    }
  }
  
  private void readLine(String[]rawFields) {
    String countryISO = rawFields[2];
    Identifier region = Identifier.of(InMemoryRegionRepository.ISO_COUNTRY_2, countryISO);
    String exchangeMIC = rawFields[3];
    Identifier mic = Identifier.of(ExchangeRepository.ISO_MIC, exchangeMIC);
    String exchangeName = rawFields[4];
    Identifier coppClarkName = Identifier.of(ExchangeRepository.COPP_CLARK_NAME, exchangeName);
    IdentifierBundle identifiers = IdentifierBundle.of(mic, coppClarkName);
    Exchange exchange = _exchangeSource.getSingleExchange(identifiers);
    if (exchange == null) {
      exchange = _exchangeMaster.addExchange(identifiers, exchangeName, region).getExchange();
    }
    exchange.addCalendarEntry(readCalendarEntryLine(rawFields));    
  }
  private ExchangeCalendarEntry readCalendarEntryLine(String[] rawFields) {
    String group = optionalStringField(rawFields[5]);
    String product = requiredStringField(rawFields[6]);
    String type = optionalStringField(rawFields[7]); // should be required, but isn't there on one entry.
    String code = optionalStringField(rawFields[8]);
    LocalDate calStart = parseDate(rawFields[9]);
    LocalDate calEnd = parseDate(rawFields[10]);
    String dayStart = requiredStringField(rawFields[11]);
    String rangeType = StringUtils.defaultIfEmpty(rawFields[12], null);
    String dayEnd = StringUtils.defaultIfEmpty(rawFields[13], null);
    String phase = optionalStringField(rawFields[14]); // nearly required, but a couple aren't
    LocalTime phaseStarts = parseTime(rawFields[15]);
    LocalTime phaseEnds = parseTime(rawFields[16]);
    LocalTime randomStartMin = parseTime(rawFields[17]);
    LocalTime randomStartMax = parseTime(rawFields[18]);
    LocalTime randomEndMin = parseTime(rawFields[19]);
    LocalTime randomEndMax = parseTime(rawFields[20]);
    LocalDate lastConfirmed = parseDate(rawFields[21]);
    String notes = optionalStringField(rawFields[22]);
    TimeZone timeZone = TimeZone.of(requiredStringField(rawFields[27]));
    return new ExchangeCalendarEntry(group, product, type, code, calStart, calEnd, dayStart, rangeType, dayEnd, 
                                     phase, phaseStarts, phaseEnds, randomStartMin, randomStartMax, 
                                     randomEndMin, randomEndMax, lastConfirmed, notes, timeZone);
  }
  
  private static LocalTime parseTime(String time) {
    return time.isEmpty() ? null : LocalTime.parse(time, s_timeFormat);
  }
  
  private static LocalDate parseDate(String date) {
    StringBuilder sb = new StringBuilder();
    sb.append(date);
    //sb.insert(7, "20"); // beacuse the parser won't cope with 2-digit years...
    return date.isEmpty() ? null : LocalDate.parse(sb.toString(), s_dateFormat);
  }
  
  private static String optionalStringField(String field) {
    return StringUtils.defaultIfEmpty(field, null);
  }
  
  private static String requiredStringField(String field) {
    if (field.isEmpty()) {
      throw new OpenGammaRuntimeException("required field is empty");
    }
    return StringUtils.defaultIfEmpty(field, null);
  }

}
