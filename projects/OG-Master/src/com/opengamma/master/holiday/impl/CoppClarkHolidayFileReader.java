/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.common.Currency;
import com.opengamma.core.exchange.ExchangeUtils;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.ArgumentChecker;

/**
 * Reads the holiday data from the Copp-Clark data source.
 * <p>
 * This will merge the input with the data already in the database.
 */
public class CoppClarkHolidayFileReader {

  /**
   * The Copp Clark scheme.
   */
  private static final IdentificationScheme COPP_CLARK_SCHEME = IdentificationScheme.of("COPP_CLARK");
  /**
   * The date format.
   */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatters.pattern("yyyyMMdd");
  /**
   * An empty list of dates.
   */
  private static final List<LocalDate> EMPTY_DATE_LIST = Collections.emptyList();
  /**
   * The file version.
   */
  private static final String VERSION = "20100610";
  private static final String CURRENCY_HOLIDAYS_RESOURCE = "/com/coppclark/holiday/Currencies_" + VERSION + ".csv";
  private static final String FINANCIAL_CENTERS_RESOURCE = "/com/coppclark/holiday/FinancialCentres_" + VERSION + ".csv";
  private static final String EXCHANGE_SETTLEMENT_RESOURCE = "/com/coppclark/holiday/ExchangeSettlement_" + VERSION + ".csv";;
  private static final String EXCHANGE_TRADING_RESOURCE = "/com/coppclark/holiday/ExchangeTrading_" + VERSION + ".csv";

  /**
   * The holiday master to populate.
   */
  private HolidayMaster _holidayMaster;

  /**
   * Creates a populated holiday source around the specified master.
   * @param holidayMaster  the holiday master to populate, not null
   * @return the holiday source, not null
   */
  public static HolidaySource createPopulated(HolidayMaster holidayMaster) {
    CoppClarkHolidayFileReader fileReader = new CoppClarkHolidayFileReader(holidayMaster);
    InputStream currencyStream = fileReader.getClass().getResourceAsStream(CURRENCY_HOLIDAYS_RESOURCE);
    InputStream financialCentersStream = fileReader.getClass().getResourceAsStream(FINANCIAL_CENTERS_RESOURCE);
    InputStream exchangeSettlementStream = fileReader.getClass().getResourceAsStream(EXCHANGE_SETTLEMENT_RESOURCE);
    InputStream exchangeTradingStream = fileReader.getClass().getResourceAsStream(EXCHANGE_TRADING_RESOURCE);
    fileReader.readStreams(currencyStream, financialCentersStream, exchangeSettlementStream, exchangeTradingStream);
    return new MasterHolidaySource(holidayMaster);
  }

  /**
   * Creates an instance with a master to populate.
   * @param holidayMaster  the holiday master, not null
   */
  public CoppClarkHolidayFileReader(HolidayMaster holidayMaster) {
    ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    _holidayMaster = holidayMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday master.
   * @return the holiday master, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Reads the streams to create the master.
   * @param currencies  the currency stream, not null
   * @param financialCenters  the centers stream, not null
   * @param exchangeSettlement  the settlement exchanges stream, not null
   * @param exchangeTrading  the trading exchanges stream, not null
   */
  public void readFiles(File currencies, File financialCenters, File exchangeSettlement, File exchangeTrading) {
    try {
      parseCurrencyFile(new FileInputStream(currencies));
      parseFinancialCentersFile(new FileInputStream(financialCenters));
      parseExchangeSettlementFile(new FileInputStream(exchangeSettlement));
      parseExchangeTradingFile(new FileInputStream(exchangeTrading));
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing exchange/currency data files", ioe);
    }
  }

  /**
   * Reads the streams to create the master.
   * @param currencyStream  the currency stream, not null
   * @param financialCentersStream  the centers stream, not null
   * @param exchangeSettlementStream  the settlement exchanges stream, not null
   * @param exchangeTradingStream  the trading exchanges stream, not null
   */
  public void readStreams(InputStream currencyStream, InputStream financialCentersStream, InputStream exchangeSettlementStream, InputStream exchangeTradingStream) {
    try {
      parseCurrencyFile(currencyStream);
      parseFinancialCentersFile(financialCentersStream);
      parseExchangeSettlementFile(exchangeSettlementStream);
      parseExchangeTradingFile(exchangeTradingStream);
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing exchange/currency data files", ioe);
    }
  }

  //-------------------------------------------------------------------------
  private void parseCurrencyFile(InputStream currencyStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {
      "CenterID", "ISOCurrencyCode", "ISOCountryCode", "RelatedFinancialCentre", 
      "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int ccIdx = columnNames.indexOf("CenterID");
    final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    Map<String, HolidayDocument> map = new HashMap<String, HolidayDocument>(512);
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(currencyStream)));
    String[] row = reader.readNext(); // throw away the header
    while ((row = reader.readNext()) != null) {
      String ccId = row[ccIdx].trim();
      String currencyISO = row[isoCurrencyIdx].trim();
      Currency currency = Currency.getInstance(currencyISO);  // validates format
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
      HolidayDocument doc = map.get(ccId);
      if (doc == null) {
        doc = new HolidayDocument(new ManageableHoliday(currency, EMPTY_DATE_LIST));
        doc.setProviderId(Identifier.of(COPP_CLARK_SCHEME, ccId));
        map.put(ccId, doc);
      }
      doc.getHoliday().getHolidayDates().add(eventDate);
    }
    mergeDocuments(map);
  }

