/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.web.config.WebConfigData;
import com.opengamma.financial.web.config.WebConfigUris;
import com.opengamma.financial.web.exchange.WebExchangeData;
import com.opengamma.financial.web.exchange.WebExchangeUris;
import com.opengamma.financial.web.position.WebPortfoliosData;
import com.opengamma.financial.web.position.WebPortfoliosUris;
import com.opengamma.financial.web.security.WebSecuritiesData;
import com.opengamma.financial.web.security.WebSecuritiesUris;
import com.opengamma.financial.web.timeseries.WebTimeSeriesData;
import com.opengamma.financial.web.timeseries.WebTimeSeriesUris;
import com.opengamma.util.rest.AbstractWebResource;

/**
 * RESTful resource for the home page.
 */
@Path("/")
public class WebHomeResource extends AbstractWebResource {

  /**
   * Creates the resource.
   */
  public WebHomeResource() {
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(@Context UriInfo uriInfo) {
    FlexiBean out = createRootData(uriInfo);
    return getFreemarker().build("home.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @param uriInfo  the URI information, not null
   * @return the output root data, not null
   */
  protected FlexiBean createRootData(UriInfo uriInfo) {
    FlexiBean out = getFreemarker().createRootData();
    out.put("uris", new WebHomeUris(uriInfo));
    
    WebPortfoliosData portfolioData = new WebPortfoliosData();
    portfolioData.setUriInfo(uriInfo);
    out.put("portfolioUris", new WebPortfoliosUris(portfolioData));
    
    WebSecuritiesData securityData = new WebSecuritiesData();
    securityData.setUriInfo(uriInfo);
    out.put("securityUris", new WebSecuritiesUris(securityData));
    
    WebExchangeData exchangeData = new WebExchangeData();
    exchangeData.setUriInfo(uriInfo);
    out.put("exchangeUris", new WebExchangeUris(exchangeData));
    
    WebTimeSeriesData timeseriesData = new WebTimeSeriesData();
    timeseriesData.setUriInfo(uriInfo);
    out.put("timeseriesUris", new WebTimeSeriesUris(timeseriesData));
    
    WebConfigData<?> configData = new WebConfigData<Object>();
    configData.setUriInfo(uriInfo);
    out.put("configUris", new WebConfigUris(configData));
    
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this page.
   * @param uriInfo  the uriInfo, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(WebHomeResource.class).build();
  }

}
