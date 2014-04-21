/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

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
import com.opengamma.master.convention.ConventionDocument;

/**
 * RESTful resource for a version of a convention.
 */
@Path("/conventions/{conventionId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebConventionVersionResource extends AbstractWebConventionResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConventionVersionResource(final AbstractWebConventionResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "conventionversion.ftl", out);
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
    ConventionDocument doc = data().getVersioned();
    out.put("type", data().getTypeMap().inverse().get(doc.getConvention().getClass()));
    String json = getFreemarker().build(JSON_DIR + "convention.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConventionDocument latestDoc = data().getConvention();
    ConventionDocument versionedConvention = data().getVersioned();
    out.put("latestConventionDoc", latestDoc);
    out.put("latestConvention", latestDoc.getConvention());
    out.put("conventionDoc", versionedConvention);
    out.put("convention", versionedConvention.getConvention());
    out.put("conventionDescription", getConventionTypesProvider().getDescription(versionedConvention.getConvention().getClass()));
    out.put("conventionXml", StringEscapeUtils.escapeJavaScript(createXML(versionedConvention.getConvention())));
    out.put("deleted", !latestDoc.isLatest());
    return out;
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
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConventionData data, final UniqueId overrideVersionId) {
    String conventionId = data.getBestConventionUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebConventionVersionResource.class).build(conventionId, versionId);
  }

}
