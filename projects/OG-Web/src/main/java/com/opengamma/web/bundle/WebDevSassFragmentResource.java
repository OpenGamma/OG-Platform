/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.io.Files;
import com.opengamma.web.sass.RubySassCompiler;

/**
 * RESTful resource for a Sass fragment in development mode.
 */
@Path("/bundles/sass/{fragment: .*}")
public class WebDevSassFragmentResource extends AbstractWebBundleResource {
    
  private static final RubySassCompiler s_sassCompiler = RubySassCompiler.getInstance();
  
  private static final File CSS_DIR = new File(System.getProperty("java.io.tmpdir"), ".og-css");
  
  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  public WebDevSassFragmentResource(final AbstractWebBundleResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces("text/css")
  public Response get(@PathParam("fragment") String fragment) {
    File templateDir = new File(data().getBundleManagerFactory().getUriProvider().getUri(""));    
    s_sassCompiler.updateStyleSheets(templateDir, CSS_DIR);
    String cssContent = null;
    try {
      cssContent = Files.toString(new File(CSS_DIR, fragment), Charset.defaultCharset());
    } catch (IOException ex) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok(cssContent).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * 
   * @param data  the data, not null
   * @param fragmentPath  the fragment, not null
   * @return the URI, not null
   */
  public static URI uri(final WebBundlesData data, String fragmentPath) {
    String baseDir = data.getBundleManagerFactory().getBaseDir() + "/";
    if (fragmentPath.startsWith(baseDir)) {
      fragmentPath = fragmentPath.substring(baseDir.length());
    }
    return data.getUriInfo().getBaseUriBuilder().path(WebDevSassFragmentResource.class).build(fragmentPath);
  }

}
