/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Provides access to a remote {@link com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter}.
 */
public class RemoteMarketDataSnapshotter extends AbstractRemoteClient implements MarketDataSnapshotter {

  public RemoteMarketDataSnapshotter(URI baseUri) {
    super(baseUri);
  }

  public RemoteMarketDataSnapshotter(URI baseUri, FudgeRestClient client) {
    super(baseUri, client);
  }

  @Override
  public StructuredMarketDataSnapshot createSnapshot(ViewClient client, ViewCycle cycle) {
    ArgumentChecker.notNull(client, "client");
    ArgumentChecker.notNull(cycle, "cycle");

    URI uri = DataMarketDataSnapshotterResource.uriCreateSnapshot(getBaseUri(), client.getUniqueId(), cycle.getUniqueId());
    return accessRemote(uri).get(StructuredMarketDataSnapshot.class);
  }

  @Override
  public Map<YieldCurveKey, Map<String, ValueRequirement>> getYieldCurveSpecifications(ViewClient client, ViewCycle cycle) {
    ArgumentChecker.notNull(client, "client");
    ArgumentChecker.notNull(cycle, "cycle");

    URI uri = DataMarketDataSnapshotterResource.uriGetYieldCurveSpecs(getBaseUri(), client.getUniqueId(), cycle.getUniqueId());
    return accessRemote(uri).get(Map.class);
  }
}
