/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.batch;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import au.com.bytecode.opencsv.CSVWriter;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;

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
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("batches/batch.ftl", out);
  }
  
  @GET
  @Produces("text/csv")
  public String getCsv() {
    StringWriter stringWriter  = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);
    csvWriter.writeNext(new String[] {
      "Calculation configuration",
      "Computation target unique id",
      "Value name",
      "Function unique id",
      "Value"
    });
    for (String calculationConfiguration : data().getBatchResults().getCalculationConfigurationNames()) {
      ViewCalculationResultModel result = data().getBatchResults().getCalculationResult(calculationConfiguration);
      for (ComputationTargetSpecification spec : result.getAllTargets()) {
        Map<String, ComputedValue> results = result.getValues(spec);
        
        for (ComputedValue value : results.values()) {
          csvWriter.writeNext(new String[] {
            calculationConfiguration,
            spec.getUniqueId().toString(),
            value.getSpecification().getValueName(), 
            value.getSpecification().getFunctionUniqueId(),
            value.getValue().toString()
          });
        }
      }
    }
    return stringWriter.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ViewComputationResultModel batchResult = data().getBatchResults();
    out.put("batchResult", batchResult);
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
