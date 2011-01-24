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

import com.opengamma.financial.batch.BatchDbManager;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.rest.WebPaging;

/**
 * RESTful resource for all batches.
 * <p>
 * The batches resource represents the whole of a batch database.
 */
@Path("/batches")
public class WebBatchesResource extends AbstractWebBatchResource {
  
  /**
   * Creates the resource.
   * @param batchDbManager  the batch DB manager, not null
   */
  public WebBatchesResource(final BatchDbManager batchDbManager) {
    super(batchDbManager);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("observationDate") String observationDate,
      @QueryParam("observationTime") String observationTime,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    BatchSearchRequest searchRequest = new BatchSearchRequest();
    
    observationDate = StringUtils.trimToNull(observationDate);
    if (observationDate != null) {
      searchRequest.setObservationDate(LocalDate.parse(observationDate));
    }
    
    observationTime = StringUtils.trimToNull(observationTime);
    searchRequest.setObservationTime(observationTime);
    
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      BatchSearchResult searchResult = data().getBatchDbManager().search(searchRequest);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
      out.put("searchResult", searchResult);
    }
    return getFreemarker().build("batches/batches.ftl", out);
  }
  
  //-------------------------------------------------------------------------
  @Path("{observationDate}/{observationTime}")
  public WebBatchResource findBatch(
      @PathParam("observationDate") String observationDate,
      @PathParam("observationTime") String observationTime) {
    
    BatchSearchRequest request = new BatchSearchRequest();
    request.setObservationDate(LocalDate.parse(observationDate));
    request.setObservationTime(observationTime);
    
    BatchSearchResult batchResults = data().getBatchDbManager().search(request);
    if (batchResults.getItems().size() != 1) {
      throw new RuntimeException("Expected 1 result, got " + batchResults.getItems().size());
    }
    data().setBatch(batchResults.getItems().get(0));
    
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
