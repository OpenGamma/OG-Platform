/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static com.opengamma.financial.security.InMemoryRegionRepository.ISO_COUNTRY_2;
import static com.opengamma.financial.security.InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
/**
 * In-memory implementation of HolidayRepository that is populated from CSV files.
 */
public class InMemoryHolidayRepository implements HolidayRepository {
  private static final IdentificationScheme ISO_MIC = new IdentificationScheme("ISO_MIC");
  private static final IdentificationScheme COPP_CLARK_NAME = new IdentificationScheme("COPP_CLARK_NAME");
  private static final IdentificationScheme COPP_CLARK_CENTER_ID = new IdentificationScheme("COPP_CLARK_CENTER_ID");

  private InMemoryRegionRepository _regionRepo;
  private InMemoryExchangeRespository _exchangeRepo;

  public InMemoryHolidayRepository(InMemoryRegionRepository regionRepo, InMemoryExchangeRespository exchangeRepo, File currencies, File financialCenters, File exchangeSettlement, File exchangeTrading) throws IOException {
    _regionRepo = regionRepo;
    _exchangeRepo = exchangeRepo;
    parseCurrencyFile(currencies);
    parseFinancialCentersFile(financialCenters);
    parseExchangeSettlementFile(exchangeSettlement);
    parseExchangeTradingFile(exchangeTrading);
  }
  
  private Map<HolidayType, Map<UniqueIdentifier, MutableLocalDateDoubleTimeSeries>> _holidayMap = new HashMap<HolidayType, Map<UniqueIdentifier, MutableLocalDateDoubleTimeSeries>>();
  
  private void parseCurrencyFile(File currencyFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "RelatedFinancialCentre", 
                                                                 "EventYear", "EventYear", "EventDayOfWeek", "EventName", "FileType" });
    //final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    //final int relatedFinancialCentreIdx = columnNames.indexOf("RelatedFinancialCentre");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    //final int fileTypeIdx = columnNames.indexOf("FileType");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(currencyFile));
    String[] row;
    while ((row = reader.readNext()) != null) {
      //String isoCurrency = row[isoCurrencyIdx];
      String isoCountry = row[isoCountryIdx];
      //String relatedFinancialCentre = row[relatedFinancialCentreIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      //String fileType = row[fileTypeIdx];
      Set<Region> matches = _regionRepo.getHierarchyNodes(null, POLITICAL_HIERARCHY_NAME, ISO_COUNTRY_2, isoCountry.trim());
      assert 1 == matches.size();
      Region myRegion = matches.iterator().next();
      if (!_holidayMap.containsKey(HolidayType.CURRENCY)) {
        _holidayMap.put(HolidayType.CURRENCY, new HashMap<UniqueIdentifier, MutableLocalDateDoubleTimeSeries>());
      }
      Map<UniqueIdentifier, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.CURRENCY);
      if (!holidayMap.containsKey(myRegion)) {
        holidayMap.put(myRegion.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(myRegion);
      dts.putDataPoint(eventDate, 1.0d);
    }
  }
  
  private void parseFinancialCentersFile(File financialCentersFile) throws IOException {
    final List<String> columnNames = Arrays.asList(new String[] {"CenterID", "ISOCurrencyCode", "ISOCountryCode", "FinancialCentre", 
        "UN/LOCODE", "EventYear", "EventDayOfWeek", "EventName", "FileType" });
    //final int isoCurrencyIdx = columnNames.indexOf("ISOCurrencyCode");
    final int isoCountryIdx = columnNames.indexOf("ISOCountryCode");
    //final int relatedFinancialCentreIdx = columnNames.indexOf("RelatedFinancialCentre");
    final int eventDateIdx = columnNames.indexOf("EventDate");
    //final int fileTypeIdx = columnNames.indexOf("FileType");
    
    DateTimeFormatter formatter = DateTimeFormatters.pattern("yyyyMMdd");
    
    CSVReader reader = new CSVReader(new FileReader(financialCentersFile));
    String[] row;
    while ((row = reader.readNext()) != null) {
      //String isoCurrency = row[isoCurrencyIdx];
      String isoCountry = row[isoCountryIdx];
      //String relatedFinancialCentre = row[relatedFinancialCentreIdx];
      String eventDateStr = row[eventDateIdx];
      LocalDate eventDate =  formatter.parse(eventDateStr, LocalDate.rule());
      //String fileType = row[fileTypeIdx];
      Set<Region> matches = _regionRepo.getHierarchyNodes(null, POLITICAL_HIERARCHY_NAME, ISO_COUNTRY_2, isoCountry.trim());
      assert 1 == matches.size();
      Region myRegion = matches.iterator().next();
      if (!_holidayMap.containsKey(HolidayType.BANK)) {
        _holidayMap.put(HolidayType.BANK, new HashMap<UniqueIdentifier, MutableLocalDateDoubleTimeSeries>());
      }
      Map<UniqueIdentifier, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.BANK);
      if (!holidayMap.containsKey(myRegion)) {
        holidayMap.put(myRegion.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(myRegion);
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
    String[] row;
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
      assert 1 == matches.size();
      Region myRegion = matches.iterator().next();
      Identifier micId = new Identifier(ISO_MIC, isoMICCode);
      Identifier coppClarkNameId = new Identifier(COPP_CLARK_NAME, exchangeName);
      Identifier coppClarkCenterId = new Identifier(COPP_CLARK_CENTER_ID, centerId);
      IdentifierBundle bundle = new IdentifierBundle(Arrays.asList(new Identifier[] { micId, coppClarkNameId, coppClarkCenterId }));
      _exchangeRepo.putExchange(null, bundle, exchangeName, myRegion.getUniqueIdentifier());
      if (!_holidayMap.containsKey(HolidayType.SETTLEMENT)) {
        _holidayMap.put(HolidayType.SETTLEMENT, new HashMap<UniqueIdentifier, MutableLocalDateDoubleTimeSeries>());
      }
      Map<UniqueIdentifier, MutableLocalDateDoubleTimeSeries> holidayMap = _holidayMap.get(HolidayType.SETTLEMENT);
      if (!holidayMap.containsKey(myRegion)) {
        holidayMap.put(myRegion.getUniqueIdentifier(), new ListLocalDateDoubleTimeSeries());
      }
      MutableLocalDateDoubleTimeSeries dts = holidayMap.get(myRegion);
      dts.putDataPoint(eventDate, 1.0d);
    }
  }
  
  private void parseExchangeTradingFile(File exchangeTradingFile) {
    parseFile(exchangeTradingFile);  
  }
  
  private void parseFile(File file) {

  }

  @Override
  public boolean isHoliday(LocalDate versionDate, Region region, LocalDate holidayDate, HolidayType type) {
    try {
      return _holidayMap.get(type).get(region.getUniqueIdentifier()).getValue(holidayDate) != 0.0d; // REVIEW: jim 21-June-2010 -- this desperately needs a better fix than this.
    } catch (NoSuchElementException nsee) {
      return false;
    }
  }

  @Override
  public boolean isHoliday(LocalDate versionDate, Exchange exchange, LocalDate holidayDate, HolidayType type) {
    try {
      return _holidayMap.get(type).get(exchange.getUniqueIdentifier()).getValue(holidayDate) != 0.0d; // REVIEW: jim 21-June-2010 -- this desperately needs a better fix than this.
    } catch (NoSuchElementException nsee) {
      return false;
    }
  }
}
