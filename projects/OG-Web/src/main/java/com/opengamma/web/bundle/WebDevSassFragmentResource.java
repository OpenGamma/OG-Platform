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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.io.Files;

/**
 * RESTful resource for a Sass fragment in development mode.
 */
@Path("/bundles/sass/{fragment: .*}")
public class WebDevSassFragmentResource extends AbstractWebBundleResource {
  
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
    fragment = StringUtils.stripToNull(fragment);
    Response result = Response.status(Status.NOT_FOUND).build();
    
    if (fragment != null) {
      File templateDir = new File(data().getBundleManagerFactory().getUriProvider().getUri(""));
      fragment = FilenameUtils.removeExtension(fragment) + "." + BundleType.SCSS.getSuffix();
      try {
        String sassContent = Files.toString(new File(templateDir, fragment), Charset.defaultCharset());
        String cssContent = data().getSassCompiler().sassConvert(sassContent);
        result = Response.ok(cssContent).build();
      } catch (IOException ex) {
        result = Response.status(Status.NOT_FOUND).build();
      }
    } 
    return result;
    
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
