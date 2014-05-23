/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;

/**
 * RESTful resource for a version of a market data snapshot.
 */
@Path("/datasnapshots/{snapshotId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebMarketDataSnapshotVersionResource extends AbstractWebMarketDataSnapshotResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebMarketDataSnapshotVersionResource(final AbstractWebMarketDataSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "snapshotversion.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getVersioned().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createRootData();
    String json = getFreemarker().build(JSON_DIR + "snapshot.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    MarketDataSnapshotDocument latestDoc = data().getSnapshot();
    MarketDataSnapshotDocument versionedSnapshot = data().getVersioned();
    out.put("latestSnapshotDoc", latestDoc);
    out.put("latestSnapshot", latestDoc.getSnapshot());
    out.put("snapshotDoc", versionedSnapshot);
    out.put("snapshot", versionedSnapshot.getSnapshot());
    out.put("deleted", !latestDoc.isLatest());
    out.put("snapshotXml", StringEscapeUtils.escapeJavaScript(createBeanXML(versionedSnapshot.getSnapshot())));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebMarketDataSnapshotData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebMarketDataSnapshotData data, final UniqueId overrideVersionId) {
    String snapshotId = data.getBestSnapshotUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebMarketDataSnapshotVersionResource.class).build(snapshotId, versionId);
  }

}
