/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

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
   * The file location of the resource.
   */
  private static final String HOLIDAY_RESOURCE_PACKAGE = "/com/coppclark/holiday/";
  /**
   * The file location of the index file.
   */
  private static final String HOLIDAY_INDEX_RESOURCE = HOLIDAY_RESOURCE_PACKAGE + "Index.txt";
  /**
   * The Euro.
   */
  private static final Currency EUR = Currency.getInstance("EUR");
  /**
   * The map of Euro capitals to currencies.
   */
  private static final Map<String, Currency> EURO_TO_OLD = new HashMap<String, Currency>();
  static {
    EURO_TO_OLD.put("AT", Currency.getInstance("ATS"));
    EURO_TO_OLD.put("BE", Currency.getInstance("BEF"));
    EURO_TO_OLD.put("NL", Currency.getInstance("NLG"));
    EURO_TO_OLD.put("DE", Currency.getInstance("DEM"));
    EURO_TO_OLD.put("FI", Currency.getInstance("FIM"));
    EURO_TO_OLD.put("FR", Currency.getInstance("FRF"));
    EURO_TO_OLD.put("IE", Currency.getInstance("IEP"));
    EURO_TO_OLD.put("IT", Currency.getInstance("ITL"));
    EURO_TO_OLD.put("LU", Currency.getInstance("LUF"));
    EURO_TO_OLD.put("MC", Currency.getInstance("MCF"));
    EURO_TO_OLD.put("PT", Currency.getInstance("PTE"));
    EURO_TO_OLD.put("SM", Currency.getInstance("SML"));
    EURO_TO_OLD.put("ES", Currency.getInstance("ESP"));
    EURO_TO_OLD.put("VA", Currency.getInstance("VAL"));
    EURO_TO_OLD.put("GR", Currency.getInstance("GRD"));
    EURO_TO_OLD.put("SI", Currency.getInstance("SIT"));
    EURO_TO_OLD.put("CY", Currency.getInstance("CYP"));
    EURO_TO_OLD.put("MT", Currency.getInstance("MTL"));
    EURO_TO_OLD.put("SK", Currency.getInstance("SKK"));
    EURO_TO_OLD.put("EE", Currency.getInstance("EEK"));
  }

  /**
   * The streams to load the currency data.
   */
  private final List<InputStream> _currencyStreams = new ArrayList<InputStream>();
  /**
   * The streams to load the financial centers data.
   */
  private final List<InputStream> _financialCentresStreams = new ArrayList<InputStream>();
  /**
   * The streams to load the exchange settlement data.
   */
  private final List<InputStream> _exchangeSettlementStreams = new ArrayList<InputStream>();
  /**
   * The streams to load the exchange trading data.
   */
  private final List<InputStream> _exchangeTradingStreams = new ArrayList<InputStream>();
  /**
   * The holiday master to populate.
   */
  private HolidayMaster _holidayMaster;

  /**
   * Creates a populated holiday source around the specified master.
   * 
   * @param holidayMaster  the holiday master to populate, not null
   * @return the holiday source, not null
   */
  public static HolidaySource createPopulated(HolidayMaster holidayMaster) {
    CoppClarkHolidayFileReader fileReader = createPopulated0(holidayMaster);
    return fileReader.getHolidaySource();
  }

  /**
   * Creates a populated file reader.
   * <p>
   * The values can be extracted using the methods.
   * 
   * @param holidayMaster  the holiday master to populate, not null
   * @return the holiday reader, not null
   */
  @SuppressWarnings("unchecked")
  private static CoppClarkHolidayFileReader createPopulated0(HolidayMaster holidayMaster) {
    CoppClarkHolidayFileReader fileReader = new CoppClarkHolidayFileReader(holidayMaster);
    InputStream indexStream = fileReader.getClass().getResourceAsStream(HOLIDAY_INDEX_RESOURCE);
    if (indexStream == null) {
      throw new IllegalArgumentException("Unable to find holiday index resource: " + HOLIDAY_INDEX_RESOURCE);
    }
    try {
      List<String> suffixes = IOUtils.readLines(indexStream, "UTF-8");
      for (String suffix : suffixes) {
        if (StringUtils.isNotEmpty(suffix)) {
          String prefix = "";
          if (suffix.startsWith("U")) {
            prefix = "Update_";
            suffix = suffix.substring(1);
          }
          prefix = HOLIDAY_RESOURCE_PACKAGE + prefix;
          suffix +=  ".csv";
          InputStream currencyStream = fileReader.getClass().getResourceAsStream(prefix + "Currencies_" + suffix);
          if (currencyStream == null) {
            throw new IllegalArgumentException("Unable to find holiday data resource: " + prefix + "Currencies_" + suffix);
          }
          fileReader.getCurrencyStreams().add(currencyStream);
          
          InputStream financialCentersStream = fileReader.getClass().getResourceAsStream(prefix + "FinancialCentres_" + suffix);
          if (financialCentersStream == null) {
            throw new IllegalArgumentException("Unable to find holiday data resource: " + prefix + "FinancialCentres_" + suffix);
          }
          fileReader.getFinancialCentresStreams().add(financialCentersStream);
          
          InputStream exchangeSettlementStream = fileReader.getClass().getResourceAsStream(prefix + "ExchangeSettlement_" + suffix);
          if (exchangeSettlementStream == null) {
            throw new IllegalArgumentException("Unable to find holiday data resource: " + prefix + "ExchangeSettlement_" + suffix);
          }
          fileReader.getExchangeSettlementStreams().add(exchangeSettlementStream);
          
          InputStream exchangeTradingStream = fileReader.getClass().getResourceAsStream(prefix + "ExchangeTrading_" + suffix);
          if (exchangeTradingStream == null) {
            throw new IllegalArgumentException("Unable to find holiday data resource: " + prefix + "ExchangeTrading_" + suffix);
          }
          fileReader.getExchangeTradingStreams().add(exchangeTradingStream);
        }
      }
      try {
        fileReader.read();
        return fileReader;
      } finally {
        fileReader.close();
      }
      
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read holiday file", ex);
    } finally {
      IOUtils.closeQuietly(indexStream);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance with a master to populate.
   * 
   * @param holidayMaster  the holiday master, not null
   */
  public CoppClarkHolidayFileReader(HolidayMaster holidayMaster) {
    ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    _holidayMaster = holidayMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday master.
   * 
   * @return the holiday master, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Gets the holiday source.
   * 
   * @return the holiday source, not null
   */
  public MasterHolidaySource getHolidaySource() {
    return new MasterHolidaySource(getHolidayMaster());
  }

  /**
   * Gets the currency streams.
   * 
   * @return the list of streams
   */
  public List<InputStream> getCurrencyStreams() {
    return _currencyStreams;
  }

  /**
   * Gets the financial centres streams.
   * 
   * @return the list of streams
   */
  public List<InputStream> getFinancialCentresStreams() {
    return _financialCentresStreams;
  }

  /**
   * Gets the exchange settlement streams.
   * 
   * @return the list of streams
   */
  public List<InputStream> getExchangeSettlementStreams() {
    return _exchangeSettlementStreams;
  }

  /**
   * Gets the exchange trading streams.
   * 
   * @return the list of streams
   */
  public List<InputStream> getExchangeTradingStreams() {
    return _exchangeTradingStreams;
  }

  //-------------------------------------------------------------------------
  /**
   * Reads the streams to create the master.
   */
  public void read() {
    try {
      parseCurrencyFile();
      parseFinancialCentersFile();
      parseExchangeSettlementFile();
      parseExchangeTradingFile();
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Problem parsing holiday data files", ioe);
    }
  }

  /**
   * Closes the streams.
   */
  public void close() {
    for (InputStream stream : getCurrencyStreams()) {
      IOUtils.closeQuietly(stream);
    }
    for (InputStream stream : getFinancialCentresStreams()) {
      IOUtils.closeQuietly(stream);
    }
    for (InputStream stream : getExchangeSettlementStreams()) {
      IOUtils.closeQuietly(stream);
    }
    for (InputStream stream : getExchangeTradingStreams()) {
      IOUtils.closeQuietly(stream);
    }
  }

  //-------------------------------------------------------------------------
  private void parseCurrencyFile() throws IOException {
    System.out.println("Parse currencies");
    Map<String, HolidayDocument> combinedMap = new HashMap<String, HolidayDocument>(512);
    
    for (InputStream stream : getCurrencyStreams()) {
      Map<String, HolidayDocument> fileMap = new HashMap<String, HolidayDocument>(512);
      CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(stream)));
      
      // header
      String[] row = reader.readNext();
      final int ccIdx = ArrayUtils.indexOf(row, "CenterID");
      final int isoCountryIdx = ArrayUtils.indexOf(row, "ISOCountryCode");
      final int isoCurrencyIdx = ArrayUtils.indexOf(row, "ISOCurrencyCode");
      final int eventDateIdx = ArrayUtils.indexOf(row, "EventDate");
      
      // data
      while ((row = reader.readNext()) != null) {
        String ccId = row[ccIdx].trim();
        String countryISO = row[isoCountryIdx].trim();
        String currencyISO = row[isoCurrencyIdx].trim();
        Currency currency = Currency.getInstance(currencyISO);  // validates format
        String eventDateStr = row[eventDateIdx];
        LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
        HolidayDocument doc = fileMap.get(ccId);
        if (doc == null) {
          currency = fixEuro(currency, countryISO);
          doc = new HolidayDocument(new ManageableHoliday(currency, EMPTY_DATE_LIST));
          doc.setProviderKey(Identifier.of(COPP_CLARK_SCHEME, ccId));
          fileMap.put(ccId, doc);
        }
        doc.getHoliday().getHolidayDates().add(eventDate);
      }
      merge(combinedMap, fileMap);
    }
    mergeWithDatabase(combinedMap);
  }

  private Currency fixEuro(Currency currency, String countryISO) {
    // historic data has lots mapped to EUR, which isn't very useful
    if (countryISO.equals("EU") || currency.equals(EUR) == false) {
      return currency;
    }
    Currency newCurrency = EURO_TO_OLD.get(countryISO);
    if (newCurrency == null) {
      throw new OpenGammaRuntimeException("EUR currency found for " + countryISO + " without mapping to true currency");
    }
    return newCurrency;
  }

  private void parseFinancialCentersFile() throws IOException {
    System.out.println("Parse financial centres");
    Map<String, HolidayDocument> combinedMap = new HashMap<String, HolidayDocument>(512);
    
    for (InputStream stream : getFinancialCentresStreams()) {
      Map<String, HolidayDocument> fileMap = new HashMap<String, HolidayDocument>(512);
      CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(stream)));
      
      // header
      String[] row = reader.readNext();
      final int ccIdx = ArrayUtils.indexOf(row, "CenterID");
      final int isoCountryIdx = ArrayUtils.indexOf(row, "ISOCountryCode");
      final int unlocodeIdx = ArrayUtils.indexOf(row, "UN/LOCODE");
      final int eventDateIdx = ArrayUtils.indexOf(row, "EventDate");
      
      // data
      while ((row = reader.readNext()) != null) {
        String ccId = row[ccIdx].trim();
        String countryISO = row[isoCountryIdx].trim();
        String unlocodePart = row[unlocodeIdx].trim();
        Identifier regionId = RegionUtils.unlocodeRegionId(countryISO + unlocodePart);
        String eventDateStr = row[eventDateIdx];
        LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
        HolidayDocument doc = fileMap.get(ccId);
        if (doc == null) {
          doc = new HolidayDocument(new ManageableHoliday(HolidayType.BANK, regionId, EMPTY_DATE_LIST));
          doc.setProviderKey(Identifier.of(COPP_CLARK_SCHEME, ccId));
          fileMap.put(ccId, doc);
        }
        doc.getHoliday().getHolidayDates().add(eventDate);
      }
      merge(combinedMap, fileMap);
    }
    mergeWithDatabase(combinedMap);
  }

  private void parseExchangeSettlementFile() throws IOException {
    System.out.println("Parse exchange settlements");
    Map<String, HolidayDocument> combinedMap = new HashMap<String, HolidayDocument>(512);
    
    for (InputStream stream : getExchangeSettlementStreams()) {
      Map<String, HolidayDocument> fileMap = new HashMap<String, HolidayDocument>(512);
      CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(stream)));
      
      // header
      String[] row = reader.readNext();
      final int ccIdx = ArrayUtils.indexOf(row, "CenterID");
      final int isoMICCodeIdx = ArrayUtils.indexOf(row, "ISO MIC Code");
      final int eventDateIdx = ArrayUtils.indexOf(row, "EventDate");
      
      // data
      while ((row = reader.readNext()) != null) {
        String ccId = row[ccIdx].trim();
        String isoMICCode = row[isoMICCodeIdx].trim();
        Identifier micId = ExchangeUtils.isoMicExchangeId(isoMICCode);
        String eventDateStr = row[eventDateIdx];
        LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
        HolidayDocument doc = fileMap.get(ccId);
        if (doc == null) {
          doc = new HolidayDocument(new ManageableHoliday(HolidayType.SETTLEMENT, micId, EMPTY_DATE_LIST));
          doc.setProviderKey(Identifier.of(COPP_CLARK_SCHEME, ccId));
          fileMap.put(ccId, doc);
        }
        doc.getHoliday().getHolidayDates().add(eventDate);
      }
      merge(combinedMap, fileMap);
    }
    mergeWithDatabase(combinedMap);
  }

  private void parseExchangeTradingFile() throws IOException {
    System.out.println("Parse exchange trading");
    Map<String, HolidayDocument> combinedMap = new HashMap<String, HolidayDocument>(512);
    
    for (InputStream stream : getExchangeTradingStreams()) {
      Map<String, HolidayDocument> fileMap = new HashMap<String, HolidayDocument>(512);
      CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(stream)));
      
      // header
      String[] row = reader.readNext();
      final int ccIdx = ArrayUtils.indexOf(row, "CenterID");
      final int isoMICCodeIdx = ArrayUtils.indexOf(row, "ISO MIC Code");
      final int eventDateIdx = ArrayUtils.indexOf(row, "EventDate");
      
      // data
      while ((row = reader.readNext()) != null) {
        String ccId = row[ccIdx].trim();
        String isoMICCode = row[isoMICCodeIdx].trim();
        Identifier micId = ExchangeUtils.isoMicExchangeId(isoMICCode);
        String eventDateStr = row[eventDateIdx];
        LocalDate eventDate =  LocalDate.parse(eventDateStr, DATE_FORMAT);
        HolidayDocument doc = fileMap.get(ccId);
        if (doc == null) {
          doc = new HolidayDocument(new ManageableHoliday(HolidayType.TRADING, micId, EMPTY_DATE_LIST));
          doc.setProviderKey(Identifier.of(COPP_CLARK_SCHEME, ccId));
          fileMap.put(ccId, doc);
        }
        doc.getHoliday().getHolidayDates().add(eventDate);
      }
      merge(combinedMap, fileMap);
    }
    mergeWithDatabase(combinedMap);
  }

  //-------------------------------------------------------------------------
  private void merge(Map<String, HolidayDocument> combinedMap, Map<String, HolidayDocument> newMap) {
    for (String id : newMap.keySet()) {
      HolidayDocument newDoc = newMap.get(id);
      Collections.sort(newDoc.getHoliday().getHolidayDates());
      HolidayDocument combinedDoc = combinedMap.get(id);
      if (combinedDoc == null) {
        combinedMap.put(id, newDoc);
      } else {
        mergeDates(combinedDoc, newDoc);
        combinedMap.put(id, newDoc);
      }
    }
  }

  private void mergeWithDatabase(Map<String, HolidayDocument> map) {
    Set<String> set = new HashSet<String>();
    for (HolidayDocument doc : map.values()) {
      if (set.add(doc.getName()) == false) {
        System.out.println("Duplicate name: " + doc.getName());
        System.exit(0);
      }
    }
    for (HolidayDocument doc : map.values()) {
      Collections.sort(doc.getHoliday().getHolidayDates());
      HolidaySearchRequest search = new HolidaySearchRequest(doc.getHoliday().getType());
      search.setProviderKey(doc.getProviderKey());
      HolidaySearchResult result = _holidayMaster.search(search);
      if (result.getDocuments().size() == 0) {
        // add new data
        _holidayMaster.add(doc);
      } else if (result.getDocuments().size() == 1) {
        // update existing data
        HolidayDocument existing = result.getFirstDocument();
        doc.setUniqueId(existing.getUniqueId());
        doc.getHoliday().setUniqueId(existing.getUniqueId());
        mergeDates(existing, doc);
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
        throw new IllegalStateException("Multiple rows in database for Copp Clark ID: " + doc.getProviderKey().getValue() + " " + doc.getHoliday().getType());
      }
    }
  }

  private void mergeDates(HolidayDocument existingDoc, HolidayDocument newDoc) {
    if (newDoc.getHoliday().getHolidayDates().size() == 0) {
      return;
    }
    
    // merge dates
    SortedSet<LocalDate> existingDates = new TreeSet<LocalDate>(existingDoc.getHoliday().getHolidayDates());
    SortedSet<LocalDate> newDates = new TreeSet<LocalDate>(newDoc.getHoliday().getHolidayDates());
    List<LocalDate> result = new ArrayList<LocalDate>(newDates);
    result.addAll(0, existingDates.headSet(newDates.first()));
    result.addAll(existingDates.tailSet(newDates.last().plusYears(1).withDayOfYear(1)));  // file is based on whole years
    
    // store into new document
    newDoc.getHoliday().getHolidayDates().clear();
    newDoc.getHoliday().getHolidayDates().addAll(result);
  }

}
