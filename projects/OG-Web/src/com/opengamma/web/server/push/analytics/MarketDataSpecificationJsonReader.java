/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * <p>Creates instances of {@link MarketDataSpecification} subclasses from JSON. The JSON format is:</p>
 * <h4>Live Data</h4>
 * <code>{"marketDataType": "live", "source": "Bloomberg"}</code>
 * <h4>Fixed Historical Data</h4>
 * <code>{"marketDataType": "fixedHistorical", "resolverKey": "", "fieldResolverKey": "", "date": "2012-08-30"}</code>
 * <h4>Latest Historical Data</h4>
 * <code>{"marketDataType": "latestHistorical", "resolverKey": "", "fieldResolverKey": ""}</code>
 * <h4>Snapshot Data</h4>
 * <code>{"marketDataType": "snapshot", "snapshotId": ""}</code>
 */
/* package */ class MarketDataSpecificationJsonReader {

  /* package */ static MarketDataSpecification buildSpecification(String json) {
    throw new UnsupportedOperationException("not implemented");
  }

  /* package */ static List<MarketDataSpecification> buildSpecifications(String json) {
    throw new UnsupportedOperationException("not implemented");
  }
}
