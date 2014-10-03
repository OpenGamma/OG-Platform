/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

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

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;

/**
 * RESTful resource for a exchange.
 */
@Path("/exchanges/{exchangeId}")
public class WebExchangeResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebExchangeResource(final AbstractWebExchangeResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "exchange.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJSON(@Context Request request) {
    EntityTag etag = new EntityTag(data().getExchange().getUniqueId().toString());
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder != null) {
      return builder.build();
    }
    FlexiBean out = createRootData();
    String json = getFreemarker().build(JSON_DIR + "exchange.ftl", out);
    return Response.ok(json).tag(etag).build();
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("regionscheme") String regionScheme,
      @FormParam("regionvalue") String regionValue) {
    if (data().getExchange().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    if (name == null || idScheme == null || idValue == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (idScheme == null) {
        out.put("err_idschemeMissing", true);
      }
      if (idValue == null) {
        out.put("err_idvalueMissing", true);
      }
      if (regionScheme == null) {
        out.put("err_regionschemeMissing", true);
      }
      if (regionValue == null) {
        out.put("err_regionvalueMissing", true);
      }
      String html = getFreemarker().build(HTML_DIR + "exchange-update.ftl", out);
      return Response.ok(html).build();
    }
    URI uri = updateExchange(name, idScheme, idValue, regionScheme, regionValue);
    return Response.seeOther(uri).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("regionscheme") String regionScheme,
      @FormParam("regionvalue") String regionValue) {
    if (data().getExchange().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    
    name = StringUtils.trimToNull(name);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    if (name == null || idScheme == null || idValue == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    updateExchange(name, idScheme, idValue, regionScheme, regionValue);
    return Response.ok().build();
  }

  private URI updateExchange(String name, String idScheme, String idValue, String regionScheme, String regionValue) {
    ManageableExchange exchange = data().getExchange().getExchange().clone();
    exchange.setName(name);
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of(idScheme, idValue)));
    exchange.setRegionIdBundle(ExternalIdBundle.of(ExternalId.of(regionScheme, regionValue)));
    ExchangeDocument doc = new ExchangeDocument(exchange);
    doc = data().getExchangeMaster().update(doc);
    data().setExchange(doc);
    URI uri = WebExchangeResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    ExchangeDocument doc = data().getExchange();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(getHTML()).build();
    }
    data().getExchangeMaster().remove(doc.getUniqueId());
    URI uri = WebExchangeResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    ExchangeDocument doc = data().getExchange();
    if (doc.isLatest()) {
      data().getExchangeMaster().remove(doc.getUniqueId());
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
    FlexiBean out = super.createRootData();
    ExchangeDocument doc = data().getExchange();
    out.put("exchangeDoc", doc);
    out.put("exchange", doc.getExchange());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebExchangeVersionsResource findVersions() {
    return new WebExchangeVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideExchangeId  the override exchange id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebExchangeData data, final UniqueId overrideExchangeId) {
    String exchangeId = data.getBestExchangeUriId(overrideExchangeId);
    return data.getUriInfo().getBaseUriBuilder().path(WebExchangeResource.class).build(exchangeId);
  }

}
