/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

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

import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ManageableConvention;

/**
 * RESTful resource for a convention document.
 *
 */
@Path("/conventions/{conventionId}")
public class WebConventionResource extends AbstractWebConventionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConventionResource(final AbstractWebConventionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    final ConventionDocument doc = data().getConvention();
    out.put("conventionXml", StringEscapeUtils.escapeJava(createBeanXML(doc.getConvention())));
    return getFreemarker().build(HTML_DIR + "convention.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context final Request request) {
    final EntityTag etag = new EntityTag(data().getConvention().getUniqueId().toString());
    final ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    final FlexiBean out = createRootData();
    final ConventionDocument doc = data().getConvention();
    out.put("conventionXML", StringEscapeUtils.escapeJava(createBeanXML(doc.getConvention())));
    out.put("type", data().getTypeMap().inverse().get(doc.getConvention().getClass()));
    final String json = getFreemarker().build(JSON_DIR + "convention.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("conventionxml") String xml) {
    if (data().getConvention().isLatest() == false) {
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
      final String html = getFreemarker().build(HTML_DIR + "convention-update.ftl", out);
      return Response.ok(html).build();
    }

    try {
      ManageableConvention convention = parseXML(xml, data().getConvention().getConvention().getClass());
      final URI uri = updateConvention(name, convention);
      return Response.seeOther(uri).build();
    } catch (Exception ex) {
      final FlexiBean out = createRootData();
      out.put("conventionXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
      out.put("err_conventionXmlMsg", StringUtils.defaultString(ex.getMessage()));
      final String html = getFreemarker().build(HTML_DIR + "convention-update.ftl", out);
      return Response.ok(html).build();
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("conventionJSON") String json,
      @FormParam("conventionXML") String xml) {
    if (data().getConvention().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }

    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    // JSON allows a null convention to just change the name
    if (name == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    ManageableConvention conventionValue = null;
    if (json != null) {
      conventionValue = (ManageableConvention) parseJSON(json);
    } else if (xml != null) {
      conventionValue = parseXML(xml, ManageableConvention.class);
    }
    updateConvention(name, conventionValue);
    return Response.ok().build();
  }

  private URI updateConvention(final String name, final ManageableConvention snapshot) {
    final ConventionDocument oldDoc = data().getConvention();
    ConventionDocument doc = new ConventionDocument(snapshot);
    snapshot.setName(name);
    doc.setUniqueId(oldDoc.getUniqueId());
    doc = data().getConventionMaster().update(doc);
    data().setConvention(doc);
    return WebConventionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    final ConventionDocument doc = data().getConvention();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getConventionMaster().remove(doc.getUniqueId());
    final URI uri = WebConventionsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    final ConventionDocument doc = data().getConvention();
    if (doc.isLatest()) {
      data().getConventionMaster().remove(doc.getUniqueId());
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
    final ConventionDocument doc = data().getConvention();
    out.put("conventionDoc", doc);
    out.put("convention", doc.getConvention());
    out.put("conventionDescription", getConventionTypesProvider().getDescription(doc.getConvention().getClass()));
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebConventionVersionsResource findVersions() {
    return new WebConventionVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConventionData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideConventionId  the override convention id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConventionData data, final UniqueId overrideConventionId) {
    final String conventionId = data.getBestConventionUriId(overrideConventionId);
    return data.getUriInfo().getBaseUriBuilder().path(WebConventionResource.class).build(conventionId);
  }

}
