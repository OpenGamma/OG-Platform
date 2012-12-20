/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful exchange resources.
 */
public abstract class AbstractWebExchangeResource extends AbstractPerRequestWebResource {

  /**
   * The backing bean.
   */
  private final WebExchangeData _data;

  /**
   * Creates the resource.
   * @param exchangeMaster  the exchange master, not null
   */
  protected AbstractWebExchangeResource(final ExchangeMaster exchangeMaster) {
    ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    _data = new WebExchangeData();
    data().setExchangeMaster(exchangeMaster);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebExchangeResource(final AbstractWebExchangeResource parent) {
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
    out.put("uris", new WebExchangeUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the beacking bean, not null
   */
  protected WebExchangeData data() {
    return _data;
  }

}
