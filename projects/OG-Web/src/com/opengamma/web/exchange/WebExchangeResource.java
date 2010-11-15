/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("exchanges/exchange.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("regionscheme") String regionScheme,
      @FormParam("regionvalue") String regionValue) {
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
      String html = getFreemarker().build("exchanges/exchange-update.ftl", out);
      return Response.ok(html).build();
    }
    ManageableExchange exchange = data().getExchange().getExchange().clone();
    exchange.setName(name);
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of(idScheme, idValue)));
    exchange.setRegionId(IdentifierBundle.of(Identifier.of(regionScheme, regionValue)));
    ExchangeDocument doc = new ExchangeDocument(exchange);
    doc = data().getExchangeMaster().update(doc);
    data().setExchange(doc);
    URI uri = WebExchangeResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    ExchangeDocument doc = data().getExchange();
    data().getExchangeMaster().remove(doc.getUniqueId());
    URI uri = WebExchangesResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ExchangeDocument doc = data().getExchange();
    out.put("exchangeDoc", doc);
    out.put("exchange", doc.getExchange());
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
  public static URI uri(final WebExchangeData data, final UniqueIdentifier overrideExchangeId) {
    String exchangeId = data.getBestExchangeUriId(overrideExchangeId);
    return data.getUriInfo().getBaseUriBuilder().path(WebExchangeResource.class).build(exchangeId);
  }

}
