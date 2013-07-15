/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.coppclark;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.exchange.ManageableExchangeDetail;
import com.opengamma.master.exchange.impl.ExchangeSearchIterator;
import com.opengamma.master.exchange.impl.InMemoryExchangeMaster;
import com.opengamma.master.exchange.impl.MasterExchangeSource;
import com.opengamma.util.i18n.Country;

/**
 * Reads the exchange data from the Copp-Clark data source.
 */
public class CoppClarkExchangeFileReader {

  /**
   * The date format.
   */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
  /**
   * The time format.
   */
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
  /**
   * The file location of the resource.
   */
  private static final String EXCHANGE_RESOURCE_PACKAGE = "/com/coppclark/exchange/";
  /**
   * The file location of the index file.
   */
  private static final String EXCHANGE_INDEX_RESOURCE = EXCHANGE_RESOURCE_PACKAGE + "Index.txt";

  private static final int INDEX_MIC = 0;
  private static final int INDEX_NAME = 1;
  private static final int INDEX_COUNTRY = 2;
  private static final int INDEX_ZONE_ID = 3;
  private static final int INDEX_PRODUCT_GROUP = 4;
  private static final int INDEX_PRODUCT_NAME = 5;
  private static final int INDEX_PRODUCT_TYPE = 6;
  private static final int INDEX_PRODUCT_CODE = 7;
  private static final int INDEX_CALENDAR_START = 8;
  private static final int INDEX_CALENDAR_END = 9;
  private static final int INDEX_DAY_START = 10;
  private static final int INDEX_DAY_RANGE_TYPE = 11;
  private static final int INDEX_DAY_END = 12;
  private static final int INDEX_PHASE_NAME = 13;
  private static final int INDEX_PHASE_TYPE = 14;
  private static final int INDEX_PHASE_START = 15;
  private static final int INDEX_PHASE_END = 16;
  private static final int INDEX_RANDOM_START_MIN = 17;
  private static final int INDEX_RANDOM_START_MAX = 18;
  private static final int INDEX_RANDOM_END_MIN = 19;
  private static final int INDEX_RANDOM_END_MAX = 20;
  private static final int INDEX_LAST_CONFIRMED = 21;
  private static final int INDEX_NOTES = 22;

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
   * The values can be extracted using the methods.
   * 
   * @return the exchange reader, not null
   */
  public static CoppClarkExchangeFileReader createPopulated() {
    return createPopulated0(new InMemoryExchangeMaster());
  }

  /**
   * Creates a populated exchange source around the specified master.
   * 
   * @param exchangeMaster  the exchange master to populate, not null
   * @return the exchange source, not null
   */
  public static ExchangeSource createPopulated(ExchangeMaster exchangeMaster) {
    CoppClarkExchangeFileReader fileReader = createPopulated0(exchangeMaster);
    return new MasterExchangeSource(fileReader.getExchangeMaster());
  }

