/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates an historical time-series master with missing time-series for each instrument referenced by the 
 * {@link InMemoryConventionBundleMaster}.
 */
public class ConventionInstrumentTimeSeriesPopulator {

  private static final Logger s_logger = LoggerFactory.getLogger(ConventionInstrumentTimeSeriesPopulator.class);
  
  private final HistoricalTimeSeriesSource _htsSource;
  private final HistoricalTimeSeriesLoader _htsLoader;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _dataField;
  private final boolean _updateExisting;
  
  public ConventionInstrumentTimeSeriesPopulator(HistoricalTimeSeriesSource htsSource,
      HistoricalTimeSeriesLoader htsLoader, String dataSource, String dataProvider, String dataField, boolean updateExisting) {
    ArgumentChecker.notNull(htsSource, "htsSource");
    ArgumentChecker.notNull(htsLoader, "htsLoader");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    _htsSource = htsSource;
    _htsLoader = htsLoader;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = dataField;
    _updateExisting = updateExisting;
  }
  
  private HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _htsSource;
  }
  
  private HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader() {
    return _htsLoader;
  }
  
  private String getDataSource() {
    return _dataSource;
  }
  
  private String getDataProvider() {
    return _dataProvider;
  }
  
  private String getDataField() {
    return _dataField;
  }
  
  private boolean isUpdateExisting() {
    return _updateExisting;
  }
  
  //-------------------------------------------------------------------------
  public void populate() {
    InMemoryConventionBundleMaster conventionMaster = new InMemoryConventionBundleMaster();
    Collection<ConventionBundle> conventions = conventionMaster.getAll();
    Set<ExternalId> externalIds = new HashSet<ExternalId>();
    for (ConventionBundle convention : conventions) {
      externalIds.add(convention.getBasisSwapPayFloatingLegInitialRate());
      externalIds.add(convention.getBasisSwapReceiveFloatingLegInitialRate());
      externalIds.add(convention.getSwapFloatingLegInitialRate());
    }
    s_logger.info("Checking {} time-series", externalIds.size());
    for (ExternalId externalId : externalIds) {
      ensureTimeseries(externalId);
    }
  }
  
  private void ensureTimeseries(ExternalId externalId) {
    s_logger.info("Checking time-series for {}", externalId);
    try {
      HistoricalTimeSeries hts = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(ExternalIdBundle.of(externalId), getDataSource(), getDataProvider(), getDataField());
      if (hts == null) {
        s_logger.info("Adding time-series for {}", externalId);
        getHistoricalTimeSeriesLoader().addTimeSeries(ImmutableSet.of(externalId), getDataProvider(), getDataField(), null, null);
      } else if (isUpdateExisting()) {
        s_logger.info("Updating time-series for {} with identifier {}", externalId, hts.getUniqueId());
        getHistoricalTimeSeriesLoader().updateTimeSeries(hts.getUniqueId());
      }
    } catch (Exception e) {
      s_logger.error("Error with time-series for " + externalId, e);
    }
  }
  
}
