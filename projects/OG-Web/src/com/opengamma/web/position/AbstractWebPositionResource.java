/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesUris;

/**
 * Abstract base class for RESTful position resources.
 */
public abstract class AbstractWebPositionResource extends AbstractWebResource {
  
  /**
   * The backing bean.
   */
  private final WebPositionsData _data;

  /**
   * The HTS resolver (for getting an HTS Id)
   */
  private final HistoricalTimeSeriesResolver _htsResolver;

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   * @param securitySource  the security source, not null
   */
  protected AbstractWebPositionResource(
      final PositionMaster positionMaster, final SecurityLoader securityLoader, final SecuritySource securitySource,
      final HistoricalTimeSeriesMaster htsMaster, final ConfigSource cfgSource) {
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _data = new WebPositionsData();
    data().setPositionMaster(positionMaster);
    data().setSecurityLoader(securityLoader);
    data().setSecuritySource(securitySource);
    
    _htsResolver = new DefaultHistoricalTimeSeriesResolver(htsMaster, cfgSource);    
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebPositionResource(final AbstractWebPositionResource parent) {
    super(parent);
    _data = parent._data;
    _htsResolver = parent._htsResolver;
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
    out.put("uris", new WebPositionsUris(data()));
    WebSecuritiesData secData = new WebSecuritiesData(data().getUriInfo());
    out.put("securityUris", new WebSecuritiesUris(secData));
    
    return out;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebPositionsData data() {
    return _data;
  }
  
  /**
   * Gets the HTS resolver
   * @return the HTS resolver, not null
   */
  protected HistoricalTimeSeriesResolver htsResolver() {
    return _htsResolver;
  }
  
}
