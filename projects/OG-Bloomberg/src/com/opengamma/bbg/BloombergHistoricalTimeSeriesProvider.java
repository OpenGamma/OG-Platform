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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.financial.provider.historicaltimeseries.impl.AbstractHistoricalTimeSeriesProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Provider of time-series from the Bloomberg data source.
 */
public class BloombergHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider implements Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalTimeSeriesProvider.class);
  /**
   * Default start date for loading time-series
   */
  private static final LocalDate DEFAULT_START_DATE = LocalDate.of(1900, 1, 1);

  /**
   * Implementation class.
   */
  private final BloombergHistoricalTimeSeriesProviderImpl _impl;

  /**
   * Creates an instance.
   * <p>
   * This will use the statistics tool in the connector.
   * 
   * @param bloombergConnector  the Bloomberg connector, not null
   */
  public BloombergHistoricalTimeSeriesProvider(BloombergConnector bloombergConnector) {
    this(bloombergConnector, bloombergConnector.getReferenceDataStatistics());
  }

  /**
   * Creates an instance with statistics gathering.
   * 
   * @param bloombergConnector  the Bloomberg connector, not null
   * @param statistics  the statistics to collect, not null
   */
  public BloombergHistoricalTimeSeriesProvider(BloombergConnector bloombergConnector, BloombergReferenceDataStatistics statistics) {
    super(BLOOMBERG_DATA_SOURCE_NAME, DEFAULT_START_DATE);
    _impl = new BloombergHistoricalTimeSeriesProviderImpl(bloombergConnector, statistics);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(
      Set<ExternalIdBundle> externalIdBundleSet, String dataProvider, String dataField, LocalDateRange dateRange, boolean isLatestOnly) {
    
    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> map = _impl.doBulkGet(externalIdBundleSet, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult(map);
    return filterBulkDates(result, dateRange, isLatestOnly);
  }

  //-------------------------------------------------------------------------
  @Override
  public void start() {
    _impl.start();
  }

  @Override
  public void stop() {
    _impl.stop();
  }

  @Override
  public boolean isRunning() {
    return _impl.isRunning();
  }

  //-------------------------------------------------------------------------
  /**
   * Loads time-series from Bloomberg.
   */
  static class BloombergHistoricalTimeSeriesProviderImpl extends AbstractBloombergStaticDataProvider {

    /**
     * The format of error messages.
     */
    private static final String ERROR_MESSAGE_FORMAT = "{0}:{1}/{2} - {3}";

    /**
     * Bloomberg statistics.
     */
    private final BloombergReferenceDataStatistics _statistics;
    /**
     * The Bloomberg service.
     */
    private Service _refDataService;

    /**
     * Creates an instance.
     * 
     * @param provider  the provider, not null
     */
    public BloombergHistoricalTimeSeriesProviderImpl(BloombergConnector bloombergConnector, BloombergReferenceDataStatistics statistics) {
      super(bloombergConnector);
      ArgumentChecker.notNull(statistics, "statistics");
      _statistics = statistics;
    }

    //-------------------------------------------------------------------------
    @Override
    protected Logger getLogger() {
      return s_logger;
    }

    @Override
    protected void openServices() {
      _refDataService = openService(BloombergConstants.REF_DATA_SVC_NAME);
    }

    //-------------------------------------------------------------------------
    /**
     * Get time-series from Bloomberg.
     * 
     * @param externalIdBundle  the identifier bundle, not null
     * @param dataProvider  the data provider, not null
     * @param dataField  the dataField, not null
     * @param dateRange  the date range to obtain, not null
     * @return a map of each supplied identifier bundle to the corresponding time-series, not null
     */
    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> doBulkGet(
        Set<ExternalIdBundle> externalIdBundle, String dataProvider, String dataField, LocalDateRange dateRange) {
      
      ensureStarted();
      s_logger.debug("Getting historical data for {}", externalIdBundle);
      if (externalIdBundle.isEmpty()) {
        s_logger.info("Historical data request for empty identifier set");
        return Collections.emptyMap();
      }
      
      Map<String, ExternalIdBundle> reverseBundleMap = Maps.newHashMap();
      Request request = createRequest(externalIdBundle, dataProvider, dataField, dateRange, reverseBundleMap);
      _statistics.recordStatistics(reverseBundleMap.keySet(), Collections.singleton(dataField));
      BlockingQueue<Element> responseElements = callBloomberg(request);
      return extractTimeSeries(externalIdBundle, dataField, reverseBundleMap, responseElements);
    }

    //-------------------------------------------------------------------------
    /**
     * Creates the Bloomberg request.
     */
    private Request createRequest(
        Set<ExternalIdBundle> externalIdBundle, String dataProvider, String dataField, LocalDateRange dateRange,
        Map<String, ExternalIdBundle> reverseBundleMap) {
      
      // create request
      Request request = _refDataService.createRequest(BLOOMBERG_HISTORICAL_DATA_REQUEST);
      Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);
      
      // identifiers
      for (ExternalIdBundle identifiers : externalIdBundle) {
        ExternalId preferredId = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(identifiers);
        s_logger.debug("Resolved preferred identifier {} from identifier bundle {}", preferredId, identifiers);
        String bbgKey = BloombergDomainIdentifierResolver.toBloombergKeyWithDataProvider(preferredId, dataProvider);
        securitiesElem.appendValue(bbgKey);
        reverseBundleMap.put(bbgKey, identifiers);
      }
      
      // field required
      Element fieldElem = request.getElement(BLOOMBERG_FIELDS_REQUEST);
      fieldElem.appendValue(dataField);
      
      // general settings
      request.set("periodicityAdjustment", "ACTUAL");
      request.set("periodicitySelection", "DAILY");
      request.set("startDate", dateRange.getStartDateInclusive().toString(DateTimeFormatters.basicIsoDate()));
      request.set("endDate", dateRange.getEndDateInclusive().toString(DateTimeFormatters.basicIsoDate()));
      request.set("adjustmentSplit", true);
      request.set("returnEids", true);
      return request;
    }

    //-------------------------------------------------------------------------
    /**
     * Call Bloomberg.
     * 
     * @param request  the request, not null
     * @return the response, may be null
     */
    private BlockingQueue<Element> callBloomberg(Request request) {
      CorrelationID cid = submitBloombergRequest(request);
      return getResultElement(cid);
    }

    //-------------------------------------------------------------------------
    /**
     * Convert response to time-series.
     */
    private static Map<ExternalIdBundle, LocalDateDoubleTimeSeries> extractTimeSeries(
        Set<ExternalIdBundle> externalIdBundle, String dataField, Map<String, ExternalIdBundle> reverseBundleMap, BlockingQueue<Element> resultElements) {
      
      // handle empty case
      if (resultElements == null || resultElements.isEmpty()) {
        s_logger.warn("Unable to get historical data for {}", externalIdBundle);
        return null;
      }
      
      // parse data
      Map<ExternalIdBundle, LocalDateDoubleTimeSeries> result = Maps.newHashMap();
      for (Element resultElem : resultElements) {
        if (resultElem.hasElement(RESPONSE_ERROR)) {
          s_logger.warn("Response error");
          extractError(resultElem.getElement(RESPONSE_ERROR));
          continue;
        }
        Element securityElem = resultElem.getElement(SECURITY_DATA);
        if (securityElem.hasElement(SECURITY_ERROR)) {
          extractError(securityElem.getElement(SECURITY_ERROR));
        }
        if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
          Element fieldExceptions = securityElem.getElement(FIELD_EXCEPTIONS);
          
          for (int i = 0; i < fieldExceptions.numValues(); i++) {
            Element fieldException = fieldExceptions.getValueAsElement(i);
            String fieldId = fieldException.getElementAsString(FIELD_ID);
            s_logger.warn("Field error on {}", fieldId);
            Element errorInfo = fieldException.getElement(ERROR_INFO);
            extractError(errorInfo);
          }
        }
        if (securityElem.hasElement(FIELD_DATA)) {
          extractFieldData(securityElem, dataField, reverseBundleMap, result);
        }
      }
      if (externalIdBundle.size() != result.size()) {
        s_logger.warn("Failed to get time series results for ({}/{}) {}", 
            new Object[]{externalIdBundle.size() - result.size(), externalIdBundle.size(), Sets.difference(externalIdBundle, result.keySet())});
      }
      return result;
    }

    /**
     * Extracts time-series.
     */
    private static void extractFieldData(
        Element securityElem, String field, Map<String, ExternalIdBundle> reverseBundleMap, Map<ExternalIdBundle, LocalDateDoubleTimeSeries> result) {
      
      String secDes = securityElem.getElementAsString(BloombergConstants.SECURITY);
      ExternalIdBundle identifiers = reverseBundleMap.get(secDes);
      if (identifiers == null) {
        String message = "Found time series data for unrecognized security" + secDes + " " + reverseBundleMap;
        throw new OpenGammaRuntimeException(message);
      }
      LocalDateDoubleTimeSeries hts = result.get(identifiers);
      MapLocalDateDoubleTimeSeries timeSeries;
      if (hts == null) {
        timeSeries = new MapLocalDateDoubleTimeSeries();
        result.put(identifiers, timeSeries);
      } else {
        timeSeries = (MapLocalDateDoubleTimeSeries) hts;
      }
      Element fieldDataArray = securityElem.getElement(FIELD_DATA);
      
      int numValues = fieldDataArray.numValues();
      for (int i = 0; i < numValues; i++) {
        Element fieldData = fieldDataArray.getValueAsElement(i);
        Datetime date = fieldData.getElementAsDate("date");
        LocalDate ldate = LocalDate.of(date.year(), date.month(), date.dayOfMonth());
        double lastPrice = fieldData.getElementAsFloat64(field);
        timeSeries.putDataPoint(ldate, lastPrice);
      }
    }

    /**
     * Process an error.
     * 
     * @param element  the error element, not null
     */
    private static void extractError(Element element) {
      int code = element.getElementAsInt32("code");
      String category = element.getElementAsString("category");
      String subcategory = element.getElementAsString("subcategory");
      String message = element.getElementAsString("message");
      
      String errorMessage = MessageFormat.format(ERROR_MESSAGE_FORMAT, code, category, subcategory, message);
      s_logger.warn(errorMessage);
    }
  }

}
