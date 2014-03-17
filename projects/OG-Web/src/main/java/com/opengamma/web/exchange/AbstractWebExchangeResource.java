/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.exchange;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful exchange resources.
 */
public abstract class AbstractWebExchangeResource
    extends AbstractPerRequestWebResource<WebExchangeData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "exchanges/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "exchanges/json/";

  /**
   * Creates the resource.
   * 
   * @param exchangeMaster  the exchange master, not null
   */
  protected AbstractWebExchangeResource(final ExchangeMaster exchangeMaster) {
    super(new WebExchangeData());
    ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    data().setExchangeMaster(exchangeMaster);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebExchangeResource(final AbstractWebExchangeResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("uris", new WebExchangeUris(data()));
    return out;
  }

}
