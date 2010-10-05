/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web.timeseries;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for a version of a time series.
 */
@Path("/timeseries/{timeseriesId}/versions/{versionId}")
@Produces(MediaType.TEXT_HTML)
public class WebTimeSeriesVersionResource extends AbstractWebTimeSeriesResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebTimeSeriesVersionResource(final AbstractWebTimeSeriesResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("timeseries/timeseriesversion.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    TimeSeriesDocument<?> latestTsDoc = data().getTimeSeries();
    TimeSeriesDocument<?> versionedTs = data().getVersioned();
    out.put("latestTimeseriesDoc", latestTsDoc);
    out.put("latestTimeseries", latestTsDoc.getTimeSeries());
    out.put("timeseriesDoc", versionedTs);
    out.put("timeseries", versionedTs.getTimeSeries());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebTimeSeriesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideVersionId  the override version id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebTimeSeriesData data, final UniqueIdentifier overrideVersionId) {
    String timeSeriesId = data.getBestTimeSeriesUriId(null);
    String versionId = StringUtils.defaultString(overrideVersionId != null ? overrideVersionId.getVersion() : data.getUriVersionId());
    return data.getUriInfo().getBaseUriBuilder().path(WebTimeSeriesVersionResource.class).build(timeSeriesId, versionId);
  }

}
