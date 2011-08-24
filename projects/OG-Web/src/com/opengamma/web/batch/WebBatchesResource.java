/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.batch;

import java.net.URI;

import javax.time.calendar.LocalDate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.batch.BatchDocument;
import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.id.UniqueId;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all batches.
 * <p>
 * The batches resource represents the whole of a batch database.
 */
@Path("/batches")
public class WebBatchesResource extends AbstractWebBatchResource {

  /**
   * Creates the resource.
   * @param batchMaster  the batch master, not null
   */
  public WebBatchesResource(final BatchMaster batchMaster) {
    super(batchMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("observationDate") String observationDate,
      @QueryParam("observationTime") String observationTime,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, observationDate, observationTime, uriInfo);
    return getFreemarker().build("batches/batches.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("observationDate") String observationDate,
      @QueryParam("observationTime") String observationTime,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, observationDate, observationTime, uriInfo);
    return getFreemarker().build("batches/jsonbatches.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, String observationDate, String observationTime, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    BatchSearchRequest searchRequest = new BatchSearchRequest();
    
    observationDate = StringUtils.trimToNull(observationDate);
    if (observationDate != null) {
      searchRequest.setObservationDate(LocalDate.parse(observationDate));
    }
    
    observationTime = StringUtils.trimToNull(observationTime);
    searchRequest.setObservationTime(observationTime);
    
    searchRequest.setPagingRequest(pr);
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      BatchSearchResult searchResult = data().getBatchMaster().search(searchRequest);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
      out.put("searchResult", searchResult);
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{batchId}")
  public WebBatchResource findSecurity(@PathParam("batchId") String idStr) {
    data().setUriBatchId(idStr);
    UniqueId oid = UniqueId.parse(idStr);
    try {
      BatchDocument doc = data().getBatchMaster().get(oid);
      data().setBatch(doc);
    } catch (DataNotFoundException ex) {
      return null;
    }
    return new WebBatchResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    BatchSearchRequest searchRequest = new BatchSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for time series.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebBatchData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebBatchesResource.class).build();
  }

}
