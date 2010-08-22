/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static com.opengamma.financial.InMemoryRegionRepository.ISO_COUNTRY_2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class CoppClarkFileReader {
  private static final Logger s_logger = LoggerFactory.getLogger(CoppClarkFileReader.class);
  /**
   * Path to copp-clark holiday files
   */
  public static final String HOLIDAYS_DIR_PATH = RegionFileReader.WORLD_DATA_DIR_PATH + File.separator + "holiday-calendars" + File.separator + "copp-clark";
  /**
   * Path to currency specific holiday calendars CSV file
   */
  public static final String CURRENCY_HOLIDAYS_FILE_PATH = HOLIDAYS_DIR_PATH + File.separator + "Currencies_20100610.csv";
  /**
   * Path to financial centres holiday calendars CSV file
   */
  public static final String FINANCIAL_CENTRES_HOLIDAYS_FILE_PATH = HOLIDAYS_DIR_PATH + File.separator + "FinancialCentres_20100610.csv";
  /**
   * Path to exchange settlement holiday calendars CSV file
   */
  public static final String EXCHANGE_SETTLEMENT_HOLIDAYS_FILE_PATH = HOLIDAYS_DIR_PATH + File.separator + "ExchangeSettlement_20100610.csv";
  /**
   * Path to exchange trading holiday calendars CSV file
   */
  public static final String EXCHANGE_TRADING_HOLIDAYS_FILE_PATH = HOLIDAYS_DIR_PATH + File.separator + "ExchangeTrading_20100610.csv";
  public static final String EXCHANGE_HOLIDAYS_REPOST_FILE_PATH = RegionFileReader.WORLD_DATA_DIR_PATH + File.separator + "exchanges" + File.separator + "THR_20100630.csv.txt";
  
  private HolidayRepository _holidayRepo;
  
  public CoppClarkFileReader(HolidayRepository holidayRepo, File currencies, File financialCenters, File exchangeSettlement, File exchangeTrading) {
    _holidayRepo = holidayRepo;
    try {
      parseCurrencyFile(currencies);
      parseFinancialCentersFile(financialCenters);
      parseExchangeSettlementFile(exchangeSettlement);
      parseExchangeTradingFile(exchangeTrading);
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing exchange/currency data files", ioe);
    }
  }

  private void parseCurrencyFile(File currencyFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "RelatedFinancialCentre", 
                                                                 "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(currencyFile));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      String isoCurrency = row[isoCurrencyIdx];
      Currency currency = Currency.getInstance(isoCurrency.trim());
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      if (eventDate == null) {
        throw new OpenGammaRuntimeException("Invalid holiday date [" + eventDateStr + "] in currencies file");
      }

      HolidaySearchRequest searchReq = new HolidaySearchRequest(currency);
      Collection<HolidayDocument> docs = _holidayRepo.searchHolidays(searchReq).getResults();
      if (docs.size() == 0) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        dates.add(eventDate);
        _holidayRepo.addHoliday(currency, dates);
      } else if (docs.size() == 1) {
        HolidayDocument holidayDoc = docs.iterator().next();
        holidayDoc.getHoliday().getHolidays().add(eventDate);
        _holidayRepo.updateHoliday(holidayDoc);
      }
    }
  }
  
  private void parseFinancialCentersFile(File financialCentersFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "FinancialCentre", 
                                                                 "UN/LOCODE", "EventYear", "EventDate", "EventDayOfWeek", "EventName", 
                                                                 "FileType" });
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(financialCentersFile));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      String isoCountry = row[isoCountryIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      Identifier regionId = Identifier.of(ISO_COUNTRY_2, isoCountry);
      HolidaySearchRequest searchReq = new HolidaySearchRequest(regionId, HolidayType.BANK);
      Collection<HolidayDocument> docs = _holidayRepo.searchHolidays(searchReq).getResults();
      if (docs.size() == 0) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        dates.add(eventDate);
        _holidayRepo.addHoliday(regionId, HolidayType.BANK, dates);
      } else if (docs.size() == 1) {
        HolidayDocument holidayDoc = docs.iterator().next();
        holidayDoc.getHoliday().getHolidays().add(eventDate);
        _holidayRepo.updateHoliday(holidayDoc);
      }
    }
  }
  
  private void parseExchangeSettlementFile(File exchangeSettlementFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    //final int centerIdIdx = columnNames.indexOf("CenterID");
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    //final int exchangeNameIdx = columnNames.indexOf("ExchangeName");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(exchangeSettlementFile));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      //String centerId = row[centerIdIdx];
      //String exchangeName = row[exchangeNameIdx];
      String isoMICCode = row[isoMICCodeIdx];
 
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      
      Identifier micId = new Identifier(ExchangeRepository.ISO_MIC, isoMICCode);
      //Identifier coppClarkNameId = new Identifier(ExchangeRepository.COPP_CLARK_NAME, exchangeName);
      //Identifier coppClarkCenterId = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, centerId);
      //IdentifierBundle bundle = new IdentifierBundle(Arrays.asList(new Identifier[] {micId, coppClarkNameId, coppClarkCenterId }));

      HolidaySearchRequest searchReq = new HolidaySearchRequest(micId, HolidayType.SETTLEMENT);
      Collection<HolidayDocument> docs = _holidayRepo.searchHolidays(searchReq).getResults();
      if (docs.size() == 0) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        dates.add(eventDate);
        _holidayRepo.addHoliday(micId, HolidayType.SETTLEMENT, dates);
      } else if (docs.size() == 1) {
        HolidayDocument holidayDoc = docs.iterator().next();
        holidayDoc.getHoliday().getHolidays().add(eventDate);
        _holidayRepo.updateHoliday(holidayDoc);
      }
    }
  }
  
  private void parseExchangeTradingFile(File exchangeTradingFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(exchangeTradingFile));
    String[] row = reader.readNext(); //skip header
    while ((row = reader.readNext()) != null) {
      String isoMICCode = row[isoMICCodeIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      Identifier micId = new Identifier(ExchangeRepository.ISO_MIC, isoMICCode);
      HolidaySearchRequest searchReq = new HolidaySearchRequest(micId, HolidayType.TRADING);
      Collection<HolidayDocument> docs = _holidayRepo.searchHolidays(searchReq).getResults();
      if (docs.size() == 0) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        dates.add(eventDate);
        _holidayRepo.addHoliday(micId, HolidayType.TRADING, dates);
      } else if (docs.size() == 1) {
        HolidayDocument holidayDoc = docs.iterator().next();
        holidayDoc.getHoliday().getHolidays().add(eventDate);
        _holidayRepo.updateHoliday(holidayDoc);
      }
    }  
  }
  

}
