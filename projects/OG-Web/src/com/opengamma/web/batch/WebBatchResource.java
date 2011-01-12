/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.batch;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import javax.time.calendar.LocalDate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import au.com.bytecode.opencsv.CSVWriter;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.financial.batch.BatchDataSearchRequest;
import com.opengamma.financial.batch.BatchDataSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.rest.WebPaging;

/**
 * RESTful resource for a batch.
 */
@Path("/batches/{observationDate}/{observationTime}")
public class WebBatchResource extends AbstractWebBatchResource {
  
  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebBatchResource(final AbstractWebBatchResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    BatchDataSearchRequest request = new BatchDataSearchRequest();
    request.setObservationDate(data().getObservationDate());
    request.setObservationTime(data().getObservationTime());
    request.setPagingRequest(PagingRequest.of(page, pageSize));
    
    BatchDataSearchResult batchResults = data().getBatchDbManager().getResults(request);
    data().setBatchResults(batchResults.getItems());

    out.put("paging", new WebPaging(batchResults.getPaging(), data().getUriInfo()));
    out.put("batchResult", batchResults.getItems());
    return getFreemarker().build("batches/batch.ftl", out);
  }
  
  @GET
  @Produces("text/csv;charset=UTF-8")
  public StreamingOutput getCsv() {
    return new StreamingOutput() {
      
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        CSVWriter csvWriter = new CSVWriter(writer);
        csvWriter.writeNext(new String[] {
          "Calculation configuration",
          "Computation target unique id",
          "Value name",
          "Function unique id",
          "Value"
        });
        
        int page = 1;
        int pageSize = 1000;
        
        while (true) {
        
          BatchDataSearchRequest request = new BatchDataSearchRequest();
          request.setObservationDate(data().getObservationDate());
          request.setObservationTime(data().getObservationTime());
          request.setPagingRequest(PagingRequest.of(page, pageSize));
          
          BatchDataSearchResult batchResults = data().getBatchDbManager().getResults(request);
          for (ViewResultEntry entry : batchResults.getItems()) {
            ComputedValue value = entry.getComputedValue();
            
            csvWriter.writeNext(new String[] {
              entry.getCalculationConfiguration(),
              value.getSpecification().getTargetSpecification().getUniqueId().toString(),
              value.getSpecification().getValueName(), 
              value.getSpecification().getFunctionUniqueId(),
              value.getValue().toString()
            });
          }
          
          if (batchResults.getPaging().isLastPage()) {
            break;            
          }

          page++;
        }
        
        csvWriter.flush();
        writer.flush();
      }
    };
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("observationDate", data().getObservationDate());
    out.put("observationTime", data().getObservationTime());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebBatchData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebBatchResource.class).build(
        data.getObservationDate(),
        data.getObservationTime());
  }
  
  public static URI uri(final WebBatchData data, final LocalDate date, final String observationTime) {
    return data.getUriInfo().getBaseUriBuilder().path(WebBatchResource.class).build(
        date,
        observationTime);
  }

}
