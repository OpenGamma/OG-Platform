/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.List;

import org.threeten.bp.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;

/**
 * 
 */
public class GoldenCopyCreator {

  private final ToolContext _toolContext;
  
  public GoldenCopyCreator(ToolContext toolContext) {
    _toolContext = toolContext;
  }


  public ToolContext getToolContext() {
    return _toolContext;
  }


  public GoldenCopy run(String viewName, String snapshotName, String version) {
    
    Instant valuationTime = getValuationTime(snapshotName);
    
    ViewRunner viewRunner = new ViewRunner(getToolContext().getConfigMaster(),
        getToolContext().getViewProcessor(),
        getToolContext().getPositionSource(),
        getToolContext().getSecuritySource(),
        getToolContext().getMarketDataSnapshotMaster());
    CalculationResults results = viewRunner.run(version, viewName, snapshotName, valuationTime);
    
    return GoldenCopy.create(snapshotName, viewName, valuationTime, results);
    
  }


  private Instant getValuationTime(String snapshotName) {
    MarketDataSnapshotMaster snapshotMaster = getToolContext().getMarketDataSnapshotMaster();
    MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
    searchRequest.setName(snapshotName);
    List<MarketDataSnapshotDocument> documents = snapshotMaster.search(searchRequest).getDocuments();
    Preconditions.checkArgument(documents.size() == 1, "Only one snapshot should exist for given name '%s'. Found %s records: %s", snapshotName, documents.size(), documents);
    return Iterables.getOnlyElement(documents).getVersionFromInstant(); //TODO - should be stored on the snapshot
  }
  
  
}
