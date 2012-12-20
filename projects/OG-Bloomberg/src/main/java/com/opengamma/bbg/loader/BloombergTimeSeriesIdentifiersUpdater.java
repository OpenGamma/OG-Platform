/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Updates the timeseries identifiers with loaded identifiers from Bloomberg
 */
public class BloombergTimeSeriesIdentifiersUpdater {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergTimeSeriesIdentifiersUpdater.class);

  /**
   * The Spring config file.
   */
  static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-timeseries-identifier-context.xml";

  /**
   * The master.
   */
  private final HistoricalTimeSeriesMaster _htsMaster;
  /**
   * The provider of identifiers.
   */
  private final ExternalIdResolver _bbgIdentifierProvider;

  /**
   * Creates a new instance of the updater.
   * 
   * @param htsMaster  the historical time-series master, not null
   * @param bbgIdentifierProvider  the identifier provider, not null
   */
  public BloombergTimeSeriesIdentifiersUpdater(final HistoricalTimeSeriesMaster htsMaster, final ExternalIdResolver bbgIdentifierProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(bbgIdentifierProvider, "identifierProvider");
    _htsMaster = htsMaster;
    _bbgIdentifierProvider = bbgIdentifierProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Main processing.
   */
  public void run() {
    // fetch the documents to update
    Iterable<HistoricalTimeSeriesInfoDocument> documents = getCurrentTimeSeriesDocuments();
    
    // find the BUIDs
    Map<ExternalId, HistoricalTimeSeriesInfoDocument> buidDocMap = extractBuids(documents);
    Set<ExternalId> buids = new HashSet<ExternalId>(buidDocMap.keySet());
    
    // query Bloomberg
    Map<ExternalId, ExternalIdBundleWithDates> buidToUpdated = _bbgIdentifierProvider.getExternalIds(buids);
    for (Entry<ExternalId, ExternalIdBundleWithDates> entry : buidToUpdated.entrySet()) {
      entry.setValue(BloombergDataUtils.addTwoDigitYearCode(entry.getValue()));
    }
    
    // update the database
    updateIdentifiers(buidDocMap, buidToUpdated);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets all the current Bloomberg-based time-series.
   * 
   * @return the current documents, not null
   */
  private Iterable<HistoricalTimeSeriesInfoDocument> getCurrentTimeSeriesDocuments() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
    return HistoricalTimeSeriesInfoSearchIterator.iterable(_htsMaster, request);
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the BUID from each document.
   * 
   * @param documents  the documents, not null
   * @return the map of BIUD to unique identifier, not null
   */
  private Map<ExternalId, HistoricalTimeSeriesInfoDocument> extractBuids(Iterable<HistoricalTimeSeriesInfoDocument> documents) {
    Map<ExternalId, HistoricalTimeSeriesInfoDocument> buids = Maps.newHashMap();
    for (HistoricalTimeSeriesInfoDocument doc : documents) {
      ExternalIdBundleWithDates identifierBundleWithDates = doc.getInfo().getExternalIdBundle();
      ExternalIdBundle bundle = identifierBundleWithDates.toBundle();
      ExternalId buid = bundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID);
      if (buid == null) {
        throw new OpenGammaRuntimeException("no buid for " + bundle);
      }
      buids.put(buid, doc);
    }
    return buids;
  }

  //-------------------------------------------------------------------------
  /**
   * Updates the identifiers.
   * 
   * @param buidDocMap  the map from BUID to document, not null
   * @param buidToUpdated  the map from BUID to updated identifier, not null
   */
  private void updateIdentifiers(
      Map<ExternalId, HistoricalTimeSeriesInfoDocument> buidDocMap,
      Map<ExternalId, ExternalIdBundleWithDates> buidToUpdated) {
    for (Entry<ExternalId, ExternalIdBundleWithDates> entry : buidToUpdated.entrySet()) {
      HistoricalTimeSeriesInfoDocument doc = buidDocMap.get(entry.getKey());
      ExternalIdBundleWithDates updatedId = entry.getValue();
      if (doc != null && doc.getInfo().getExternalIdBundle().equals(updatedId) == false) {
        doc.getInfo().setExternalIdBundle(updatedId);
        s_logger.debug("Updated {} with {}", doc.getUniqueId(), updatedId);
        _htsMaster.update(doc);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the updater.
   * This uses the updater configured by Spring.
   * 
   * @param args  not used
   */
  public static void main(String[] args) { //CSIGNORE
    PlatformConfigUtils.configureSystemProperties();
    BloombergTimeSeriesIdentifiersUpdater updater = loadUpdater();
    updater.run();
  }

  /**
   * Gets the loader from Spring config.
   * 
   * @return the identifier loader, not null
   */
  private static BloombergTimeSeriesIdentifiersUpdater loadUpdater() {    
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    BloombergTimeSeriesIdentifiersUpdater loader = (BloombergTimeSeriesIdentifiersUpdater) context.getBean("identifiersLoader");
    return loader;
  }

}
