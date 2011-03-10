/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.web.batch.WebBatchData;
import com.opengamma.web.batch.WebBatchUris;
import com.opengamma.web.exchange.WebExchangeData;
import com.opengamma.web.exchange.WebExchangeUris;
import com.opengamma.web.holiday.WebHolidayData;
import com.opengamma.web.holiday.WebHolidayUris;
import com.opengamma.web.portfolio.WebPortfoliosData;
import com.opengamma.web.portfolio.WebPortfoliosUris;
import com.opengamma.web.position.WebPositionsData;
import com.opengamma.web.position.WebPositionsUris;
import com.opengamma.web.region.WebRegionData;
import com.opengamma.web.region.WebRegionUris;
import com.opengamma.web.security.WebSecuritiesData;
import com.opengamma.web.security.WebSecuritiesUris;
import com.opengamma.web.timeseries.WebTimeSeriesData;
import com.opengamma.web.timeseries.WebTimeSeriesUris;

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
    
    WebPositionsData positionData = new WebPositionsData();
    positionData.setUriInfo(uriInfo);
    out.put("positionUris", new WebPositionsUris(positionData));
    
    WebSecuritiesData securityData = new WebSecuritiesData();
    securityData.setUriInfo(uriInfo);
    out.put("securityUris", new WebSecuritiesUris(securityData));
    
    WebExchangeData exchangeData = new WebExchangeData();
    exchangeData.setUriInfo(uriInfo);
    out.put("exchangeUris", new WebExchangeUris(exchangeData));
    
    WebHolidayData holidayData = new WebHolidayData();
    holidayData.setUriInfo(uriInfo);
    out.put("holidayUris", new WebHolidayUris(holidayData));
    
    WebRegionData regionData = new WebRegionData();
    regionData.setUriInfo(uriInfo);
    out.put("regionUris", new WebRegionUris(regionData));
    
    WebTimeSeriesData timeseriesData = new WebTimeSeriesData();
    timeseriesData.setUriInfo(uriInfo);
    out.put("timeseriesUris", new WebTimeSeriesUris(timeseriesData));
    
//    WebConfigData<?> configData = new WebConfigData<Object>();
//    configData.setUriInfo(uriInfo);
//    out.put("configUris", new WebConfigUris(configData));
    
    WebBatchData batchData = new WebBatchData();
    batchData.setUriInfo(uriInfo);
    out.put("batchUris", new WebBatchUris(batchData));
    
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
