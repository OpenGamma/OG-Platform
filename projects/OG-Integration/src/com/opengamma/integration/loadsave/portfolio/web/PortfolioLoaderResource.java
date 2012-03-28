/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.integration.loadsave.portfolio.ResolvingPortfolioCopier;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
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
  private final ResolvingPortfolioCopier _copier;

  /*public PortfolioLoaderResource(BloombergSecuritySource bbgSecuritySource,
                                 HistoricalTimeSeriesMaster htsMaster,
                                 HistoricalTimeSeriesSource bbgHtsSource,
                                 ReferenceDataProvider bbgRefDataProvider,
                                 PortfolioMaster portfolioMaster,
                                 PositionMaster positionMaster,
                                 SecurityMaster securityMaster) {
    ArgumentChecker.notNull(bbgSecurityMaster, "bbgSecurityMaster");
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(bbgHtsSource, "bbgHtsSource");
    ArgumentChecker.notNull(bbgRefDataProvider, "bbgRefDataProvider");
    _copier = new ResolvingPortfolioLoader(bbgSecurityMaster,
                                           htsMaster,
                                           bbgHtsSource,
                                           bbgRefDataProvider,
                                           portfolioMaster,
                                           positionMaster,
                                           securityMaster);
  }*/

  public PortfolioLoaderResource() {
    _copier = null;
  }

  @Path("{updatePeriod}/{updateCount}")
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response uploadPortfolio(FormDataMultiPart formData,
                                  // TODO according to the docs this should work but jersey won't start with them uncommented
                                  //@FormDataParam("file") FormDataBodyPart fileBodyPart,
                                  //@FormDataParam("portfolioName") String portfolioName,
                                  //@FormDataParam("dataField") String dataField,
                                  @PathParam("updatePeriod") final long updatePeriod,
                                  @PathParam("updateCount") final int updateCount) throws IOException {
    FormDataBodyPart fileBodyPart = getBodyPart(formData, "file");
    FormDataBodyPart dataFieldBodyPart = getBodyPart(formData, "dataField");
    FormDataBodyPart bodyPart = getBodyPart(formData, "portfolioName");
    String dataField = dataFieldBodyPart.getValue();
    String portfolioName = bodyPart.getValue();
    String fileName = fileBodyPart.getFormDataContentDisposition().getFileName();
    Object fileEntity = fileBodyPart.getEntity();
    if (!(fileEntity instanceof BodyPartEntity)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    InputStream fileStream = ((BodyPartEntity) fileEntity).getInputStream();
    String fileContent = IOUtils.toString(fileStream);
    s_logger.warn("Portfolio uploaded. fileName: {}, portfolioName: {}, dataField: {}, portfolio: {}",
                  new Object[]{fileName, portfolioName, dataField, fileContent});
    // TODO fix the args
    // TODO stream the output back to the web
    //_copier.loadPortfolio(portfolioName, fileName, fileStream, "", "", new String[]{dataField}, "", true);
    StreamingOutput streamingOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        output.write("<html><head><style type=text/css>".getBytes());
        output.write(("body {font: normal 12px/160% arial, helvetica, sans-serif;}" +
            "div {position: absolute; top: 30px; left: 0; right: 0; background: #fff;}").getBytes());
        output.write("</style></head><body>".getBytes());
        output.write("<header>Please wait, this may take a few minutes...</header>".getBytes());
        for (int i = 0; i < updateCount; i++) {
          try {
            Thread.sleep(updatePeriod);
          } catch (InterruptedException e) {
            s_logger.warn("This shouldn't happen");
          }
          if (i == updateCount - 1) {
            output.write(("<div>Done</div>").getBytes());
          } else {
            output.write(("<div>uploading " + i + " ...</div>").getBytes());
          }
          output.flush();
        }
        output.write("</body></html>".getBytes());
      }
    };
    return Response.ok(streamingOutput).build();
  }

  private static FormDataBodyPart getBodyPart(FormDataMultiPart formData, String fieldName) {
    FormDataBodyPart bodyPart = formData.getField(fieldName);
    if (bodyPart == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    return bodyPart;
  }
}