  /**
   * Creates a populated file reader.
   * <p>
   * The values can be extracted using the methods.
   * 
   * @param exchangeMaster  the exchange master to populate, not null
   * @return the exchange reader, not null
   */
  private static CoppClarkExchangeFileReader createPopulated0(ExchangeMaster exchangeMaster) {
    CoppClarkExchangeFileReader fileReader = new CoppClarkExchangeFileReader(exchangeMaster);
    InputStream indexStream = fileReader.getClass().getResourceAsStream(EXCHANGE_INDEX_RESOURCE);
    if (indexStream == null) {
      throw new IllegalArgumentException("Unable to find exchange index resource: " + EXCHANGE_INDEX_RESOURCE);
    }
    try {
      List<String> fileNames = IOUtils.readLines(indexStream, "UTF-8");
      if (fileNames.size() != 1) {
        throw new IllegalArgumentException("Exchange index file should contain one line");
      }
      InputStream dataStream = fileReader.getClass().getResourceAsStream(EXCHANGE_RESOURCE_PACKAGE + fileNames.get(0));
      if (dataStream == null) {
        throw new IllegalArgumentException("Unable to find exchange data resource: " + EXCHANGE_RESOURCE_PACKAGE + fileNames.get(0));
      }
      try {
        fileReader.readStream(dataStream);
        return fileReader;
      } finally {
        IOUtils.closeQuietly(dataStream);
      }
      
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read exchange file", ex);
    } finally {
      IOUtils.closeQuietly(indexStream);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance with the exchange master to populate.
   * 
   * @param exchangeMaster  the exchange master, not null
   */
  public CoppClarkExchangeFileReader(ExchangeMaster exchangeMaster) {
    _exchangeMaster = exchangeMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange master.
   * 
   * @return the exchange master, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  /**
   * Gets the exchange source.
   * 
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
      String[] line = reader.readNext();  // header
      int[] indices = findIndices(line);
      line = reader.readNext();
      while (line != null) {
        readLine(line, indices);
        line = reader.readNext();
      }
      mergeDocuments();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read exchange file", ex);
    }
  }

  private int[] findIndices(String[] headers) {
    int[] indices = new int[INDEX_NOTES + 1];
    indices[INDEX_MIC] = ArrayUtils.indexOf(headers, "MIC Code");
    indices[INDEX_NAME] = ArrayUtils.indexOf(headers, "Exchange");
    indices[INDEX_COUNTRY] = ArrayUtils.indexOf(headers, "ISO Code");
    indices[INDEX_ZONE_ID] = ArrayUtils.indexOf(headers, "Olson time zone");
    indices[INDEX_PRODUCT_GROUP] = ArrayUtils.indexOf(headers, "Group");
    indices[INDEX_PRODUCT_NAME] = ArrayUtils.indexOf(headers, "Product");
    indices[INDEX_PRODUCT_TYPE] = ArrayUtils.indexOf(headers, "Type");
    indices[INDEX_PRODUCT_CODE] = ArrayUtils.indexOf(headers, "Code");
    indices[INDEX_CALENDAR_START] = ArrayUtils.indexOf(headers, "Calendar Start");
    indices[INDEX_CALENDAR_END] = ArrayUtils.indexOf(headers, "Calendar End");
    indices[INDEX_DAY_START] = ArrayUtils.indexOf(headers, "Day Start");
    indices[INDEX_DAY_RANGE_TYPE] = ArrayUtils.indexOf(headers, "Range Type");
    indices[INDEX_DAY_END] = ArrayUtils.indexOf(headers, "Day End");
    indices[INDEX_PHASE_NAME] = ArrayUtils.indexOf(headers, "Phase");
    indices[INDEX_PHASE_TYPE] = ArrayUtils.indexOf(headers, "Phase Type");
    indices[INDEX_PHASE_START] = ArrayUtils.indexOf(headers, "Phase Starts");
    indices[INDEX_PHASE_END] = ArrayUtils.indexOf(headers, "Phase Ends");
    indices[INDEX_RANDOM_START_MIN] = ArrayUtils.indexOf(headers, "Random Start Min");
    indices[INDEX_RANDOM_START_MAX] = ArrayUtils.indexOf(headers, "Random Start Max");
    indices[INDEX_RANDOM_END_MIN] = ArrayUtils.indexOf(headers, "Random End Min");
    indices[INDEX_RANDOM_END_MAX] = ArrayUtils.indexOf(headers, "Random End Max");
    indices[INDEX_LAST_CONFIRMED] = ArrayUtils.indexOf(headers, "Last Confirmed");
    indices[INDEX_NOTES] = ArrayUtils.indexOf(headers, "Notes");
    if (ArrayUtils.contains(indices, -1)) {
      throw new OpenGammaRuntimeException("Column not found in exchange file (column must have been renamed!)");
    }
    return indices;
  }

  private void readLine(String[] rawFields, int[] indices) {
    String exchangeMIC = requiredStringField(rawFields[indices[INDEX_MIC]]);
    try {
      ExchangeDocument doc = _data.get(exchangeMIC);
      if (doc == null) {
        String countryISO = requiredStringField(rawFields[indices[INDEX_COUNTRY]]);
        String exchangeName = requiredStringField(rawFields[indices[INDEX_NAME]]);
        String timeZoneId = requiredStringField(rawFields[indices[INDEX_ZONE_ID]]);
        ExternalIdBundle id = ExternalIdBundle.of(ExternalSchemes.isoMicExchangeId(exchangeMIC));
        ExternalIdBundle region = ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.of(countryISO)));
        ZoneId timeZone = ZoneId.of(timeZoneId);
        ManageableExchange exchange = new ManageableExchange(id, exchangeName, region, timeZone);
        doc = new ExchangeDocument(exchange);
        _data.put(exchangeMIC, doc);
      }
      String timeZoneId = requiredStringField(rawFields[indices[INDEX_ZONE_ID]]);
      if (ZoneId.of(timeZoneId).equals(doc.getExchange().getTimeZone()) == false) {
        throw new OpenGammaRuntimeException("Multiple time-zone entries for exchange: " + doc.getExchange());
      }
      doc.getExchange().getDetail().add(readDetailLine(rawFields, indices));
    } catch (RuntimeException ex) {
      throw new OpenGammaRuntimeException("Error reading data for exchange: " + exchangeMIC, ex);
    }
  }

  private ManageableExchangeDetail readDetailLine(String[] rawFields, int[] indices) {
    ManageableExchangeDetail detail = new ManageableExchangeDetail();
    detail.setProductGroup(optionalStringField(rawFields[indices[INDEX_PRODUCT_GROUP]]));
    detail.setProductName(requiredStringField(rawFields[indices[INDEX_PRODUCT_NAME]]));
    detail.setProductType(optionalStringField(rawFields[indices[INDEX_PRODUCT_TYPE]])); // should be required, but isn't there on one entry.
    detail.setProductCode(optionalStringField(rawFields[indices[INDEX_PRODUCT_CODE]]));
    detail.setCalendarStart(parseDate(rawFields[indices[INDEX_CALENDAR_START]]));
    detail.setCalendarEnd(parseDate(rawFields[indices[INDEX_CALENDAR_END]]));
    detail.setDayStart(requiredStringField(rawFields[indices[INDEX_DAY_START]]));
    detail.setDayRangeType(StringUtils.trimToNull(rawFields[indices[INDEX_DAY_RANGE_TYPE]]));
    detail.setDayEnd(StringUtils.trimToNull(rawFields[indices[INDEX_DAY_END]]));
    detail.setPhaseName(optionalStringField(rawFields[indices[INDEX_PHASE_NAME]])); // nearly required, but a couple aren't
    detail.setPhaseType(optionalStringField(rawFields[indices[INDEX_PHASE_TYPE]]));
    detail.setPhaseStart(parseTime(rawFields[indices[INDEX_PHASE_START]]));
    detail.setPhaseEnd(parseTime(rawFields[indices[INDEX_PHASE_END]]));
    detail.setRandomStartMin(parseTime(rawFields[indices[INDEX_RANDOM_START_MIN]]));
    detail.setRandomStartMax(parseTime(rawFields[indices[INDEX_RANDOM_START_MAX]]));
    detail.setRandomEndMin(parseTime(rawFields[indices[INDEX_RANDOM_END_MIN]]));
    detail.setRandomEndMax(parseTime(rawFields[indices[INDEX_RANDOM_END_MAX]]));
    detail.setLastConfirmed(parseDate(rawFields[indices[INDEX_LAST_CONFIRMED]]));
    detail.setNotes(optionalStringField(rawFields[indices[INDEX_NOTES]]));
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
    ExchangeSearchRequest allSearch = new ExchangeSearchRequest();
    Map<String, UniqueId> mics = new HashMap<String, UniqueId>();
    for (ExchangeDocument doc : ExchangeSearchIterator.iterable(_exchangeMaster, allSearch)) {
      mics.put(doc.getExchange().getISOMic(), doc.getUniqueId());
    }
    
    List<String> messages = new ArrayList<String>();
    for (ExchangeDocument doc : _data.values()) {
      mics.remove(doc.getExchange().getISOMic());
      ExternalId mic = doc.getExchange().getExternalIdBundle().getExternalId(ExternalSchemes.ISO_MIC);
      ExchangeSearchRequest search = new ExchangeSearchRequest(mic);
      ExchangeSearchResult result = _exchangeMaster.search(search);
      if (result.getDocuments().size() == 0) {
        // add new data
        doc = _exchangeMaster.add(doc);
        messages.add("Added " + doc.getExchange().getISOMic() + " " + doc.getUniqueId());
      } else if (result.getDocuments().size() == 1) {
        // update from existing data
        ExchangeDocument existing = result.getFirstDocument();
        doc.setUniqueId(existing.getUniqueId());
        doc.getExchange().setUniqueId(existing.getUniqueId());
        // only update if changed
        doc.setVersionFromInstant(null);
        doc.setVersionToInstant(null);
        doc.setCorrectionFromInstant(null);
        doc.setCorrectionToInstant(null);
        existing.setVersionFromInstant(null);
        existing.setVersionToInstant(null);
        existing.setCorrectionFromInstant(null);
        existing.setCorrectionToInstant(null);
        Collections.sort(existing.getExchange().getDetail(), DETAIL_COMPARATOR);
        Collections.sort(doc.getExchange().getDetail(), DETAIL_COMPARATOR);
        if (doc.equals(existing) == false) {  // only update if changed
          messages.add("Updated " + doc.getExchange().getISOMic() + " " + doc.getUniqueId());
          doc = _exchangeMaster.update(doc);
        }
      } else {
        throw new IllegalStateException("Multiple rows in database for ISO MIC ID: " + mic.getValue());
      }
    }
    
    // do not remove exchanges, even when they disappear
//    for (UniqueId uniqueId : mics.values()) {
//      System.out.println("Removed " + uniqueId);
//      _exchangeMaster.remove(uniqueId);
//    }
    
    for (String msg : messages) {
      System.out.println(msg);
    }
  }

  //-------------------------------------------------------------------------
  private static final DetailComparator DETAIL_COMPARATOR = new DetailComparator();
  static class DetailComparator implements Comparator<ManageableExchangeDetail> {
    @Override
    public int compare(ManageableExchangeDetail detail1, ManageableExchangeDetail detail2) {
      return new CompareToBuilder()
        .append(detail1.getProductGroup(), detail2.getProductGroup())
        .append(detail1.getProductName(), detail2.getProductName())
        .append(detail1.getCalendarStart(), detail2.getCalendarStart())
        .append(detail1.getCalendarEnd(), detail2.getCalendarEnd())
        .append(detail1.getDayStart(), detail2.getDayStart())
        .append(detail1.getDayEnd(), detail2.getDayEnd())
        .append(detail1.getPhaseName(), detail2.getPhaseName())
        .append(detail1.getPhaseStart(), detail2.getPhaseStart())
        .toComparison();
    }
  }

}
