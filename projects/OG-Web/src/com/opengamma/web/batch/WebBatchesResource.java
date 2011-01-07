/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.batch.BatchDbManager;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;

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
    out.put("searchRequest", searchRequest);
    
    if (observationDate != null && observationTime != null) {
      BatchSearchResult searchResult = data().getBatchDbManager().search(searchRequest);
      out.put("searchResult", searchResult);
    }
    return getFreemarker().build("batches/batches.ftl", out);
  }
  
  //-------------------------------------------------------------------------
  @Path("{observationDate}/{observationTime}")
  public WebBatchResource findBatch(
      @PathParam("observationDate") String observationDate,
      @PathParam("observationTime") String observationTime) {
    data().setObservationDate(LocalDate.parse(observationDate));
    data().setObservationTime(observationTime);
    ViewComputationResultModel batchResults = data().getBatchDbManager().getResults(
        data().getObservationDate(), 
        data().getObservationTime());
    data().setBatchResults(batchResults);
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
