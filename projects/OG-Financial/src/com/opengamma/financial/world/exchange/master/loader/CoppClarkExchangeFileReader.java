/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.loader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.world.exchange.ExchangeUtils;
import com.opengamma.financial.world.exchange.master.ExchangeDocument;
import com.opengamma.financial.world.exchange.master.ExchangeMaster;
import com.opengamma.financial.world.exchange.master.ManageableExchange;
import com.opengamma.financial.world.exchange.master.ManageableExchangeCalendarEntry;
import com.opengamma.financial.world.exchange.master.MasterExchangeSource;
import com.opengamma.financial.world.exchange.master.memory.InMemoryExchangeMaster;
import com.opengamma.financial.world.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Reads the exchange data from the Copp-Clark data source.
 */
public class CoppClarkExchangeFileReader {

  /**
   * The date format.
   */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatters.pattern("dd-MMM-yyyy");
  /**
   * The time format.
   */
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatters.pattern("HH:mm:ss");
  /**
   * The file version.
   */
  private static final String VERSION = "20100630";
  /**
   * The file location.
   */
  private static final String EXCHANGE_HOLIDAYS_REPOST_RESOURCE = "/com/coppclark/exchange/THR_" + VERSION + ".csv.txt";

  /**
   * The exchange master to populate.
   */
  private ExchangeMaster _exchangeMaster;
  /**
   * The exchange source to populate.
   */
  private MasterExchangeSource _exchangeSource;

  /**
   * Creates a populated in-memory master and source.
   * <p>
   * The values can be extracted using the accessor methods.
   * 
   * @return the exchange reader, not null
   */
  public static CoppClarkExchangeFileReader createPopulated() {
    CoppClarkExchangeFileReader fileReader = new CoppClarkExchangeFileReader();
    fileReader.readFile(fileReader.getClass().getResourceAsStream(EXCHANGE_HOLIDAYS_REPOST_RESOURCE));
    return fileReader;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance with an in-memory exchange master.
   */
  public CoppClarkExchangeFileReader() {
    this(new InMemoryExchangeMaster());
  }

  /**
   * Creates an instance with the exchange master to populate.
   * @param exchangeMaster  the exchange master, not null
   */
  public CoppClarkExchangeFileReader(ExchangeMaster exchangeMaster) {
    _exchangeMaster = exchangeMaster;
    _exchangeSource = new MasterExchangeSource(exchangeMaster);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange master.
   * @return the exchange master, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  /**
   * Gets the exchange source.
   * @return the exchange source, not null
   */
  public MasterExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Reads the specified input stream, parsing the exchange data.
   * @param inputStream  the input stream, not null
   */
  public void readFile(InputStream inputStream) {
    try {
      CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(inputStream)));
      String[] line = reader.readNext();
      line = reader.readNext(); // deliberate, headers...
      while (line != null) {
        readLine(line);
        line = reader.readNext(); // deliberate
      }
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Could not read exchange file, see chained exception", ioe);
    }
  }

  private void readLine(String[]rawFields) {
    String countryISO = rawFields[2];
    Identifier region = RegionUtils.countryRegionId(countryISO);
    String exchangeMIC = rawFields[3];
    Identifier mic = Identifier.of(ExchangeUtils.ISO_MIC, exchangeMIC);
    String exchangeName = rawFields[4];
    Identifier coppClarkName = Identifier.of(ExchangeUtils.COPP_CLARK_NAME, exchangeName);
    IdentifierBundle identifiers = IdentifierBundle.of(mic, coppClarkName);
    ManageableExchange exchange = _exchangeSource.getSingleExchange(identifiers);
    if (exchange == null) {
      ExchangeDocument addDoc = new ExchangeDocument(new ManageableExchange(identifiers, exchangeName, region));
      exchange = _exchangeMaster.add(addDoc).getExchange();
    }
    exchange.getCalendarEntries().add(readCalendarEntryLine(rawFields));    
  }

  private ManageableExchangeCalendarEntry readCalendarEntryLine(String[] rawFields) {
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
    return new ManageableExchangeCalendarEntry(group, product, type, code, calStart, calEnd, dayStart, rangeType, dayEnd, 
                                     phase, phaseStarts, phaseEnds, randomStartMin, randomStartMax, 
                                     randomEndMin, randomEndMax, lastConfirmed, notes, timeZone);
  }

  private static LocalTime parseTime(String time) {
    return time.isEmpty() ? null : LocalTime.parse(time, TIME_FORMAT);
  }

  private static LocalDate parseDate(String date) {
    StringBuilder sb = new StringBuilder();
    sb.append(date);
    //sb.insert(7, "20"); // beacuse the parser won't cope with 2-digit years...
    return date.isEmpty() ? null : LocalDate.parse(sb.toString(), DATE_FORMAT);
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
