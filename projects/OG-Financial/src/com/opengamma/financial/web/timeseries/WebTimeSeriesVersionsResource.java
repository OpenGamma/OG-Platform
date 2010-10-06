/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web.timeseries;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesSearchHistoricRequest;
import com.opengamma.financial.timeseries.TimeSeriesSearchHistoricResult;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for all versions of a time series.
 */
@Path("/timeseries/{timeseriesId}/versions")
@Produces(MediaType.TEXT_HTML)
public class WebTimeSeriesVersionsResource extends AbstractWebTimeSeriesResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebTimeSeriesVersionsResource(final AbstractWebTimeSeriesResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    TimeSeriesSearchHistoricRequest request = new TimeSeriesSearchHistoricRequest();
    request.setTimeSeriesId(data().getTimeSeries().getUniqueIdentifier());
    TimeSeriesSearchHistoricResult<?> result = data().getTimeSeriesMaster().searchHistoric(request);
    
    FlexiBean out = createRootData();
    out.put("versionsResult", result);
    out.put("versions", result.getDocuments());
    return getFreemarker().build("timeseries/timeseriesversions.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    TimeSeriesDocument<?> doc = data().getTimeSeries();
    out.put("timeseriesDoc", doc);
    out.put("timeseries", doc.getTimeSeries());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("{versionId}")
  public WebTimeSeriesVersionResource findVersion(@PathParam("versionId") String idStr) {
    data().setUriVersionId(idStr);
    TimeSeriesDocument<?> doc = data().getTimeSeries();
    UniqueIdentifier combined = doc.getUniqueIdentifier().withVersion(idStr);
    if (doc.getUniqueIdentifier().equals(combined) == false) {
      TimeSeriesDocument<?> versioned = data().getTimeSeriesMaster().getTimeSeries(combined);
      data().setVersioned(versioned);
    } else {
      data().setVersioned(doc);
    }
    return new WebTimeSeriesVersionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebTimeSeriesData data) {
    String timeseriesId = data.getBestTimeSeriesUriId(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebTimeSeriesVersionsResource.class).build(timeseriesId);
  }

}
