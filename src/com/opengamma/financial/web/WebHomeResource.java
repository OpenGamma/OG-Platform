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

import com.opengamma.financial.web.position.WebPortfoliosData;
import com.opengamma.financial.web.position.WebPortfoliosUris;
import com.opengamma.financial.web.security.WebSecuritiesData;
import com.opengamma.financial.web.security.WebSecuritiesUris;
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
    
    WebPortfoliosData portfoliosData = new WebPortfoliosData();
    portfoliosData.setUriInfo(uriInfo);
    out.put("portfolioUris", new WebPortfoliosUris(portfoliosData));
    
    WebSecuritiesData securitiesData = new WebSecuritiesData();
    securitiesData.setUriInfo(uriInfo);
    out.put("securitiesUris", new WebSecuritiesUris(securitiesData));
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
