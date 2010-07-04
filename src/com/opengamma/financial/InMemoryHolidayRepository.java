/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static com.opengamma.financial.InMemoryRegionRepository.ISO_COUNTRY_2;
import static com.opengamma.financial.InMemoryRegionRepository.WORLD_DATA_DIR_PATH;
import static com.opengamma.financial.InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;
/**
 * In-memory implementation of HolidayRepository that is populated from CSV files.
 * THIS IMPLEMENTAION DOES NOT IMPLEMENT VERSIONING, DATES PASSED IN ARE IGNORED
 */
public class InMemoryHolidayRepository implements HolidayRepository {
  // TODO: jim 2-Jul-2010 -- Make this cope with versioning...
  /**
   * Path to copp-clark holiday files
   */
  public static final String HOLIDAYS_DIR_PATH = WORLD_DATA_DIR_PATH + File.separator + "holiday-calendars" + File.separator + "copp-clark";
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

  private InMemoryRegionRepository _regionRepo;
  private InMemoryExchangeRepository _exchangeRepo;

  public InMemoryHolidayRepository(InMemoryRegionRepository regionRepo, InMemoryExchangeRepository exchangeRepo, 
                                   File currencies, File financialCenters, File exchangeSettlement, File exchangeTrading) {
    _regionRepo = regionRepo;
    _exchangeRepo = exchangeRepo;
    try {
      parseCurrencyFile(currencies);
      parseFinancialCentersFile(financialCenters);
      parseExchangeSettlementFile(exchangeSettlement);
      parseExchangeTradingFile(exchangeTrading);
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing exchange/currency data files", ioe);
    }
  }
  
  private Map<HolidayType, Map<Object, MutableLocalDateDoubleTimeSeries>> _holidayMap = new HashMap<HolidayType, Map<Object, MutableLocalDateDoubleTimeSeries>>();
  
