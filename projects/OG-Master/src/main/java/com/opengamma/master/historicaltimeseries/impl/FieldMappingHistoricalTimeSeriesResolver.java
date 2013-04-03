/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * Historical time series resolver that can expose synthetic data fields by mapping these onto a real, underlying data
 * field together with an optional adjuster.
 */
public class FieldMappingHistoricalTimeSeriesResolver extends DefaultHistoricalTimeSeriesResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(FieldMappingHistoricalTimeSeriesResolver.class);
  
  private final Map<String, HistoricalTimeSeriesFieldAdjustmentMap> _fieldMaps;
  
  public FieldMappingHistoricalTimeSeriesResolver(Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps, HistoricalTimeSeriesSelector selector, HistoricalTimeSeriesMaster master) {
    super(selector, master);
    _fieldMaps = getFieldMaps(fieldMaps);
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    // Apply any field mappings
    Map<String, HistoricalTimeSeriesFieldAdjustment> fieldMappings = getFieldAdjustments(dataSource, dataField);
    if (fieldMappings.size() == 1) {
      // Optimisation - might as well restrict the search results
      Map.Entry<String, HistoricalTimeSeriesFieldAdjustment> fieldMappingEntry = Iterables.getOnlyElement(fieldMappings.entrySet());
      dataSource = fieldMappingEntry.getKey();
      dataProvider = fieldMappingEntry.getValue().getUnderlyingDataProvider();
      dataField = fieldMappingEntry.getValue().getUnderlyingDataField();
    } else if (fieldMappings.size() > 1) {
      // Could have been mapped to multiple underlying providers/fields
      dataField = null;
      dataProvider = null;
    }
    if (identifierBundle != null) {
      Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates = search(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField);
      if (!fieldMappings.isEmpty()) {
        Iterator<ManageableHistoricalTimeSeriesInfo> it = timeSeriesCandidates.iterator();
        while (it.hasNext()) {
          ManageableHistoricalTimeSeriesInfo candidate = it.next();
          HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMappings.get(candidate.getDataSource());
          if (fieldAdjustment == null ||
              ((fieldAdjustment.getUnderlyingDataProvider() != null && !fieldAdjustment.getUnderlyingDataProvider().equals(candidate.getDataProvider()))
              || !fieldAdjustment.getUnderlyingDataField().equals(candidate.getDataField()))) {
            // Incompatible
            it.remove();
          }
        }
      }
      ManageableHistoricalTimeSeriesInfo selectedResult = select(timeSeriesCandidates, resolutionKey);
      if (selectedResult == null) {
        s_logger.debug("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, dataField, resolutionKey });
        return null;
      }
      HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMappings.get(selectedResult.getDataSource());
      HistoricalTimeSeriesAdjuster adjuster = fieldAdjustment != null ? fieldAdjustment.getAdjuster() : null;
      return new HistoricalTimeSeriesResolutionResult(selectedResult, adjuster);
    } else {
      return search(dataSource, dataProvider, dataField);
    }
  }
  
  public Collection<HistoricalTimeSeriesFieldAdjustmentMap> getFieldMaps() {
    return _fieldMaps.values();
  }
  
  //-------------------------------------------------------------------------
  private Map<String, HistoricalTimeSeriesFieldAdjustmentMap> getFieldMaps(Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps) {
    Map<String, HistoricalTimeSeriesFieldAdjustmentMap> result = new HashMap<String, HistoricalTimeSeriesFieldAdjustmentMap>();
    for (HistoricalTimeSeriesFieldAdjustmentMap fieldMap : fieldMaps) {
      if (result.put(fieldMap.getDataSource(), fieldMap) != null) {
        throw new IllegalArgumentException("Only one field map per data source is permitted. Found multiple for data source " + fieldMap.getDataSource());
      }
    }
    return result;
  }
  
  private Map<String, HistoricalTimeSeriesFieldAdjustment> getFieldAdjustments(String dataSource, String dataField) {
    if (dataSource != null) {
      HistoricalTimeSeriesFieldAdjustmentMap fieldMap = _fieldMaps.get(dataSource);
      HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMap != null ? fieldMap.getFieldAdjustment(dataField) : null;
      return fieldAdjustment != null ? ImmutableMap.of(dataSource, fieldAdjustment) : ImmutableMap.<String, HistoricalTimeSeriesFieldAdjustment>of();
    }
    
    Map<String, HistoricalTimeSeriesFieldAdjustment> results = new HashMap<String, HistoricalTimeSeriesFieldAdjustment>();
    for (Map.Entry<String, HistoricalTimeSeriesFieldAdjustmentMap> fieldMapEntry : _fieldMaps.entrySet()) {
      HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMapEntry.getValue().getFieldAdjustment(dataField);
      if (fieldAdjustment != null) {
        results.put(fieldMapEntry.getKey(), fieldAdjustment);
      }
    }
    return results;
  }
  
}
