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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.BloombergSecuritySource;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.integration.loadsave.portfolio.ResolvingPortfolioCopier;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.loadsave.portfolio.rowparser.ExchangeTradedRowParser;
import com.opengamma.integration.loadsave.portfolio.rowparser.RowParser;
import com.opengamma.integration.loadsave.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
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
 */
@Path("portfolioupload")
public class PortfolioLoaderResource {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderResource.class);
  private final HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  private final HistoricalTimeSeriesSource _bloombergHistoricalTimeSeriesSource;
  private final ReferenceDataProvider _bloombergReferenceDataProvider;
  private final BloombergSecuritySource _bloombergSecuritySource;
  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;

  public PortfolioLoaderResource(HistoricalTimeSeriesMaster historicalTimeSeriesMaster,
                                 HistoricalTimeSeriesSource bloombergHistoricalTimeSeriesSource,
                                 ReferenceDataProvider bloombergReferenceDataProvider,
                                 BloombergSecuritySource bloombergSecuritySource,
                                 PortfolioMaster portfolioMaster,
                                 PositionMaster positionMaster, SecurityMaster securityMaster) {
    ArgumentChecker.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    ArgumentChecker.notNull(bloombergHistoricalTimeSeriesSource, "bloombergHistoricalTimeSeriesSource");
    ArgumentChecker.notNull(bloombergReferenceDataProvider, "bloombergReferenceDataProvider");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _bloombergSecuritySource = bloombergSecuritySource;
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
    _bloombergHistoricalTimeSeriesSource = bloombergHistoricalTimeSeriesSource;
    _bloombergReferenceDataProvider = bloombergReferenceDataProvider;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Response uploadPortfolio(FormDataMultiPart formData
                                  // TODO not sure why these don't work
                                  //@FormDataParam("file") FormDataBodyPart fileBodyPart,
                                  //@FormDataParam("file") InputStream fileStream,
                                  //@FormDataParam("portfolioName") String portfolioName,
                                  //@FormDataParam("dataField") String dataField
                                  //@FormDataParam("dataProvider") String dataProvider
                                  ) throws IOException {
    String dataField = getString(formData, "dataField");
    String dataProvider = getString(formData, "dataProvider");
    String portfolioName = getString(formData, "portfolioName");
    FormDataBodyPart fileBodyPart = getBodyPart(formData, "file");
    String fileName = fileBodyPart.getFormDataContentDisposition().getFileName();
    Object fileEntity = fileBodyPart.getEntity();
    // fields can be separated by whitespace or a comma with whitespace
    String[] dataFields = dataField.split("(\\s*,\\s*|\\s+)");
    if (!(fileEntity instanceof BodyPartEntity)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    InputStream fileStream = ((BodyPartEntity) fileEntity).getInputStream();
    //String fileContent = IOUtils.toString(fileStream);
    s_logger.info("Portfolio uploaded. fileName: {}, portfolioName: {}, dataField: {}, dataProvider: {}",
                  new Object[]{fileName, portfolioName, dataField, dataProvider});
    final ResolvingPortfolioCopier copier = new ResolvingPortfolioCopier(_historicalTimeSeriesMaster,
                                                                         _bloombergHistoricalTimeSeriesSource,
                                                                         _bloombergReferenceDataProvider,
                                                                         dataProvider,
                                                                         dataFields);
    RowParser rowParser = new ExchangeTradedRowParser(_bloombergSecuritySource);
    final PortfolioReader portfolioReader = new SingleSheetSimplePortfolioReader(fileName, fileStream, rowParser);
    final PortfolioWriter portfolioWriter = new MasterPortfolioWriter(portfolioName, _portfolioMaster, _positionMaster, _securityMaster);
    StreamingOutput streamingOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        // TODO callback for progress updates as portoflio is copied
        copier.copy(portfolioReader, portfolioWriter);
      }
    };
    return Response.ok(streamingOutput).build();
  }

  private static FormDataBodyPart getBodyPart(FormDataMultiPart formData, String fieldName) {
    FormDataBodyPart bodyPart = formData.getField(fieldName);
    if (bodyPart == null) {
      Response response = Response.status(Response.Status.BAD_REQUEST).entity("Missing form field: " + fieldName).build();
      throw new WebApplicationException(response);
    }
    return bodyPart;
  }

  private static String getString(FormDataMultiPart formData, String fieldName) {
    FormDataBodyPart bodyPart = getBodyPart(formData, fieldName);
    String value = bodyPart.getValue();
    if (StringUtils.isEmpty(value)) {
      Response response = Response.status(Response.Status.BAD_REQUEST).entity("Missing form value: " + fieldName).build();
      throw new WebApplicationException(response);
    }
    return value;
  }
}
