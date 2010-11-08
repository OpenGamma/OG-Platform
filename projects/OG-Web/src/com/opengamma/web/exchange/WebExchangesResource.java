/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import java.net.URI;
import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.world.exchange.master.ExchangeDocument;
import com.opengamma.financial.world.exchange.master.ExchangeMaster;
import com.opengamma.financial.world.exchange.master.ExchangeSearchRequest;
import com.opengamma.financial.world.exchange.master.ExchangeSearchResult;
import com.opengamma.financial.world.exchange.master.ManageableExchange;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.rest.WebPaging;

/**
 * RESTful resource for all exchanges.
 * <p>
 * The exchanges resource represents the whole of a exchange master.
 */
@Path("/exchanges")
public class WebExchangesResource extends AbstractWebExchangeResource {

  /**
   * Creates the resource.
   * @param exchangeMaster  the exchange master, not null
   */
  public WebExchangesResource(final ExchangeMaster exchangeMaster) {
    super(exchangeMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      Identifier id = Identifier.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addIdentifierBundle(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      ExchangeSearchResult searchResult = data().getExchangeMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return getFreemarker().build("exchanges/exchanges.ftl", out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("name") String name,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("regionscheme") String regionScheme,
      @FormParam("regionvalue") String regionValue) {
    name = StringUtils.trimToNull(name);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    regionScheme = StringUtils.trimToNull(regionScheme);
    regionValue = StringUtils.trimToNull(regionValue);
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
      String html = getFreemarker().build("exchanges/exchanges-add.ftl", out);
      return Response.ok(html).build();
    }
    Identifier id = Identifier.of(idScheme, idValue);
    Identifier region = Identifier.of(regionScheme, regionValue);
    ManageableExchange exchange = new ManageableExchange(IdentifierBundle.of(id), name, IdentifierBundle.of(region), null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument added = data().getExchangeMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getExchangeId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{exchangeId}")
  public WebExchangeResource findExchange(@PathParam("exchangeId") String idStr) {
    data().setUriExchangeId(idStr);
    ExchangeDocument exchangeDoc = data().getExchangeMaster().get(UniqueIdentifier.parse(idStr));
    data().setExchange(exchangeDoc);
    return new WebExchangeResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for exchanges.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebExchangeData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for exchanges.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebExchangeData data, IdentifierBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebExchangesResource.class);
    if (identifiers != null) {
      Iterator<Identifier> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        Identifier id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
