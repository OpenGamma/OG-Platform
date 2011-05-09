/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import java.net.URI;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all regions.
 * <p>
 * The regions resource represents the whole of a region master.
 */
@Path("/regions")
public class WebRegionsResource extends AbstractWebRegionResource {

  /**
   * Creates the resource.
   * @param regionMaster  the region master, not null
   */
  public WebRegionsResource(final RegionMaster regionMaster) {
    super(regionMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("classification") RegionClassification classification,
      @Context UriInfo uriInfo) {
    FlexiBean out = createSearchResultData(page, pageSize, name, classification, uriInfo);
    return getFreemarker().build("regions/regions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("name") String name,
      @QueryParam("classification") RegionClassification classification,
      @Context UriInfo uriInfo) {
    FlexiBean out = createSearchResultData(page, pageSize, name, classification, uriInfo);
    return getFreemarker().build("regions/jsonregions.ftl", out);
  }

  private FlexiBean createSearchResultData(int page, int pageSize, String name, RegionClassification classification, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    RegionSearchRequest searchRequest = new RegionSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setClassification(classification);
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      Identifier id = Identifier.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addRegionKey(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      RegionSearchResult searchResult = data().getRegionMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{regionId}")
  public WebRegionResource findRegion(@PathParam("regionId") String idStr) {
    data().setUriRegionId(idStr);
    UniqueIdentifier oid = UniqueIdentifier.parse(idStr);
    try {
      RegionDocument doc = data().getRegionMaster().get(oid);
      data().setRegion(doc);
    } catch (DataNotFoundException ex) {
      RegionHistoryRequest historyRequest = new RegionHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      RegionHistoryResult historyResult = data().getRegionMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        return null;
      }
      data().setRegion(historyResult.getFirstDocument());
    }
    return new WebRegionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    RegionSearchRequest searchRequest = new RegionSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for regions.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebRegionData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for regions.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebRegionData data, IdentifierBundle identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebRegionsResource.class);
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