  private void parseFinancialCentersFile(InputStream financialCentersStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {
      "CenterID", "ISOCurrencyCode", "ISOCountryCode", "FinancialCentre", 
      "UN/LOCODE", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int ccIdx = columnNames.indexOf("CenterID");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    Map<String, HolidayDocument> map = new HashMap<String, HolidayDocument>(512);
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(financialCentersStream)));
    String[] row = reader.readNext(); // throw away the header
    while ((row = reader.readNext()) != null) {
      String ccId = row[ccIdx].trim();
      String isoCountry = row[isoCountryIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
      Identifier regionId = RegionUtils.countryRegionId(isoCountry);
      HolidayDocument doc = map.get(ccId);
      if (doc == null) {
        doc = new HolidayDocument(new ManageableHoliday(HolidayType.BANK, regionId, EMPTY_DATE_LIST));
        doc.setProviderId(Identifier.of(COPP_CLARK_SCHEME, ccId));
        map.put(ccId, doc);
      }
      doc.getHoliday().getHolidayDates().add(eventDate);
    }
    mergeDocuments(map);
  }

  private void parseExchangeSettlementFile(InputStream exchangeSettlementStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {
      "CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear",
      "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int ccIdx = columnNames.indexOf("CenterID");
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    Map<String, HolidayDocument> map = new HashMap<String, HolidayDocument>(512);
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(exchangeSettlementStream)));
    String[] row = reader.readNext(); // throw away the header
    while ((row = reader.readNext()) != null) {
      String ccId = row[ccIdx].trim();
      String isoMICCode = row[isoMICCodeIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
      Identifier micId = Identifier.of(ExchangeUtils.ISO_MIC, isoMICCode);
      HolidayDocument doc = map.get(ccId);
      if (doc == null) {
        doc = new HolidayDocument(new ManageableHoliday(HolidayType.SETTLEMENT, micId, EMPTY_DATE_LIST));
        doc.setProviderId(Identifier.of(COPP_CLARK_SCHEME, ccId));
        map.put(ccId, doc);
      }
      doc.getHoliday().getHolidayDates().add(eventDate);
    }
    mergeDocuments(map);
  }

  private void parseExchangeTradingFile(InputStream exchangeTradingStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {
      "CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear",
      "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int ccIdx = columnNames.indexOf("CenterID");
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    Map<String, HolidayDocument> map = new HashMap<String, HolidayDocument>(512);
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(exchangeTradingStream)));
    String[] row = reader.readNext(); // throw away the header
    while ((row = reader.readNext()) != null) {
      String ccId = row[ccIdx].trim();
      String isoMICCode = row[isoMICCodeIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
      Identifier micId = Identifier.of(ExchangeUtils.ISO_MIC, isoMICCode);
      HolidayDocument doc = map.get(ccId);
      if (doc == null) {
        doc = new HolidayDocument(new ManageableHoliday(HolidayType.TRADING, micId, EMPTY_DATE_LIST));
        doc.setProviderId(Identifier.of(COPP_CLARK_SCHEME, ccId));
        map.put(ccId, doc);
      }
      doc.getHoliday().getHolidayDates().add(eventDate);
    }
    mergeDocuments(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Merges the documents into the database.
   * @param map  the map of documents, not null
   */
  private void mergeDocuments(Map<String, HolidayDocument> map) {
    for (HolidayDocument doc : map.values()) {
      Collections.sort(doc.getHoliday().getHolidayDates());
      HolidaySearchRequest search = new HolidaySearchRequest(doc.getHoliday().getType());
      search.setProviderId(doc.getProviderId());
      HolidaySearchResult result = _holidayMaster.search(search);
      if (result.getDocuments().size() == 0) {
        // add new data
        _holidayMaster.add(doc);
      } else if (result.getDocuments().size() == 1) {
        // update existing data
        HolidayDocument existing = result.getFirstDocument();
        doc.setUniqueId(existing.getUniqueId());
        doc.getHoliday().setUniqueIdentifier(existing.getUniqueId());
        // merge dates
        if (doc.getHoliday().getHolidayDates().size() > 0) {
          LocalDate newFirstDate = doc.getHoliday().getHolidayDates().get(0);
          List<LocalDate> existingDates = existing.getHoliday().getHolidayDates();
          for (int i = 0; i < existingDates.size(); i++) {
            LocalDate existingDate = existingDates.get(i);
            if (existingDate.getYear() < newFirstDate.getYear()) {
              doc.getHoliday().getHolidayDates().add(i, existingDate);
            } else {
              break;
            }
          }
        }
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
          _holidayMaster.update(doc);
        }
      } else {
        throw new IllegalStateException("Multiple rows in database for Copp Clark ID: " + doc.getProviderId().getValue() + " " + doc.getHoliday().getType());
      }
    }
  }

}
