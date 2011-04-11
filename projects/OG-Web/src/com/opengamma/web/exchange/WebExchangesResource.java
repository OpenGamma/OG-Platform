/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

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
    FlexiBean out = createSearchResultData(page, pageSize, name, uriInfo);
    return getFreemarker().build("exchanges/exchanges.ftl", out);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @Context UriInfo uriInfo) {
    FlexiBean out = createSearchResultData(page, pageSize, name, uriInfo);
    return getFreemarker().build("exchanges/jsonexchanges.ftl", out);
  }

  private FlexiBean createSearchResultData(int page, int pageSize, String name, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      Identifier id = Identifier.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExchangeKey(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      ExchangeSearchResult searchResult = data().getExchangeMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
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
    URI uri = createExchange(name, idScheme, idValue, regionScheme, regionValue);
    return Response.seeOther(uri).build();
  }

  private URI createExchange(String name, String idScheme, String idValue, String regionScheme, String regionValue) {
    Identifier id = Identifier.of(idScheme, idValue);
    Identifier region = Identifier.of(regionScheme, regionValue);
    ManageableExchange exchange = new ManageableExchange(IdentifierBundle.of(id), name, IdentifierBundle.of(region), null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument added = data().getExchangeMaster().add(doc);
    URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return uri;
  }
  
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
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
      return Response.status(Status.BAD_REQUEST).build();
    }
    URI uri = createExchange(name, idScheme, idValue, regionScheme, regionValue);
    return Response.created(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{exchangeId}")
  public WebExchangeResource findExchange(@PathParam("exchangeId") String idStr) {
    data().setUriExchangeId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
    try {
      ExchangeDocument doc = data().getExchangeMaster().get(oid);
      data().setExchange(doc);
    } catch (DataNotFoundException ex) {
      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      ExchangeHistoryResult historyResult = data().getExchangeMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setExchange(historyResult.getFirstDocument());
    }
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
