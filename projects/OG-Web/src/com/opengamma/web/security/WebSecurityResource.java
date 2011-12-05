/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.Collections;

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

import org.eclipse.jetty.webapp.WebAppContext;
import org.joda.beans.impl.flexi.FlexiBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import sun.awt.AppContext;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.DefaultHistoricalMarketDataFieldResolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.web.FreemarkerCustomRenderer;

/**
 * RESTful resource for a security.
 */
@Path("/securities/{securityId}")
public class WebSecurityResource extends AbstractWebSecurityResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebSecurityResource(final AbstractWebSecurityResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/security.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build("securities/jsonsecurity.ftl", out);
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    URI uri = updateSecurity(doc);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {  // TODO: idempotent
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    updateSecurity(doc);
    return Response.ok().build();
  }

  private URI updateSecurity(SecurityDocument doc) {
    ExternalIdBundle identifierBundle = doc.getSecurity().getExternalIdBundle();
    data().getSecurityLoader().loadSecurity(Collections.singleton(identifierBundle));
    return WebSecurityResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getSecurityMaster().remove(doc.getUniqueId());
    URI uri = WebSecurityResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    SecurityDocument doc = data().getSecurity();
    if (doc.isLatest()) {  // idempotent DELETE
      data().getSecurityMaster().remove(doc.getUniqueId());
    }
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    SecurityDocument doc = data().getSecurity();
    
    UniqueId htsId = htsResolver().resolve("PX_LAST", doc.getSecurity().getExternalIdBundle(), null, null);
    
    out.put("securityDoc", doc); 
    out.put("security", doc.getSecurity());
    out.put("timeSeriesId", htsId == null ? null : htsId.getObjectId());
    out.put("deleted", !doc.isLatest());
    addSecuritySpecificMetaData(doc.getSecurity(), out);
    out.put("customRenderer", FreemarkerCustomRenderer.INSTANCE);
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebSecurityVersionsResource findVersions() {
    return new WebSecurityVersionsResource(this);
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
   * @param overrideSecurityId  the override security id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data, final UniqueId overrideSecurityId) {
    String securityId = data.getBestSecurityUriId(overrideSecurityId);
    return data.getUriInfo().getBaseUriBuilder().path(WebSecurityResource.class).build(securityId);
  }

}
