/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for market data snapshots.
 */
@Path("marketDataSnapshotSource")
public class MarketDataSnapshotSourceService extends AbstractResourceService<MarketDataSnapshotSource, MarketDataSnapshotSourceResource> {

  public MarketDataSnapshotSourceService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected MarketDataSnapshotSourceResource createResource(MarketDataSnapshotSource underlying) {
    return new MarketDataSnapshotSourceResource(underlying, getFudgeContext());
  }

}
