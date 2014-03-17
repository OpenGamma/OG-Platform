/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.exchange.WebExchangeData;
import com.opengamma.web.exchange.WebExchangeUris;
import com.opengamma.web.region.WebRegionData;
import com.opengamma.web.region.WebRegionUris;

/**
 * Abstract base class for RESTful function resources.
 */
public abstract class AbstractWebFunctionResource
    extends AbstractPerRequestWebResource<WebFunctionData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "functions/html/";

  /**
   * Creates the resource.
   * 
   * @param functionConfigurationSource  the function master, not null
   */
  protected AbstractWebFunctionResource(final FunctionConfigurationSource functionConfigurationSource) {
    super(new WebFunctionData());
    ArgumentChecker.notNull(functionConfigurationSource, "functionConfigurationSource");
    data().setFunctionSource(functionConfigurationSource);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebFunctionResource(final AbstractWebFunctionResource parent) {
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
    out.put("uris", new WebFunctionUris(data()));
    WebExchangeData exchangeData = new WebExchangeData(data().getUriInfo());
    out.put("exchangeUris", new WebExchangeUris(exchangeData));
    WebRegionData regionData = new WebRegionData(data().getUriInfo());
    out.put("regionUris", new WebRegionUris(regionData));
    return out;
  }

}
