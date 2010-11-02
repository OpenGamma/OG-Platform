/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.security.master.SecurityLoader;
import com.opengamma.financial.security.master.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractWebResource;
import com.opengamma.web.WebHomeUris;
import com.opengamma.web.position.WebPortfoliosData;
import com.opengamma.web.position.WebPortfoliosUris;

/**
 * Abstract base class for RESTful security resources.
 */
public abstract class AbstractWebSecurityResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebSecuritiesData _data;

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   */
  protected AbstractWebSecurityResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    _data = new WebSecuritiesData();
    data().setSecurityMaster(securityMaster);
    data().setSecurityLoader(securityLoader);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebSecurityResource(final AbstractWebSecurityResource parent) {
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
    out.put("uris", new WebSecuritiesUris(data()));
    WebPortfoliosData portData = new WebPortfoliosData(data().getUriInfo());
    out.put("portfolioUris", new WebPortfoliosUris(portData));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the beacking bean, not null
   */
  protected WebSecuritiesData data() {
    return _data;
  }

}
