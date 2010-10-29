/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesUris;

/**
 * Abstract base class for RESTful portfolio resources.
 */
public abstract class AbstractWebPortfolioResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebPortfoliosData _data;

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   */
  protected AbstractWebPortfolioResource(final PositionMaster positionMaster) {
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _data = new WebPortfoliosData();
    data().setPositionMaster(positionMaster);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebPortfolioResource(final AbstractWebPortfolioResource parent) {
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
    out.put("uris", new WebPortfoliosUris(data()));
    WebSecuritiesData secData = new WebSecuritiesData(data().getUriInfo());
    out.put("securityUris", new WebSecuritiesUris(secData));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the beacking bean, not null
   */
  protected WebPortfoliosData data() {
    return _data;
  }

}
