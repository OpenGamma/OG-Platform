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
import com.google.common.collect.Iterables;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates an historical time-series master with missing time-series for each instrument referenced by the 
 * {@link InMemoryConventionBundleMaster}.
 */
public class ConventionInstrumentTimeSeriesLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(ConventionInstrumentTimeSeriesLoader.class);

  private final InMemoryConventionBundleMaster _conventionMaster;
  
  private final HistoricalTimeSeriesSource _htsSource;
  private final HistoricalTimeSeriesLoader _htsLoader;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _dataField;
  private final ExternalScheme _identifierScheme;
  private final boolean _updateExisting;
  
  public ConventionInstrumentTimeSeriesLoader(HistoricalTimeSeriesSource htsSource,
      HistoricalTimeSeriesLoader htsLoader, String dataSource, String dataProvider, String dataField,
      ExternalScheme identifierScheme, boolean updateExisting) {
    ArgumentChecker.notNull(htsSource, "htsSource");
    ArgumentChecker.notNull(htsLoader, "htsLoader");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(identifierScheme, "identifierScheme");
    _conventionMaster = new InMemoryConventionBundleMaster();
    _htsSource = htsSource;
    _htsLoader = htsLoader;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = dataField;
    _identifierScheme = identifierScheme;
    _updateExisting = updateExisting;
  }
  
  private InMemoryConventionBundleMaster getConventionMaster() {
    return _conventionMaster;
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
  
  private ExternalScheme getIdentifierScheme() {
    return _identifierScheme;
  }
  
  private boolean isUpdateExisting() {
    return _updateExisting;
  }
  
  //-------------------------------------------------------------------------
  public void run() {
    Collection<ConventionBundle> conventions = getConventionMaster().getAll();
    Set<ExternalId> externalIds = new HashSet<ExternalId>();
    for (ConventionBundle convention : conventions) {
      addExternalId(convention.getSwapFloatingLegInitialRate(), externalIds);
    }
    s_logger.info("Checking {} time-series: {}", externalIds.size(), externalIds);
    for (ExternalId externalId : externalIds) {
      ensureTimeseries(externalId);
    }
  }
  
  private void addExternalId(ExternalId externalId, Set<ExternalId> externalIds) {
    if (externalId == null) {
      return;
    }
    if (externalId.isNotScheme(getIdentifierScheme())) {
      ConventionBundleSearchResult result = getConventionMaster().searchConventionBundle(new ConventionBundleSearchRequest(externalId));
      if (result.getResults().size() == 0) {
        s_logger.warn("Unable to find mapping from {} to identifier with scheme {}", externalId, getIdentifierScheme());
        return;
      }
      if (result.getResults().size() > 1) {
        s_logger.warn("Found multiple conventions for {}, with potentially ambiguous mappings to scheme {}", externalId, getIdentifierScheme());
        return;
      }
      ConventionBundleDocument searchResult = Iterables.getOnlyElement(result.getResults());
      externalId = searchResult.getConventionSet().getIdentifiers().getExternalId(getIdentifierScheme());
      if (externalId == null) {
        s_logger.warn("Convention for {} does not include a mapping to an identifier with scheme {}", externalId, getIdentifierScheme());
        return;
      }
    }
    externalIds.add(externalId);
  }
  
  private void ensureTimeseries(ExternalId externalId) {
    s_logger.info("Checking time-series for {}", externalId);
    try {
      HistoricalTimeSeries hts = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(ExternalIdBundle.of(externalId), getDataSource(), getDataProvider(), getDataField());
      if (hts == null) {
        s_logger.info("Adding time-series for {}", externalId);
        getHistoricalTimeSeriesLoader().loadTimeSeries(ImmutableSet.of(externalId), getDataProvider(), getDataField(), null, null);
      } else if (isUpdateExisting()) {
        s_logger.info("Updating time-series for {} with identifier {}", externalId, hts.getUniqueId());
        getHistoricalTimeSeriesLoader().updateTimeSeries(hts.getUniqueId());
      }
    } catch (Exception e) {
      s_logger.error("Error with time-series for " + externalId, e);
    }
  }
  
}
