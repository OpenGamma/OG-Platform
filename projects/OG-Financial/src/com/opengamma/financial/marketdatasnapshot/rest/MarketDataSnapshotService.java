package com.opengamma.financial.marketdatasnapshot.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteMarketDataSnapshot}.
 */
@Path("marketDataSnapshotMaster")
public class MarketDataSnapshotService extends AbstractResourceService<MarketDataSnapshotMaster, MarketDataSnapshotMasterResource>  {

  public MarketDataSnapshotService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected MarketDataSnapshotMasterResource createResource(MarketDataSnapshotMaster underlying) {
    return new MarketDataSnapshotMasterResource(underlying, getFudgeContext());
  }
}
