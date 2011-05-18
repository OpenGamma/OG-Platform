/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for market data snapshots.
 */
@Path("marketDataSnapshotMaster")
public class MarketDataSnapshotMasterService extends AbstractResourceService<MarketDataSnapshotMaster, MarketDataSnapshotMasterResource> {

  public MarketDataSnapshotMasterService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected MarketDataSnapshotMasterResource createResource(MarketDataSnapshotMaster underlying) {
    return new MarketDataSnapshotMasterResource(underlying, getFudgeContext());
  }

}
