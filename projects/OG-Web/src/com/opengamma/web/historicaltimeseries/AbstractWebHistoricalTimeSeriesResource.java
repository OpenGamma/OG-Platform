/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaltimeseries;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful historical time-series resources.
 */
public abstract class AbstractWebHistoricalTimeSeriesResource extends AbstractPerRequestWebResource {

  /**
   * The backing bean.
   */
  private final WebHistoricalTimeSeriesData _data;

  /**
   * Creates the resource.
   * @param master  the historical data master, not null
   * @param loader  the historical data loader, not null
   */
  protected AbstractWebHistoricalTimeSeriesResource(final HistoricalTimeSeriesMaster master, final HistoricalTimeSeriesLoader loader) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(loader, "loader");
    _data = new WebHistoricalTimeSeriesData();
    data().setHistoricalTimeSeriesMaster(master);
    data().setHistoricalTimeSeriesLoader(loader);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebHistoricalTimeSeriesResource(final AbstractWebHistoricalTimeSeriesResource parent) {
    super(parent);
    _data = parent._data;
  }

  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebHistoricalTimeSeriesUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebHistoricalTimeSeriesData data() {
    return _data;
  }

}
