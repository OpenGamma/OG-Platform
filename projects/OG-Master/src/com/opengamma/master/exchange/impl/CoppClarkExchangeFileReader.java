/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.ExchangeUtils;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.exchange.ManageableExchangeDetail;

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
   * The parsed data.
   */
  private Map<String, ExchangeDocument> _data = new LinkedHashMap<String, ExchangeDocument>();

  /**
   * Creates a populated in-memory master and source.
   * <p>
   * The values can be extracted using the accessor methods.
   * 
   * @return the exchange reader, not null
   */
  public static CoppClarkExchangeFileReader createPopulated() {
    CoppClarkExchangeFileReader fileReader = new CoppClarkExchangeFileReader();
    fileReader.readStream(fileReader.getClass().getResourceAsStream(EXCHANGE_HOLIDAYS_REPOST_RESOURCE));
    return fileReader;
  }

  /**
   * Creates a populated exchange source around the specified master.
   * @param exchangeMaster  the exchange master to populate, not null
   * @return the exchange source, not null
   */
  public static ExchangeSource createPopulated(ExchangeMaster exchangeMaster) {
    CoppClarkExchangeFileReader fileReader = new CoppClarkExchangeFileReader(exchangeMaster);
    InputStream stream = fileReader.getClass().getResourceAsStream(EXCHANGE_HOLIDAYS_REPOST_RESOURCE);
    fileReader.readStream(stream);
    return new MasterExchangeSource(exchangeMaster);
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
    return new MasterExchangeSource(getExchangeMaster());
  }

  //-------------------------------------------------------------------------
  /**
   * Reads the specified input stream, parsing the exchange data.
   * @param inputStream  the input stream, not null
   */
  public void readStream(InputStream inputStream) {
    try {
      CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(inputStream)));
      String[] line = reader.readNext();
      line = reader.readNext();  // skip header
      while (line != null) {
        readLine(line);
        line = reader.readNext();
      }
      mergeDocuments();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not read exchange file, see chained exception", ex);
    }
  }

  private void readLine(String[] rawFields) {
    String exchangeMIC = requiredStringField(rawFields[3]);
    ExchangeDocument doc = _data.get(exchangeMIC);
    if (doc == null) {
      String countryISO = requiredStringField(rawFields[2]);
      String exchangeName = requiredStringField(rawFields[4]);
      String timeZoneId = requiredStringField(rawFields[27]);
      IdentifierBundle id = IdentifierBundle.of(ExchangeUtils.isoMicExchangeId(exchangeMIC));
      IdentifierBundle region = IdentifierBundle.of(RegionUtils.countryRegionId(countryISO));
      TimeZone timeZone = TimeZone.of(timeZoneId);
      ManageableExchange exchange = new ManageableExchange(id, exchangeName, region, timeZone);
      doc = new ExchangeDocument(exchange);
      _data.put(exchangeMIC, doc);
    }
    String timeZoneId = requiredStringField(rawFields[27]);
    if (TimeZone.of(timeZoneId).equals(doc.getExchange().getTimeZone()) == false) {
      throw new OpenGammaRuntimeException("Multiple time-zone entries for exchange: " + doc.getExchange());
    }
    doc.getExchange().getDetail().add(readDetailLine(rawFields));    
  }

  private ManageableExchangeDetail readDetailLine(String[] rawFields) {
    ManageableExchangeDetail detail = new ManageableExchangeDetail();
    detail.setProductGroup(optionalStringField(rawFields[5]));
    detail.setProductName(requiredStringField(rawFields[6]));
    detail.setProductType(optionalStringField(rawFields[7])); // should be required, but isn't there on one entry.
    detail.setProductCode(optionalStringField(rawFields[8]));
    detail.setCalendarStart(parseDate(rawFields[9]));
    detail.setCalendarEnd(parseDate(rawFields[10]));
    detail.setDayStart(requiredStringField(rawFields[11]));
    detail.setDayRangeType(StringUtils.trimToNull(rawFields[12]));
    detail.setDayEnd(StringUtils.trimToNull(rawFields[13]));
    detail.setPhaseName(optionalStringField(rawFields[14])); // nearly required, but a couple aren't
    detail.setPhaseType(null); //optionalStringField(rawFields[])); // NEW FIELD in later data, set to null for now
    detail.setPhaseStart(parseTime(rawFields[15]));
    detail.setPhaseEnd(parseTime(rawFields[16]));
    detail.setRandomStartMin(parseTime(rawFields[17]));
    detail.setRandomStartMax(parseTime(rawFields[18]));
    detail.setRandomEndMin(parseTime(rawFields[19]));
    detail.setRandomEndMax(parseTime(rawFields[20]));
    detail.setLastConfirmed(parseDate(rawFields[21]));
    detail.setNotes(optionalStringField(rawFields[22]));
    return detail;
  }

  private static LocalDate parseDate(String date) {
    StringBuilder sb = new StringBuilder();
    sb.append(date);
    return date.isEmpty() ? null : LocalDate.parse(sb.toString(), DATE_FORMAT);
  }

  private static LocalTime parseTime(String time) {
    return time.isEmpty() ? null : LocalTime.parse(time, TIME_FORMAT);
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

  //-------------------------------------------------------------------------
  /**
   * Merges the documents into the database.
   * @param map  the map of documents, not null
   */
  private void mergeDocuments() {
    for (ExchangeDocument doc : _data.values()) {
      Identifier mic = ExchangeUtils.isoMicExchangeId(doc.getExchange().getIdentifiers().getIdentifier(ExchangeUtils.ISO_MIC));
      ExchangeSearchRequest search = new ExchangeSearchRequest(mic);
      ExchangeSearchResult result = _exchangeMaster.search(search);
      if (result.getDocuments().size() == 0) {
        // add new data
        _exchangeMaster.add(doc);
      } else if (result.getDocuments().size() == 1) {
        // update from existing data
        ExchangeDocument existing = result.getFirstDocument();
        doc.setExchangeId(existing.getExchangeId());
        doc.getExchange().setUniqueIdentifier(existing.getExchangeId());
        // only update if changed
        doc.setVersionFromInstant(null);
        doc.setVersionToInstant(null);
        doc.setCorrectionFromInstant(null);
        doc.setCorrectionToInstant(null);
        existing.setVersionFromInstant(null);
        existing.setVersionToInstant(null);
        existing.setCorrectionFromInstant(null);
        existing.setCorrectionToInstant(null);
        if (doc.equals(existing) == false) {  // only update if changed
          _exchangeMaster.update(doc);
        }
      } else {
        throw new IllegalStateException("Multiple rows in database for ISO MIC ID: " + mic.getValue());
      }
    }
  }

}
