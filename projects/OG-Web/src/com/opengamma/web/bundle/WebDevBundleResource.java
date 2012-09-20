/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * RESTful resource for a CSS/Javascript bundle in development mode.
 */
@Path("/bundles/dev/{bundleId}")
public class WebDevBundleResource extends AbstractWebBundleResource {

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  public WebDevBundleResource(final AbstractWebBundleResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces("text/css")
  public Response get(@PathParam("bundleId") String idStr) {
    return Response.ok(getCssImports(idStr)).build();
  }

  private String getCssImports(String bundleId) {
    BundleManager bundleManager = data().getDevBundleManager();
    Bundle bundle = bundleManager.getBundle(bundleId);
    return BundleUtils.buildImports(bundle, new WebBundlesUris(data()));
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * 
   * @param data  the data, not null
   * @param bundleId  the bundleId, not null
   * @return the URI, not null
   */
  public static URI uri(final WebBundlesData data, String bundleId) {
    return data.getUriInfo().getBaseUriBuilder().path(WebDevBundleResource.class).build(bundleId);
  }

}
