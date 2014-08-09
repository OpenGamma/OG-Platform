/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.Lists;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all versions of an snapshot.
 */
@Path("/snapshots/{snapshotId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebNamedSnapshotVersionsResource extends AbstractWebNamedSnapshotResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebNamedSnapshotVersionsResource(final AbstractWebNamedSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest(data().getSnapshot().getUniqueId());
    MarketDataSnapshotHistoryResult result = data().getSnapshotMaster().history(request);

    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", Lists.newArrayList("not implemented"));
    return getFreemarker().build(HTML_DIR + "namedsnapshotversions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    MarketDataSnapshotHistoryRequest request = new MarketDataSnapshotHistoryRequest(data().getSnapshot().getUniqueId());
    request.setPagingRequest(pr);
    MarketDataSnapshotHistoryResult result = data().getSnapshotMaster().history(request);

    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getSnapshots());
    out.put("paging", new WebPaging(result.getPaging(), data().getUriInfo()));
    String json = getFreemarker().build(JSON_DIR + "namedsnapshotversions.ftl", out);
    return Response.ok(json).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    MarketDataSnapshotDocument doc = data().getSnapshot();
    out.put("snapshotDoc", doc);
    out.put("snapshot", doc.getNamedSnapshot());
    out.put("snapshotDescription", getSnapshotTypesProvider().getDescription(doc.getNamedSnapshot().getClass()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebNamedSnapshotVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    MarketDataSnapshotDocument doc = data().getSnapshot();
    UniqueId combined = doc.getUniqueId().withVersion(idStr);
    if (doc.getUniqueId().equals(combined) == false) {
      MarketDataSnapshotDocument versioned = data().getSnapshotMaster().get(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebNamedSnapshotVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebNamedSnapshotData data) {
    String snapshotId = data.getBestSnapshotUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebNamedSnapshotVersionsResource.class).build(snapshotId);
  }

}