  @SuppressWarnings("unchecked")
  private void parseCurrencyFile(File currencyFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "RelatedFinancialCentre", 
                                                                 "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    //final int relatedFinancialCentreIdx = columnNames.indexOf("RelatedFinancialCentre");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    //final int fileTypeIdx = columnNames.indexOf("FileType");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(currencyFile));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      String isoCurrency = row[isoCurrencyIdx];
      Currency currency = Currency.getInstance(isoCurrency.trim());
      String isoCountry = row[isoCountryIdx];
      //String relatedFinancialCentre = row[relatedFinancialCentreIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      if (eventDate == null) {
        throw new OpenGammaRuntimeException("Invalid holiday date [" + eventDateStr + "] in currencies file");
      }
      //String fileType = row[fileTypeIdx];
      //Pair<String, Object> protoIndependentStateCond = Pair.<String, Object>of(InMemoryRegionRepository.TYPE_COLUMN, RegionType.PROTO_INDEPENDENT_STATE);
      Pair<String, Object> countryCodeCond = Pair.<String, Object>of(ISO_COUNTRY_2, isoCountry.trim());
      SortedSet<Region> matches = _regionRepo.getHierarchyNodes(null, POLITICAL_HIERARCHY_NAME, countryCodeCond);
      if (matches == null || matches.size() == 0) {
        throw new OpenGammaRuntimeException("Cannot get region for country code " + isoCountry.trim());
      }
      // now the results are sorted by region 'size' (region type's natural ordering), then we get the first one, should be the top.
//      if (matches.size() > 1) {
//        throw new OpenGammaRuntimeException("Found more than one match for region with iso code " + isoCountry.trim());
//      }
//      assert 1 == matches.size();
      Region myRegion = matches.iterator().next();
      if (!_holidayMap.containsKey(HolidayType.CURRENCY)) {
        _holidayMap.put(HolidayType.CURRENCY, new HashMap<Object, MutableLocalDateDoubleTimeSeries>());
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.CURRENCY);
      if (!holidayMap.containsKey(myRegion.getUniqueIdentifier())) {
        holidayMap.put(myRegion.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(myRegion.getUniqueIdentifier());
      dts.putDataPoint(eventDate, 1.0d);
      // file under both currency AND region.
      if (!holidayMap.containsKey(currency)) {
        holidayMap.put(currency, new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts2 = holidayMap.get(currency);
      dts2.putDataPoint(eventDate, 1.0d);
    }
  }
  
  private void parseFinancialCentersFile(File financialCentersFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "FinancialCentre", 
                                                                 "UN/LOCODE", "EventYear", "EventDate", "EventDayOfWeek", "EventName", 
                                                                 "FileType" });
    //final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    //final int relatedFinancialCentreIdx = columnNames.indexOf("RelatedFinancialCentre");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    //final int fileTypeIdx = columnNames.indexOf("FileType");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(financialCentersFile));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      //String isoCurrency = row[isoCurrencyIdx];
      String isoCountry = row[isoCountryIdx];
      //String relatedFinancialCentre = row[relatedFinancialCentreIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      //String fileType = row[fileTypeIdx];
      Set<Region> matches = _regionRepo.getHierarchyNodes(null, POLITICAL_HIERARCHY_NAME, ISO_COUNTRY_2, isoCountry.trim());
      // assert 1 == matches.size(); not now...
      if (matches == null || matches.size() == 0) {
        throw new OpenGammaRuntimeException("Cannot find region with country code: " + isoCountry.trim());
      }
      Region myRegion = matches.iterator().next();
      if (!_holidayMap.containsKey(HolidayType.BANK)) {
        _holidayMap.put(HolidayType.BANK, new HashMap<Object, MutableLocalDateDoubleTimeSeries>());
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.BANK);
      if (!holidayMap.containsKey(myRegion.getUniqueIdentifier())) {
        holidayMap.put(myRegion.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(myRegion.getUniqueIdentifier());
      dts.putDataPoint(eventDate, 1.0d);
    }
  }
  
  private void parseExchangeSettlementFile(File exchangeSettlementFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int centerIdIdx = columnNames.indexOf("CenterID");
    //final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    final int exchangeNameIdx = columnNames.indexOf("ExchangeName");
    //final int relatedFinancialCentreIdx = columnNames.indexOf("RelatedFinancialCentre");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    //final int fileTypeIdx = columnNames.indexOf("FileType");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(exchangeSettlementFile));
    String[] row = reader.readNext(); // throw away the header.
    while ((row = reader.readNext()) != null) {
      String centerId = row[centerIdIdx];
      //String isoCurrency = row[isoCurrencyIdx];
      String isoCountry = row[isoCountryIdx];
      String isoMICCode = row[isoMICCodeIdx];
      String exchangeName = row[exchangeNameIdx];
      //String relatedFinancialCentre = row[relatedFinancialCentreIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      //String fileType = row[fileTypeIdx];
      SortedSet<Region> matches = _regionRepo.getHierarchyNodes(null, POLITICAL_HIERARCHY_NAME, ISO_COUNTRY_2, isoCountry.trim());
      // assert 1 == matches.size(); now we rely on the ordering
      Region myRegion = matches.iterator().next();
      Identifier micId = new Identifier(ExchangeRepository.ISO_MIC, isoMICCode);
      Identifier coppClarkNameId = new Identifier(ExchangeRepository.COPP_CLARK_NAME, exchangeName);
      Identifier coppClarkCenterId = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, centerId);
      IdentifierBundle bundle = new IdentifierBundle(Arrays.asList(new Identifier[] {micId, coppClarkNameId, coppClarkCenterId }));
      Exchange exchange = _exchangeRepo.putExchange(null, bundle, exchangeName, myRegion.getUniqueIdentifier());
      if (!_holidayMap.containsKey(HolidayType.SETTLEMENT)) {
        _holidayMap.put(HolidayType.SETTLEMENT, new HashMap<Object, MutableLocalDateDoubleTimeSeries>());
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.SETTLEMENT);
      if (!holidayMap.containsKey(exchange.getUniqueIdentifier())) {
        holidayMap.put(exchange.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(exchange.getUniqueIdentifier());
      dts.putDataPoint(eventDate, 1.0d);
    }
  }
  
  private void parseExchangeTradingFile(File exchangeTradingFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISO MIC Code", "ISOCountryCode", "ExchangeName", "EventYear", "EventDate", "EventDayOfWeek", "EventName", "FileType" });
    final int centerIdIdx = columnNames.indexOf("CenterID");
    //final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    final int isoMICCodeIdx = columnNames.indexOf("ISO MIC Code");
    final int exchangeNameIdx = columnNames.indexOf("ExchangeName");
    //final int relatedFinancialCentreIdx = columnNames.indexOf("RelatedFinancialCentre");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    //final int fileTypeIdx = columnNames.indexOf("FileType");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(exchangeTradingFile));
    String[] row = reader.readNext(); //skip header
    while ((row = reader.readNext()) != null) {
      String centerId = row[centerIdIdx];
      //String isoCurrency = row[isoCurrencyIdx];
      String isoCountry = row[isoCountryIdx];
      String isoMICCode = row[isoMICCodeIdx];
      String exchangeName = row[exchangeNameIdx];
      //String relatedFinancialCentre = row[relatedFinancialCentreIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      //String fileType = row[fileTypeIdx];
      Set<Region> matches = _regionRepo.getHierarchyNodes(null, POLITICAL_HIERARCHY_NAME, ISO_COUNTRY_2, isoCountry.trim());
      //assert 1 == matches.size(); now we rely on the ordering to put the most important one at the top.
      Region myRegion = matches.iterator().next();
      Identifier micId = new Identifier(ExchangeRepository.ISO_MIC, isoMICCode);
      Identifier coppClarkNameId = new Identifier(ExchangeRepository.COPP_CLARK_NAME, exchangeName);
      Identifier coppClarkCenterId = new Identifier(ExchangeRepository.COPP_CLARK_CENTER_ID, centerId);
      IdentifierBundle bundle = new IdentifierBundle(Arrays.asList(new Identifier[] {micId, coppClarkNameId, coppClarkCenterId }));
      Exchange exchange = _exchangeRepo.putExchange(null, bundle, exchangeName, myRegion.getUniqueIdentifier());
      if (!_holidayMap.containsKey(HolidayType.TRADING)) {
        _holidayMap.put(HolidayType.TRADING, new HashMap<Object, MutableLocalDateDoubleTimeSeries>());
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.TRADING);
      if (!holidayMap.containsKey(exchange.getUniqueIdentifier())) {
        holidayMap.put(exchange.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(exchange.getUniqueIdentifier());
      dts.putDataPoint(eventDate, 1.0d);
    }  
  }

  @Override
  public boolean isHoliday(LocalDate versionDate, Currency currency, LocalDate holidayDate, HolidayType type) {
    try {
      if (type == HolidayType.SETTLEMENT || type == HolidayType.TRADING) {
        throw new OpenGammaRuntimeException("Cannot use a currency " + currency + " to establish settlement or trading holidays, need an exchange");
      }
      if (!_holidayMap.containsKey(type)) {
        throw new OpenGammaRuntimeException("No holidays registered of type " + type);
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> exchangeUIdToDTS = _holidayMap.get(type);
      if (!exchangeUIdToDTS.containsKey(currency)) {
        throw new OpenGammaRuntimeException("No holidays registered for currency " + currency + " of type " + type); 
      }
      LocalDateDoubleTimeSeries dts = exchangeUIdToDTS.get(currency);
      Double value = dts.getValue(holidayDate);
      if (value == null && (!isWeekend(holidayDate))) {
        return false; // REVIEW: jim 21-June-2010 -- this desperately needs a better fix than this.
      } else {
        return true;
      }
    } catch (NoSuchElementException nsee) { // actually, this shouldn't happen, but we keep it here in case we change the dts used.
      return false;
    }  
  }
  
  @Override
  public boolean isHoliday(LocalDate versionDate, Region region, LocalDate holidayDate, HolidayType type) {
    try {
      if (!_holidayMap.containsKey(type)) {
        throw new OpenGammaRuntimeException("No holidays registered of type " + type);
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> exchangeUIdToDTS = _holidayMap.get(type);
      if (!exchangeUIdToDTS.containsKey(region.getUniqueIdentifier())) {
        throw new OpenGammaRuntimeException("No holidays registered for region " + region + " of type " + type); 
      }
      LocalDateDoubleTimeSeries dts = exchangeUIdToDTS.get(region.getUniqueIdentifier());
      Double value = dts.getValue(holidayDate);
      if (value == null && (!isWeekend(holidayDate))) {
        return false; // REVIEW: jim 21-June-2010 -- this desperately needs a better fix than this.
      } else {
        return true;
      }
    } catch (NoSuchElementException nsee) { // actually, this shouldn't happen, but we keep it here in case we change the dts used.
      return false;
    }  
  }

  @Override
  public boolean isHoliday(LocalDate versionDate, Exchange exchange, LocalDate holidayDate, HolidayType type) {
    try {
      if (type == HolidayType.BANK || type == HolidayType.CURRENCY) {
        return isHoliday(versionDate, exchange.getRegion(), holidayDate, type);
      }
      if (!_holidayMap.containsKey(type)) {
        throw new OpenGammaRuntimeException("No holidays registered of type " + type);
      }
      Map<Object, MutableLocalDateDoubleTimeSeries> exchangeUIdToDTS = _holidayMap.get(type);
      if (!exchangeUIdToDTS.containsKey(exchange.getUniqueIdentifier())) {
        throw new OpenGammaRuntimeException("No holidays registered for exchange " + exchange + " of type " + type); 
      }
      LocalDateDoubleTimeSeries dts = exchangeUIdToDTS.get(exchange.getUniqueIdentifier());
      Double value = dts.getValue(holidayDate);
      if (value == null && (!isWeekend(holidayDate))) {
        return false; // REVIEW: jim 21-June-2010 -- this desperately needs a better fix than this.
      } else {
        return true;
      }
    } catch (NoSuchElementException nsee) { // actually, this shouldn't happen, but we keep it here in case we change the dts used.
      return false;
    }
  }
  
  private boolean isWeekend(LocalDate holidayDate) {
    return (holidayDate.getDayOfWeek() == DayOfWeek.SATURDAY) || (holidayDate.getDayOfWeek() == DayOfWeek.SUNDAY);
  }
}
