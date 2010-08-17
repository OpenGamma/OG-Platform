/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.web;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base class for RESTful portfolio resources.
 */
public abstract class AbstractWebPortfolioResource {

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
   * @param data  the backing data, not null
   */
  protected AbstractWebPortfolioResource(final WebPortfoliosData data) {
    ArgumentChecker.notNull(data, "data");
    _data = data;
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
   * Gets the backing bean.
   * @return the beacking bean, not null
   */
  protected WebPortfoliosData data() {
    return _data;
  }

}
