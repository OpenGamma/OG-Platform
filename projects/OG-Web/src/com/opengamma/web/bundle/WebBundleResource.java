/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a security.
 */
@Path("/bundle/{bundleId}")
public class WebBundleResource {
   
  private final CompressedBundleSource _compressor;
  
  /**
   * Creates the resource.
   * @param compressor  the bundle compressor, not null
   */
  public WebBundleResource(final CompressedBundleSource compressor) {
    ArgumentChecker.notNull(compressor, "BundleCompressor");
    _compressor = compressor;
  }
    
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response get(@PathParam("bundleId") String idStr) {
    String compressedContent = _compressor.getBundle(idStr);
    return Response.ok(compressedContent).build();
  }
  
}
