/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_FIELDS_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_HISTORICAL_DATA_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_SECURITIES_REQUEST;
import static com.opengamma.bbg.BloombergConstants.ERROR_INFO;
import static com.opengamma.bbg.BloombergConstants.FIELD_DATA;
import static com.opengamma.bbg.BloombergConstants.FIELD_EXCEPTIONS;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID;
import static com.opengamma.bbg.BloombergConstants.RESPONSE_ERROR;
import static com.opengamma.bbg.BloombergConstants.SECURITY_DATA;
import static com.opengamma.bbg.BloombergConstants.SECURITY_ERROR;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.SessionOptions;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Loads time-series from Bloomberg.
 */
public class BloombergHistoricalTimeSeriesSource extends AbstractBloombergStaticDataProvider implements HistoricalTimeSeriesSource {

  /**
   * Default start date for loading time-series
   */
  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1900, MonthOfYear.JANUARY, 01);

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalTimeSeriesSource.class);
  /**
   * The format of error messages.
   */
  private static final String ERROR_MESSAGE_FORMAT = "{0}:{1}/{2} - {3}";
  /**
   * The Bloomberg service.
   */
  private static final UniqueIdSupplier UID_SUPPLIER = new UniqueIdSupplier("BbgHTS");

  /**
   * The Bloomberg service.
   */
  private Service _refDataService;

  private final BloombergReferenceDataStatistics _statistics;
  
  /**
   * Creates an instance with session options.
   * 
   * @param sessionOptions  the Bloomberg session options, not null
   */
  public BloombergHistoricalTimeSeriesSource(SessionOptions sessionOptions) {
    this(sessionOptions, NullBloombergReferenceDataStatistics.INSTANCE);
  }
  
  /**
   * Creates an instance with session options.
   * 
   * @param sessionOptions  the Bloomberg session options, not null
   * @param statistics the statistics to collect
   */
  public BloombergHistoricalTimeSeriesSource(SessionOptions sessionOptions, BloombergReferenceDataStatistics statistics) {
    super(sessionOptions);
    _statistics = statistics;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Logger getLogger() {
    return s_logger;
  }

  @Override
  protected void openServices() {
    Service refDataService = openService(BloombergConstants.REF_DATA_SVC_NAME);
    setRefDataService(refDataService);
  }

  private void setRefDataService(Service refDataService) {
    _refDataService = refDataService;
  }

  private Service getRefDataService() {
    return _refDataService;
  }

  //-------------------------------------------------------------------------
  private Request composeRequest(final String identifier, final String dataSource, final String dataProvider, 
      final String field, LocalDate startDate, LocalDate endDate, Integer maxPoints) {
    ArgumentChecker.notNull(identifier, "identifier must not be null or empty");
    
    Request request = getRefDataService().createRequest(BLOOMBERG_HISTORICAL_DATA_REQUEST);
    Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);
    securitiesElem.appendValue(identifier);
    
    Element fieldElem = request.getElement(BLOOMBERG_FIELDS_REQUEST);
    
    fieldElem.appendValue(field == null ? "PX_LAST" : field);

    request.set("periodicityAdjustment", "ACTUAL");
    request.set("periodicitySelection", "DAILY");
    request.set("startDate", DateUtils.printYYYYMMDD(startDate));
    request.set("endDate", DateUtils.printYYYYMMDD(endDate));
    request.set("returnEids", true);
 
    if (maxPoints != null && maxPoints <= 0) {
      request.set("maxDataPoints", -maxPoints);
    }
    
    return request;
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String field, LocalDate startDate, LocalDate endDate, Integer maxPoints) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(startDate, "startDate");
    ArgumentChecker.notNull(endDate, "endDate");
    Validate.isTrue(ObjectUtils.equals(dataSource, BLOOMBERG_DATA_SOURCE_NAME), getClass().getName() + "cannot support " + dataSource);
    
    if (maxPoints != null && maxPoints > 0) {
      throw new UnsupportedOperationException("Fetching a bounded number of points from the start of a Bloomberg time-series is unsupported");
    }
    
    s_logger.info("Getting HistoricalTimeSeries for security {}", identifiers);
    
    ensureStarted();
    
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("endDate must be after startDate");
    }
    ExternalId dsid = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(identifiers);
    String bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(dsid, dataProvider);
    Request request = composeRequest(bbgKey, dataSource, dataProvider, field, startDate, endDate, maxPoints);
    _statistics.gotFields(Collections.singleton(bbgKey), Collections.singleton(field));
    LocalDateDoubleTimeSeries timeSeries = processRequest(bbgKey, request, field);
    return new SimpleHistoricalTimeSeries(UID_SUPPLIER.get(), timeSeries);
  }

  private LocalDateDoubleTimeSeries processRequest(String identifier, Request request, String field) {
    CorrelationID cid = submitBloombergRequest(request);
    BlockingQueue<Element> resultElements = getResultElement(cid);
    
    if (resultElements == null || resultElements.isEmpty()) {
      s_logger.info("Unable to get HistoricalTimeSeries for {}", identifier);
      return null;
    }
    List<LocalDate> dates = new ArrayList<LocalDate>();
    List<Double> values = new ArrayList<Double>();
    for (Element resultElem : resultElements) {
      if (resultElem.hasElement(RESPONSE_ERROR)) {
        s_logger.warn("Response error");
        processError(resultElem.getElement(RESPONSE_ERROR));
      }
      Element securityElem = resultElem.getElement(SECURITY_DATA);
      if (securityElem.hasElement(SECURITY_ERROR)) {
        processError(securityElem.getElement(SECURITY_ERROR));
        return null;
      }
      if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
        Element fieldExceptions = securityElem.getElement(FIELD_EXCEPTIONS);
        
        for (int i = 0; i < fieldExceptions.numValues(); i++) {
          Element fieldException = fieldExceptions.getValueAsElement(i);
          String fieldId = fieldException.getElementAsString(FIELD_ID);
          s_logger.warn("Field error on {}", fieldId);
          Element errorInfo = fieldException.getElement(ERROR_INFO);
          processError(errorInfo);
        }
      }
      if (securityElem.hasElement(FIELD_DATA)) {
        processFieldData(securityElem.getElement(FIELD_DATA), field, dates, values);
      }
    }
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  private void processFieldData(Element securityElem, String field, Map<String, ExternalIdBundle> bbgSecDomainMap, Map<ExternalIdBundle, HistoricalTimeSeries> result) {
    String secDes = securityElem.getElementAsString(BloombergConstants.SECURITY);
    ExternalIdBundle identifiers = bbgSecDomainMap.get(secDes);
    if (identifiers == null) {
      String message = "Found time series data for unrecognized security" + secDes + " " + bbgSecDomainMap;
      throw new OpenGammaRuntimeException(message);
    }
    HistoricalTimeSeries hts = result.get(identifiers);
    MapLocalDateDoubleTimeSeries timeSeries;
    if (hts == null) {
      timeSeries = new MapLocalDateDoubleTimeSeries();
      hts = new SimpleHistoricalTimeSeries(UID_SUPPLIER.get(), timeSeries);
      result.put(identifiers, hts);
    } else {
      timeSeries = (MapLocalDateDoubleTimeSeries) hts.getTimeSeries();
    }
    Element fieldDataArray = securityElem.getElement(FIELD_DATA);
    int numValues = fieldDataArray.numValues();
    for (int i = 0; i < numValues; i++) {
      Element fieldData = fieldDataArray.getValueAsElement(i);
      Datetime date = fieldData.getElementAsDate("date");
      double lastPrice = fieldData.getElementAsFloat64(field);
      int year = date.year();
      int month = date.month();
      int day = date.dayOfMonth();
      timeSeries.putDataPoint(LocalDate.of(year, month, day), lastPrice);
    }
  }

  private String printYYYYMMDD(LocalDate localDate) {
    String formatted = DateTimeFormatters.isoLocalDate().print(localDate);
    return StringUtils.remove(formatted, '-');
  }

  protected void processFieldData(final Element fieldDataArray, String field, final List<LocalDate> dates, final List<Double> values) {
    int numValues = fieldDataArray.numValues();
    for (int i = 0; i < numValues; i++) {
      Element fieldData = fieldDataArray.getValueAsElement(i);
      Datetime date = fieldData.getElementAsDate("date");
      double fieldValue = fieldData.getElementAsFloat64(field);
      dates.add(LocalDate.of(date.year(), date.month(), date.dayOfMonth()));
      values.add(fieldValue);
    }
  }

  protected void processError(Element element) {
    int code = element.getElementAsInt32("code");
    String category = element.getElementAsString("category");
    String subcategory = element.getElementAsString("subcategory");
    String message = element.getElementAsString("message");
    
    String errorMessage = MessageFormat.format(ERROR_MESSAGE_FORMAT, code, category, subcategory, message);
    s_logger.warn(errorMessage);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final ExternalIdBundle identifiers, final String dataSource, final String dataProvider, final String dataField) {
    LocalDate yesterday = DateUtils.previousWeekDay();
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, DEFAULT_START_DATE, yesterday, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      final ExternalIdBundle identifiers, final String dataSource, final String dataProvider, final String dataField,
      final LocalDate start, boolean includeStart, final LocalDate end, boolean includeEnd) {
    LocalDate resolvedStart = null;
    if (!includeStart && start != null) {
      resolvedStart = start.plusDays(1);
    } else {
      resolvedStart = start;
    }
    LocalDate resolvedEnd = null;
    if (!includeEnd && end != null) {
      resolvedEnd = end.minusDays(1);
    } else {
      resolvedEnd = end;
    }
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, resolvedStart, resolvedEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    LocalDate resolvedStart = null;
    if (!includeStart && start != null) {
      resolvedStart = start.plusDays(1);
    } else {
      resolvedStart = start;
    }
    LocalDate resolvedEnd = null;
    if (!includeEnd && end != null) {
      resolvedEnd = end.minusDays(1);
    } else {
      resolvedEnd = end;
    }
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, resolvedStart, resolvedEnd, maxPoints);    
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
      LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(identifierSet, "identifierSet");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(start, "start"); 
    ArgumentChecker.notNull(end, "end");
    Validate.isTrue(ObjectUtils.equals(dataSource, BLOOMBERG_DATA_SOURCE_NAME), getClass().getName() + "cannot support " + dataSource);
    
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("end must be after start");
    }
    
    ensureStarted();
    s_logger.debug("Getting historical data for {}", identifierSet);
    
    if (identifierSet.isEmpty()) {
      s_logger.info("Historical data request for empty identifier set");
      return Collections.emptyMap();
    }
    Map<String, ExternalIdBundle> bbgSecDomainMap = new HashMap<String, ExternalIdBundle>();
    Request request = getRefDataService().createRequest(BLOOMBERG_HISTORICAL_DATA_REQUEST);
    Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);
    for (ExternalIdBundle identifiers : identifierSet) {
      ExternalId preferredIdentifier = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(identifiers);
      s_logger.debug("Resolved preferred identifier {} from identifier bundle {}", preferredIdentifier, identifiers);
      String bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(preferredIdentifier, dataProvider);
      securitiesElem.appendValue(bbgKey);
      bbgSecDomainMap.put(bbgKey, identifiers);
    }
    
    Element fieldElem = request.getElement(BLOOMBERG_FIELDS_REQUEST);
    fieldElem.appendValue(dataField);
    
    // TODO: inclusive start / exclusive end
    request.set("periodicityAdjustment", "ACTUAL");
    request.set("periodicitySelection", "DAILY");
    request.set("startDate", printYYYYMMDD(start));
    request.set("endDate", printYYYYMMDD(end));
    request.set("adjustmentSplit", true);
    
    _statistics.gotFields(bbgSecDomainMap.keySet(), Collections.singleton(dataField));
    CorrelationID cid = submitBloombergRequest(request);
    BlockingQueue<Element> resultElements = getResultElement(cid);
    if (resultElements == null || resultElements.isEmpty()) {
      s_logger.warn("Unable to get historical data for {}", identifierSet);
      return null;
    }
    
    //REVIEW simon 2011/11/01: should this be deduped with the single case? 
    Map<ExternalIdBundle, HistoricalTimeSeries> result = new HashMap<ExternalIdBundle, HistoricalTimeSeries>(); 
    for (Element resultElem : resultElements) {
      if (resultElem.hasElement(RESPONSE_ERROR)) {
        s_logger.warn("Response error");
        processError(resultElem.getElement(RESPONSE_ERROR));
        continue;
      }
      Element securityElem = resultElem.getElement(SECURITY_DATA);
      if (securityElem.hasElement(SECURITY_ERROR)) {
        processError(securityElem.getElement(SECURITY_ERROR));
      }
      if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
        Element fieldExceptions = securityElem.getElement(FIELD_EXCEPTIONS);
        
        for (int i = 0; i < fieldExceptions.numValues(); i++) {
          Element fieldException = fieldExceptions.getValueAsElement(i);
          String fieldId = fieldException.getElementAsString(FIELD_ID);
          s_logger.warn("Field error on {}", fieldId);
          Element errorInfo = fieldException.getElement(ERROR_INFO);
          processError(errorInfo);
        }
      }
      if (securityElem.hasElement(FIELD_DATA)) {
        processFieldData(securityElem, dataField, bbgSecDomainMap, result);
      }
    }
    if (identifierSet.size() != result.size()) {
      s_logger.warn("Failed to get time series results for ({}/{}) {}", 
          new Object[]{identifierSet.size() - result.size(), identifierSet.size(), Sets.difference(identifierSet, result.keySet())});
    }
    return result;
  }

}
