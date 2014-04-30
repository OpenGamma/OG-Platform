/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.marketdatasnapshot;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;

/**
 * RESTful resource for a market data snapshot document.
 *
 */
@Path("/datasnapshots/{snapshotId}")
public class WebMarketDataSnapshotResource extends AbstractWebMarketDataSnapshotResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebMarketDataSnapshotResource(final AbstractWebMarketDataSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "snapshot.ftl", out);
  }
  
  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("snapshotxml") String xml) {
    if (data().getSnapshot().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      final FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      final String html = getFreemarker().build(HTML_DIR + "snapshot-update.ftl", out);
      return Response.ok(html).build();
    }

    final URI uri = updateSnapshot(name, parseXML(xml, ManageableMarketDataSnapshot.class));
    return Response.seeOther(uri).build();
  }
  
  private URI updateSnapshot(final String name, final ManageableMarketDataSnapshot snapshot) {
    final MarketDataSnapshotDocument oldDoc = data().getSnapshot();
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
    snapshot.setName(name);
    doc.setUniqueId(oldDoc.getUniqueId());
    doc = data().getMarketDataSnapshotMaster().update(doc);
    data().setSnapshot(doc);
    final URI uri = WebMarketDataSnapshotResource.uri(data());
    return uri;
  }
  
  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final MarketDataSnapshotDocument doc = data().getSnapshot();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getMarketDataSnapshotMaster().remove(doc.getUniqueId());
    final URI uri = WebMarketDataSnapshotsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final MarketDataSnapshotDocument doc = data().getSnapshot();
    out.put("snapshotDoc", doc);
    out.put("snapshot", doc.getSnapshot());
    out.put("deleted", !doc.isLatest());
    out.put("snapshotXml", StringEscapeUtils.escapeJavaScript(createBeanXML(doc.getSnapshot())));
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebMarketDataSnapshotVersionsResource findVersions() {
    return new WebMarketDataSnapshotVersionsResource(this);
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
   * @param overrideSnapshotId  the override snapshot id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebMarketDataSnapshotData data, final UniqueId overrideSnapshotId) {
    final String snapshotId = data.getBestSnapshotUriId(overrideSnapshotId);
    return data.getUriInfo().getBaseUriBuilder().path(WebMarketDataSnapshotResource.class).build(snapshotId);
  }

}
