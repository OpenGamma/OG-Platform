/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaldata;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.historicaldata.HistoricalDataLoader;
import com.opengamma.master.historicaldata.HistoricalDataMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful historical data resources.
 */
public abstract class AbstractWebHistoricalDataResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebHistoricalDataData _data;

  /**
   * Creates the resource.
   * @param master  the historical data master, not null
   * @param loader  the historical data loader, not null
   */
  protected AbstractWebHistoricalDataResource(final HistoricalDataMaster master, final HistoricalDataLoader loader) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(loader, "loader");
    _data = new WebHistoricalDataData();
    data().setTimeSeriesMaster(master);
    data().setTimeSeriesLoader(loader);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebHistoricalDataResource(final AbstractWebHistoricalDataResource parent) {
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
    out.put("uris", new WebHistoricalDataUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebHistoricalDataData data() {
    return _data;
  }

}
