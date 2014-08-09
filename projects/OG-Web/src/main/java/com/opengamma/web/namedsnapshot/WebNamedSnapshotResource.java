/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;

/**
 * RESTful resource for a named snapshot document.
 *
 */
@Path("/snapshots/{snapshotId}")
public class WebNamedSnapshotResource extends AbstractWebNamedSnapshotResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebNamedSnapshotResource(final AbstractWebNamedSnapshotResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    final MarketDataSnapshotDocument doc = data().getSnapshot();
    out.put("snapshotXml", StringEscapeUtils.escapeJava(createBeanXML(doc.getNamedSnapshot())));
    return getFreemarker().build(HTML_DIR + "namedsnapshot.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    final EntityTag etag = new EntityTag(data().getSnapshot().getUniqueId().toString());
    final ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    final FlexiBean out = createRootData();
    final MarketDataSnapshotDocument doc = data().getSnapshot();
    out.put("snapshotXML", StringEscapeUtils.escapeJava(createBeanXML(doc.getNamedSnapshot())));
    out.put("type", data().getTypeMap().inverse().get(doc.getNamedSnapshot().getClass()));
    final String json = getFreemarker().build(JSON_DIR + "namedsnapshot.ftl", out);
    return Response.ok(json).tag(etag).build();
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
      final String html = getFreemarker().build(HTML_DIR + "namedsnapshot-update.ftl", out);
      return Response.ok(html).build();
    }

    try {
      NamedSnapshot snapshot = parseXML(xml, data().getSnapshot().getNamedSnapshot().getClass());
      final URI uri = updateSnapshot(snapshot);
      return Response.seeOther(uri).build();
    } catch (Exception ex) {
      final FlexiBean out = createRootData();
      out.put("snapshotXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
      out.put("err_snapshotXmlMsg", StringUtils.defaultString(ex.getMessage()));
      final String html = getFreemarker().build(HTML_DIR + "namedsnapshot-update.ftl", out);
      return Response.ok(html).build();
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("snapshotJSON") String json,
      @FormParam("snapshotXML") String xml) {
    if (data().getSnapshot().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    NamedSnapshot snapshot = parseXML(xml, NamedSnapshot.class);
    updateSnapshot(snapshot);
    return Response.ok().build();
  }

  private URI updateSnapshot(final NamedSnapshot snapshot) {
    final MarketDataSnapshotDocument oldDoc = data().getSnapshot();
    MarketDataSnapshotDocument doc = new MarketDataSnapshotDocument(snapshot);
    doc.setUniqueId(oldDoc.getUniqueId());
    doc = data().getSnapshotMaster().update(doc);
    data().setSnapshot(doc);
    return WebNamedSnapshotResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final MarketDataSnapshotDocument doc = data().getSnapshot();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getSnapshotMaster().remove(doc.getUniqueId());
    final URI uri = WebNamedSnapshotsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final MarketDataSnapshotDocument doc = data().getSnapshot();
    if (doc.isLatest()) {
      data().getSnapshotMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
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
    out.put("snapshot", doc.getNamedSnapshot());
    out.put("snapshotDescription", getSnapshotTypesProvider().getDescription(doc.getNamedSnapshot().getClass()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebNamedSnapshotVersionsResource findVersions() {
    return new WebNamedSnapshotVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebNamedSnapshotData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideSnapshotId  the override snapshot id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebNamedSnapshotData data, final UniqueId overrideSnapshotId) {
    final String snapshotId = data.getBestSnapshotUriId(overrideSnapshotId);
    return data.getUriInfo().getBaseUriBuilder().path(WebNamedSnapshotResource.class).build(snapshotId);
  }

}
