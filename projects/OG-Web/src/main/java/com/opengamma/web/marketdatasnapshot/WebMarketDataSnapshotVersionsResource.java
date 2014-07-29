/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;

/**
 * RESTful resource for all versions of a market data snapshot.
 */
@Path("/datasnapshots/{snapshotId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebMarketDataSnapshotVersionsResource extends AbstractWebMarketDataSnapshotResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebMarketDataSnapshotVersionsResource(final AbstractWebMarketDataSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest(data().getSnapshot().getUniqueId());
    MarketDataSnapshotHistoryResult result = data().getMarketDataSnapshotMaster().history(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getSnapshots());
    return getFreemarker().build(HTML_DIR + "snapshotversions.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    MarketDataSnapshotDocument doc = data().getSnapshot();
    out.put("snapshotDoc", doc);
    out.put("snapshot", doc.getSnapshot());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebMarketDataSnapshotVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    MarketDataSnapshotDocument doc = data().getSnapshot();
    UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      MarketDataSnapshotDocument versioned = data().getMarketDataSnapshotMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebMarketDataSnapshotVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebMarketDataSnapshotData data) {
    String snapshotId = data.getBestSnapshotUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebMarketDataSnapshotVersionsResource.class).build(snapshotId);
  }

}
