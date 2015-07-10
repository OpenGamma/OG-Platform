/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.historicaltimeseries;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_FIELDS_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_HISTORICAL_DATA_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_SECURITIES_REQUEST;
import static com.opengamma.bbg.BloombergConstants.DATA_PROVIDER_UNKNOWN;
import static com.opengamma.bbg.BloombergConstants.DEFAULT_DATA_PROVIDER;
import static com.opengamma.bbg.BloombergConstants.EID_DATA;
import static com.opengamma.bbg.BloombergConstants.ERROR_INFO;
import static com.opengamma.bbg.BloombergConstants.FIELD_DATA;
import static com.opengamma.bbg.BloombergConstants.FIELD_EXCEPTIONS;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID;
import static com.opengamma.bbg.BloombergConstants.RESPONSE_ERROR;
import static com.opengamma.bbg.BloombergConstants.SECURITY_DATA;
import static com.opengamma.bbg.BloombergConstants.SECURITY_ERROR;
import static com.opengamma.bbg.util.BloombergDataUtils.toBloombergDate;
import static com.opengamma.core.id.ExternalSchemes.BLOOMBERG_BUID;
import static com.opengamma.core.id.ExternalSchemes.BLOOMBERG_TICKER;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.LocalDate;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.AbstractBloombergStaticDataProvider;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergPermissions;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.provider.historicaltimeseries.impl.AbstractHistoricalTimeSeriesProvider;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.LocalDateRange;

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
  private final BloombergHistoricalDataRequestService _historicalDataService;
  
  /**
   * Creates an instance.
   * <p>
   * This will use the statistics tool in the connector.
   * 
   * @param bloombergConnector the bloomberg connector, not null
   */
  public BloombergHistoricalTimeSeriesProvider(BloombergConnector bloombergConnector) {
    this(ArgumentChecker.notNull(bloombergConnector, "bloombergConnector"), bloombergConnector.getReferenceDataStatistics());
  }

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector the bloomberg connector, not null
   * @param statistics the statistics, not null
   */
  public BloombergHistoricalTimeSeriesProvider(BloombergConnector bloombergConnector, BloombergReferenceDataStatistics statistics) {
    super(BLOOMBERG_DATA_SOURCE_NAME);
    _historicalDataService = new BloombergHistoricalDataRequestService(bloombergConnector, statistics);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(HistoricalTimeSeriesProviderGetRequest request) {
    fixRequestDateRange(request, DEFAULT_START_DATE);
    HistoricalTimeSeriesProviderGetResult result = _historicalDataService.doBulkGet(request.getExternalIdBundles(), request.getDataProvider(), request.getDataField(), request.getDateRange(),
        request.getMaxPoints());
    return filterResult(result, request.getDateRange(), request.getMaxPoints());
  }

  //-------------------------------------------------------------------------
  @Override
  public void start() {
    _historicalDataService.start();
  }

  @Override
  public void stop() {
    _historicalDataService.stop();
  }

  @Override
  public boolean isRunning() {
    return _historicalDataService.isRunning();
  }

  static class BloombergHistoricalDataRequestService extends AbstractBloombergStaticDataProvider {

    /**
     * The format of error messages.
     */
    private static final String ERROR_MESSAGE_FORMAT = "{0}:{1}/{2} - {3}";

    /**
     * Bloomberg statistics.
     */
    private final BloombergReferenceDataStatistics _statistics;

    BloombergHistoricalDataRequestService(BloombergConnector bloombergConnector) {
      this(ArgumentChecker.notNull(bloombergConnector, "bloombergConnector"), bloombergConnector.getReferenceDataStatistics());
    }

    /**
     * Creates an instance.
     * 
     * @param bloombergConnector the bloomberg connector, not null
     * @param statistics the statistics, not null
     * @param applicationName the bpipe application name if applicable
     * @param reAuthorizationScheduleTime the identity re authorization schedule time in hours
     */
    BloombergHistoricalDataRequestService(BloombergConnector bloombergConnector, BloombergReferenceDataStatistics statistics) {
      super(bloombergConnector, BloombergConstants.REF_DATA_SVC_NAME);
      ArgumentChecker.notNull(statistics, "statistics");
      _statistics = statistics;
    }

    //-------------------------------------------------------------------------
    @Override
    protected Logger getLogger() {
      return s_logger;
    }

    //-------------------------------------------------------------------------
    /**
     * Get time-series from Bloomberg.
     * 
     * @param externalIdBundle the identifier bundle, not null
     * @param dataProvider the data provider, not null
     * @param dataField the dataField, not null
     * @param dateRange the date range to obtain, not null
     * @param maxPoints the maximum number of points required, negative back from the end date, null for all
     * @return a map of each supplied identifier bundle to the corresponding time-series, not null
     */
    public HistoricalTimeSeriesProviderGetResult doBulkGet(Set<ExternalIdBundle> externalIdBundle, String dataProvider, String dataField, LocalDateRange dateRange, Integer maxPoints) {

      ensureStarted();
      getLogger().debug("Getting historical data for {}", externalIdBundle);
      if (externalIdBundle.isEmpty()) {
        getLogger().info("Historical data request for empty identifier set");
        return new HistoricalTimeSeriesProviderGetResult();
      }

      Map<String, ExternalIdBundle> reverseBundleMap = Maps.newHashMap();
      Request request = createRequest(externalIdBundle, dataProvider, dataField, dateRange, maxPoints, reverseBundleMap);
      _statistics.recordStatistics(reverseBundleMap.keySet(), Collections.singleton(dataField));
      HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
      try {
        List<Element> responseElements = submitRequest(request).get();
        final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> tsMap = extractTimeSeries(externalIdBundle, dataField, reverseBundleMap, responseElements);
        final Map<ExternalIdBundle, Set<String>> permissions = extractPermissions(reverseBundleMap, responseElements);
        if (tsMap != null) {
          result = permissions == null ? new HistoricalTimeSeriesProviderGetResult(tsMap) : new HistoricalTimeSeriesProviderGetResult(tsMap, permissions);
        }
      } catch (InterruptedException | ExecutionException ex) {
        getLogger().warn(String.format("Error getting bulk historical data for %s %s %s %s %s", externalIdBundle, dataProvider, dataField, dateRange, maxPoints), ex);
      }
      return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Creates the Bloomberg request.
     * 
     * @param externalIdBundle the external bundles, not null
     * @param dataProvider the data provider, not null
     * @param dataField the data field, not null
     * @param dateRange the date range, not null
     * @param maxPoints the maximum points
     * @param reverseBundleMap the reverse bundle map, not null
     * 
     * @return the bloomberg request
     */
    protected Request createRequest(Set<ExternalIdBundle> externalIdBundle, String dataProvider, String dataField, LocalDateRange dateRange, Integer maxPoints,
        Map<String, ExternalIdBundle> reverseBundleMap) {

      // create request
      Request request = getService().createRequest(BLOOMBERG_HISTORICAL_DATA_REQUEST);
      Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);

      // identifiers
      for (ExternalIdBundle identifiers : externalIdBundle) {
        ExternalId preferredId = getPreferredIdentifier(identifiers, dataProvider);
        getLogger().debug("Resolved preferred identifier {} from identifier bundle {}", preferredId, identifiers);
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
      request.set("startDate", toBloombergDate(dateRange.getStartDateInclusive()));
      if (!dateRange.isEndDateMaximum()) {
        request.set("endDate", toBloombergDate(dateRange.getEndDateInclusive()));
      }
      request.set("adjustmentSplit", true);
      if (maxPoints != null && maxPoints <= 0) {
        request.set("maxDataPoints", -maxPoints);
      }
      request.set("returnEids", true);
      return request;
    }

    private ExternalId getPreferredIdentifier(final ExternalIdBundle identifiers, final String dataProvider) {
      ExternalId preferredId = null;
      if (dataProvider == null || dataProvider.equalsIgnoreCase(DATA_PROVIDER_UNKNOWN) || dataProvider.equalsIgnoreCase(DEFAULT_DATA_PROVIDER)) {
        preferredId = identifiers.getExternalId(BLOOMBERG_BUID);
      }
      if (preferredId == null) {
        Set<ExternalId> tickers = identifiers.getExternalIds(BLOOMBERG_TICKER);
        if (tickers == null || tickers.size() == 0) {
          preferredId = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(identifiers);
        } else if (tickers.size() == 1) {
          preferredId = tickers.iterator().next();
        } else { // multiple matches, find the shortest code and use that.
          int minLength = Integer.MAX_VALUE;
          for (ExternalId id : tickers) {
            if (id.getValue().length() <= minLength) {
              preferredId = id;
              minLength = id.getValue().length();
            }
          }
        }
      }
      if (preferredId == null) {
        throw new OpenGammaRuntimeException("Couldn't establish preferred identifier, this should not happen and indicates a code logic error");
      }
      return preferredId;
    }

    /**
     * Convert response to time-series.
     */
    private Map<ExternalIdBundle, LocalDateDoubleTimeSeries> extractTimeSeries(Set<ExternalIdBundle> externalIdBundle, String dataField,
        Map<String, ExternalIdBundle> reverseBundleMap, List<Element> resultElements) {

      // handle empty case
      if (resultElements == null || resultElements.isEmpty()) {
        getLogger().warn("Unable to get historical data for {}", externalIdBundle);
        return null;
      }

      // parse data
      Map<ExternalIdBundle, LocalDateDoubleTimeSeriesBuilder> result = Maps.newHashMap();
      for (Element resultElem : resultElements) {
        if (resultElem.hasElement(RESPONSE_ERROR)) {
          getLogger().warn("Response error");
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
            getLogger().warn("Field error on {}", fieldId);
            Element errorInfo = fieldException.getElement(ERROR_INFO);
            extractError(errorInfo);
          }
        }
        if (securityElem.hasElement(FIELD_DATA)) {
          extractFieldData(securityElem, dataField, reverseBundleMap, result);
        }
      }
      if (externalIdBundle.size() != result.size()) {
        getLogger().warn("Failed to get time series results for ({}/{}) {}", externalIdBundle.size() - result.size(), externalIdBundle.size(),
            Sets.difference(externalIdBundle, result.keySet()));
      }
      return convertResult(result);
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    private static Map<ExternalIdBundle, LocalDateDoubleTimeSeries> convertResult(Map result) {
      // ignore generics, which is safe as of JDK8
      for (Object o : result.entrySet()) {
        Entry entry = (Entry) o;
        LocalDateDoubleTimeSeriesBuilder bld = (LocalDateDoubleTimeSeriesBuilder) entry.getValue();
        entry.setValue(bld.build());
      }
      return (Map<ExternalIdBundle, LocalDateDoubleTimeSeries>) result;
    }

    /**
     * Extracts time-series.
     */
    private void extractFieldData(Element securityElem, String field, Map<String, ExternalIdBundle> reverseBundleMap, Map<ExternalIdBundle, LocalDateDoubleTimeSeriesBuilder> result) {

      String secDes = securityElem.getElementAsString(BloombergConstants.SECURITY);
      ExternalIdBundle identifiers = reverseBundleMap.get(secDes);
      if (identifiers == null) {
        String message = "Found time series data for unrecognized security" + secDes + " " + reverseBundleMap;
        throw new OpenGammaRuntimeException(message);
      }
      LocalDateDoubleTimeSeriesBuilder bld = result.get(identifiers);
      if (bld == null) {
        bld = ImmutableLocalDateDoubleTimeSeries.builder();
        result.put(identifiers, bld);
      }
      Element fieldDataArray = securityElem.getElement(FIELD_DATA);

      int numValues = fieldDataArray.numValues();
      for (int i = 0; i < numValues; i++) {
        Element fieldData = fieldDataArray.getValueAsElement(i);
        Datetime date = fieldData.getElementAsDate("date");
        LocalDate ldate = LocalDate.of(date.year(), date.month(), date.dayOfMonth());
        double lastPrice = fieldData.getElementAsFloat64(field);
        bld.put(ldate, lastPrice);
      }
    }

    /**
     * Process an error.
     * 
     * @param element the error element, not null
     */
    private static void extractError(Element element) {
      int code = element.getElementAsInt32("code");
      String category = element.getElementAsString("category");
      String subcategory = element.getElementAsString("subcategory");
      String message = element.getElementAsString("message");

      String errorMessage = MessageFormat.format(ERROR_MESSAGE_FORMAT, code, category, subcategory, message);
      s_logger.warn(errorMessage);
    }

    protected Map<ExternalIdBundle, Set<String>> extractPermissions(Map<String, ExternalIdBundle> reverseBundleMap, List<Element> responseElements) {
      final Map<ExternalIdBundle, Set<String>> result = new HashMap<>();
      for (Element resultElem : responseElements) {
        if (resultElem.hasElement(SECURITY_DATA)) {
          Element securityElem = resultElem.getElement(SECURITY_DATA);
          String secDes = securityElem.getElementAsString(BloombergConstants.SECURITY);
          ExternalIdBundle identifiers = reverseBundleMap.get(secDes);
          if (identifiers != null) {
            if (securityElem.hasElement(EID_DATA)) {
              Element eidData = securityElem.getElement(EID_DATA);
              Set<String> eids = new HashSet<>();
              int numValues = eidData.numValues();
              for (int i = 0; i < numValues; i++) {
                try {
                  int eid = eidData.getValueAsInt32(i);
                  eids.add(BloombergPermissions.createEidPermissionString(eid));
                } catch (Exception ex) {
                  getLogger().warn("Error extracting EID from {} for security:{}", eidData, identifiers);
                }
              }
              getLogger().debug("EIDS {} return for security {}", eids, identifiers);
              result.put(identifiers, eids);
            }
          }
        }
      }
      return result;
    }
  }

}
