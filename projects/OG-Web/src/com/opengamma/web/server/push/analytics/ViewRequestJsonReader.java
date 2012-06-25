/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import javax.time.Instant;

import org.json.JSONException;
import org.json.JSONObject;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public class ViewRequestJsonReader {

  public static ViewRequest createViewRequest(String json) {
    JSONObject jsonObject;
    String viewDefinitionId;
    String aggregator;
    ViewRequest.MarketData marketData;
    try {
      jsonObject = new JSONObject(json);
      viewDefinitionId = jsonObject.getString("viewDefinitionId");
      aggregator = jsonObject.optString("aggregator", null);
      String marketDataType = jsonObject.getString("marketDataType");
      if (marketDataType.equals("live")) {
        // TODO should this be optional for auto / default behaviour?
        String provider = jsonObject.getString("provider");
        marketData = new ViewRequest.Live(provider);
      } else {
        UniqueId snapshotId = UniqueId.parse(jsonObject.getString("snapshotId"));
        String versionDateTime = jsonObject.optString("versionDateTime", null);
        VersionCorrection versionCorrection;
        if (versionDateTime != null) {
          versionCorrection = VersionCorrection.ofVersionAsOf(Instant.parse(versionDateTime));
        } else {
          versionCorrection = VersionCorrection.LATEST;
        }
        marketData = new ViewRequest.Snapshot(snapshotId, versionCorrection);
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to decode ViewRequest", e);
    }
    return new ViewRequest(UniqueId.parse(viewDefinitionId), aggregator, marketData);
  }
}
