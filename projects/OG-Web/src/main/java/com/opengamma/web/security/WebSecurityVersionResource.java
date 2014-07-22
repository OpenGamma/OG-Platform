/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

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
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.web.FreemarkerCustomRenderer;

/**
 * RESTful resource for a version of a security.
 */
@Path("/securities/{securityId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebSecurityVersionResource extends AbstractWebSecurityResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebSecurityVersionResource(final AbstractWebSecurityResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "securityversion.ftl", out);
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
    String json = getFreemarker().build(JSON_DIR + "security.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    SecurityDocument latestSecDoc = data().getSecurity();
    SecurityDocument versionedSecurity = data().getVersioned();
    out.put("latestSecurityDoc", latestSecDoc);
    out.put("latestSecurity", latestSecDoc.getSecurity());
    out.put("securityDoc", versionedSecurity);
    out.put("security", versionedSecurity.getSecurity());
    out.put("deleted", !latestSecDoc.isLatest());
    addSecuritySpecificMetaData(versionedSecurity.getSecurity(), out);
    out.put("customRenderer", FreemarkerCustomRenderer.INSTANCE);
    out.put(SECURITY_XML, StringEscapeUtils.escapeJavaScript(createBeanXML(versionedSecurity.getSecurity())));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data, final UniqueId overrideVersionId) {
    String securityId = data.getBestSecurityUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityVersionResource.class).build(securityId, versionId);
  }

}
