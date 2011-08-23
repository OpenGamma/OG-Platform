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
import com.opengamma.financial.batch.BatchDocument;
import com.opengamma.financial.batch.BatchError;
import com.opengamma.financial.batch.BatchGetRequest;
import com.opengamma.id.UniqueId;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for a batch.
 */
@Path("/batches/{batchId}")
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
  public String getHTML(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @Context UriInfo uriInfo) {
    BatchGetRequest request = new BatchGetRequest(data().getBatch().getUniqueId());
    request.setDataPagingRequest(PagingRequest.ofPageDefaulted(page, pageSize));
    request.setErrorPagingRequest(PagingRequest.ALL);
    BatchDocument batchDoc = data().getBatchMaster().get(request);
    data().setBatch(batchDoc);
    
    FlexiBean out = createRootData();
    return getFreemarker().build("batches/batch.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @Context UriInfo uriInfo) {
    BatchGetRequest request = new BatchGetRequest(data().getBatch().getUniqueId());
    request.setDataPagingRequest(PagingRequest.ofPageDefaulted(page, pageSize));
    request.setErrorPagingRequest(PagingRequest.ALL);
    BatchDocument batchDoc = data().getBatchMaster().get(request);
    data().setBatch(batchDoc);
    
    FlexiBean out = createRootData();
    return getFreemarker().build("batches/jsonbatch.ftl", out);
  }

  @GET
  @Produces("text/csv;charset=UTF-8")
  public StreamingOutput getCSV(
      @QueryParam("export") final String export) {
    if (export.equalsIgnoreCase("errors")) {
      return createErrorCsvOutput();
    } else {
      return createDataCsvOutput();
    }
  }

  /**
   * Creates the CSV output for the batch data.
   * 
   * @return the CSV output, not null
   */
  protected StreamingOutput createDataCsvOutput() {
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
        
        BatchGetRequest request = new BatchGetRequest(data().getBatch().getUniqueId());
        request.setDataPagingRequest(PagingRequest.ofPage(1, 1000));
        request.setErrorPagingRequest(PagingRequest.NONE);
        while (true) {
          BatchDocument batchDoc = data().getBatchMaster().get(request);
          for (ViewResultEntry entry : batchDoc.getData()) {
            ComputedValue value = entry.getComputedValue();
            csvWriter.writeNext(new String[] {
              entry.getCalculationConfiguration(),
              value.getSpecification().getTargetSpecification().getUniqueId().toString(),
              value.getSpecification().getValueName(), 
              value.getSpecification().getFunctionUniqueId(),
              value.getValue().toString()
            });
          }
          if (batchDoc.getDataPaging().isLastPage()) {
            break;            
          }
          request.setDataPagingRequest(batchDoc.getDataPaging().nextPagingRequest());
        }
        csvWriter.flush();
        writer.flush();
      }
    };
  }

  /**
   * Creates the CSV output for the batch errors.
   * 
   * @return the CSV output, not null
   */
  protected StreamingOutput createErrorCsvOutput() {
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
          "Exception class",
          "Exception msg",
          "Stack trace"
        });
        
        BatchGetRequest request = new BatchGetRequest(data().getBatch().getUniqueId());
        request.setDataPagingRequest(PagingRequest.NONE);
        request.setErrorPagingRequest(PagingRequest.ofPage(1, 1000));
        while (true) {
          BatchDocument batchDoc = data().getBatchMaster().get(request);
          for (BatchError entry : batchDoc.getErrors()) {
            csvWriter.writeNext(new String[] {
              entry.getCalculationConfiguration(),
              entry.getComputationTarget().getUniqueId().toString(),
              entry.getValueName(), 
              entry.getFunctionUniqueId(),
              entry.getExceptionClass(),
              entry.getExceptionMsg(),
              entry.getStackTrace()
            });
          }
          if (batchDoc.getErrorsPaging().isLastPage()) {
            break;            
          }
          request.setErrorPagingRequest(batchDoc.getErrorsPaging().nextPagingRequest());
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
    BatchDocument batchDoc = data().getBatch();
    out.put("batch", batchDoc);
    out.put("batchDoc", batchDoc);
    out.put("resultPaging", new WebPaging(batchDoc.getDataPaging(), data().getUriInfo()));
    out.put("errorPaging", new WebPaging(batchDoc.getErrorsPaging(), data().getUriInfo()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebBatchData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideBatchId  the override batch id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebBatchData data, final UniqueId overrideBatchId) {
    String batchId = data.getBestBatchUriId(overrideBatchId);
    return data.getUriInfo().getBaseUriBuilder().path(WebBatchResource.class).build(batchId);
  }

}
