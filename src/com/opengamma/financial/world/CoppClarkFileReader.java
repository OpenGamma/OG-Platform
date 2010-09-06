/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world;

import static com.opengamma.financial.world.region.InMemoryRegionMaster.ISO_COUNTRY_2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.opengamma.financial.Currency;
import com.opengamma.financial.world.exchange.ExchangeMaster;
import com.opengamma.financial.world.holiday.DefaultHolidaySource;
import com.opengamma.financial.world.holiday.HolidayDocument;
import com.opengamma.financial.world.holiday.HolidayMaster;
import com.opengamma.financial.world.holiday.HolidaySearchRequest;
import com.opengamma.financial.world.holiday.HolidaySource;
import com.opengamma.financial.world.holiday.HolidayType;
import com.opengamma.financial.world.region.RegionFileReader;
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
  private static final String VERSION = "20100610";
  private static final String CURRENCY_HOLIDAYS_RESOURCE = "/com/coppclark/holiday/Currencies_" + VERSION + ".csv";
  private static final String FINANCIAL_CENTERS_RESOURCE = "/com/coppclark/holiday/FinancialCentres_" + VERSION + ".csv";
  private static final String EXCHANGE_SETTLEMENT_RESOURCE = "/com/coppclark/holiday/ExchangeSettlement_" + VERSION + ".csv";;
  private static final String EXCHANGE_TRADING_RESOURCE = "/com/coppclark/holiday/ExchangeTrading_" + VERSION + ".csv";
  
  private HolidayMaster _holidayRepo;
  
  // REVIEW: jim 29-Aug-2010 -- this initialization in the constructor is horrible.
  public CoppClarkFileReader(HolidayMaster holidayRepo, File currencies, File financialCenters, File exchangeSettlement, File exchangeTrading) {
    _holidayRepo = holidayRepo;
    try {
      parseCurrencyFile(new FileInputStream(currencies));
      parseFinancialCentersFile(new FileInputStream(financialCenters));
      parseExchangeSettlementFile(new FileInputStream(exchangeSettlement));
      parseExchangeTradingFile(new FileInputStream(exchangeTrading));
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing exchange/currency data files", ioe);
    }
  }
  
  public CoppClarkFileReader(HolidayMaster holidayRepo) {
    _holidayRepo = holidayRepo;
  }
  
  public static HolidaySource createPopulatedHolidaySource(HolidayMaster holidayMaster) {
    CoppClarkFileReader fileReader = new CoppClarkFileReader(holidayMaster);
    InputStream currencyStream = fileReader.getClass().getResourceAsStream(CURRENCY_HOLIDAYS_RESOURCE);
    InputStream financialCentersStream = fileReader.getClass().getResourceAsStream(FINANCIAL_CENTERS_RESOURCE);
    InputStream exchangeSettlementStream = fileReader.getClass().getResourceAsStream(EXCHANGE_SETTLEMENT_RESOURCE);
    InputStream exchangeTradingStream = fileReader.getClass().getResourceAsStream(EXCHANGE_TRADING_RESOURCE);
    fileReader.parseStreams(currencyStream, financialCentersStream, exchangeSettlementStream, exchangeTradingStream);
    return new DefaultHolidaySource(holidayMaster);
  }
  
  public void parseStreams(InputStream currencyStream, InputStream financialCentersStream, InputStream exchangeSettlementStream, InputStream exchangeTradingStream) {
    try {
      parseCurrencyFile(currencyStream);
      parseFinancialCentersFile(financialCentersStream);
      parseExchangeSettlementFile(exchangeSettlementStream);
      parseExchangeTradingFile(exchangeTradingStream);
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing exchange/currency data files", ioe);
    }
  }

  private void parseCurrencyFile(InputStream currencyStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "RelatedFinancialCentre", 
                                                                 "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(currencyStream)));
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
  
  private void parseFinancialCentersFile(InputStream financialCentersStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "FinancialCentre", 
                                                                 "UN/LOCODE", "EventYear", "EventDate", "EventDayOfWeek", "EventName", 
                                                                 "FileType" });
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(financialCentersStream)));
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
  
  private void parseExchangeSettlementFile(InputStream exchangeSettlementStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    //final int centerIdIdx = columnNames.indexOf("CenterID");
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    //final int exchangeNameIdx = columnNames.indexOf("ExchangeName");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(exchangeSettlementStream)));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      //String centerId = row[centerIdIdx];
      //String exchangeName = row[exchangeNameIdx];
      String isoMICCode = row[isoMICCodeIdx];
 
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      
      Identifier micId = new Identifier(ExchangeMaster.ISO_MIC, isoMICCode);
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
  
  private void parseExchangeTradingFile(InputStream exchangeTradingStream) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(exchangeTradingStream)));
    String[] row = reader.readNext(); //skip header
    while ((row = reader.readNext()) != null) {
      String isoMICCode = row[isoMICCodeIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      Identifier micId = new Identifier(ExchangeMaster.ISO_MIC, isoMICCode);
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
