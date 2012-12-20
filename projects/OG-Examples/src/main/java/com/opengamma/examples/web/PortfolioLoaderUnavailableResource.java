/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Placeholder REST resource for portfolio upload in the examples project.  The upload depends on Bloomberg
 * data and therefore isn't available in OG-Examples but the upload button in the UI is still there.  This class
 * prevents an ugly 404 error if the user tries to upload a portfolio.
 */
@Path("portfolioupload")
public class PortfolioLoaderUnavailableResource {

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Response portfolioLoaderUnavailable() {
    return Response.ok("Portfolio upload requires Bloomberg data  \nPlease see OG-BloombergExamples").build();
  }
}
