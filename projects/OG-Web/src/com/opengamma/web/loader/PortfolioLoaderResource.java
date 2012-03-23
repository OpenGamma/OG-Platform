/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.loader;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * REST resource that uploads a CSV file containing a portfolio definition and passes it to a portfolio loader.
 * TODO currently it doesn't do anything except read the file and send back some made-up data to the client
 */
@Path("portfolioupload")
public class PortfolioLoaderResource {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderResource.class);

  @Path("{updatePeriod}/{updateCount}")
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public void uploadPortfolio(FormDataMultiPart formData,
                              // TODO according to the docs this should work but jersey won't start with them uncommented
                              //@FormDataParam("file") FormDataBodyPart fileBodyPart,
                              //@FormDataParam("dataField") String dataField,
                              @PathParam("updatePeriod") long updatePeriod,
                              @PathParam("updateCount") int updateCount,
                              @Context HttpServletResponse response) throws IOException {
    FormDataBodyPart fileBodyPart = formData.getField("file");
    if (fileBodyPart == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    FormDataBodyPart dataFieldBodyPart = formData.getField("dataField");
    if (dataFieldBodyPart == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String dataField = dataFieldBodyPart.getValue();
    Object fileEntity = fileBodyPart.getEntity();
    if (!(fileEntity instanceof BodyPartEntity)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    InputStream fileStream = ((BodyPartEntity) fileEntity).getInputStream();
    String fileContent = IOUtils.toString(fileStream);
    s_logger.warn("Portfolio uploaded. dataField: {}, portfolio: {}", dataField, fileContent);
    ServletOutputStream outputStream = response.getOutputStream();
    for (int i = 0; i < updateCount; i++) {
      try {
        Thread.sleep(updatePeriod);
      } catch (InterruptedException e) {
        s_logger.warn("This shouldn't happen");
      }
      outputStream.write("uploading portfolio...\n".getBytes());
      outputStream.flush();
    }
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
