/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.web.analytics.rest.MarketDataSnapshotListResource;

/**
 * REST resource that provides a list of available randomized market data snapshots. Returns the same list as
 * {@link MarketDataSnapshotListResource} but uses a different endpoint
 */
@Path("randomizedmarketdatasnapshots")
public class RandomizedMarketDataSnapshotListResource extends MarketDataSnapshotListResource {

  /**
   *
   * @param snapshotMaster For querying snapshots
   */
  public RandomizedMarketDataSnapshotListResource(MarketDataSnapshotMaster snapshotMaster) {
    super(snapshotMaster);
  }

  /**
   * Forwards to {@link MarketDataSnapshotListResource#getMarketDataSnapshotList()}.
   * @return JSON {@code [{basisViewName: basisViewName1, snapshots: [{id: snapshot1Id, name: snapshot1Name}, ...]}, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getMarketDataSnapshotList() {
    return super.getMarketDataSnapshotList();
  }
}
