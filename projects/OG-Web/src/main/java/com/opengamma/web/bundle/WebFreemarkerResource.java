/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * RESTful resource allowing any Freemarker template to be called.
 * Only basic data will be available to the page.
 */
@Path("bundles/fm/{file : .*}")
public class WebFreemarkerResource extends AbstractWebBundleResource {

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  public WebFreemarkerResource(final AbstractWebBundleResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(@PathParam("file") String freemarkerFile) {
    return getFreemarker().build(freemarkerFile, createRootData());
  }

}
