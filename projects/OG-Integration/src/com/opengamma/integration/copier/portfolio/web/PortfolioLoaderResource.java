/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.web;

import java.io.FilterInputStream;
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
import com.opengamma.integration.copier.portfolio.ResolvingPortfolioCopier;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.copier.portfolio.rowparser.ExchangeTradedRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
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
    InputStream fileStream = new WorkaroundInputStream(((BodyPartEntity) fileEntity).getInputStream());
    //String fileContent = IOUtils.toString(fileStream);
    s_logger.info("Portfolio uploaded. fileName: {}, portfolioName: {}, dataField: {}, dataProvider: {}",
                  new Object[]{fileName, portfolioName, dataField, dataProvider});
    final ResolvingPortfolioCopier copier = new ResolvingPortfolioCopier(_historicalTimeSeriesMaster,
                                                                         _bloombergHistoricalTimeSeriesSource,
                                                                         _bloombergReferenceDataProvider,
                                                                         dataProvider,
                                                                         dataFields);
    RowParser rowParser = new ExchangeTradedRowParser(_bloombergSecuritySource);
    SheetFormat format = getFormatForFileName(fileName);
    final PortfolioReader portfolioReader = new SingleSheetSimplePortfolioReader(format, fileStream, rowParser);
    final PortfolioWriter portfolioWriter = new MasterPortfolioWriter(portfolioName, _portfolioMaster, _positionMaster, _securityMaster, false);
    StreamingOutput streamingOutput = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        // TODO callback for progress updates as portoflio is copied
        copier.copy(portfolioReader, portfolioWriter);
        output.write("Upload complete".getBytes());
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

  // TODO this belongs somewhere else
  private static SheetFormat getFormatForFileName(String fileName) {
    if (fileName.toLowerCase().endsWith("csv")) {
      return SheetFormat.CSV;
    } else if (fileName.toLowerCase().endsWith("xls")) {
      return SheetFormat.XLS;
    } else if (fileName.toLowerCase().endsWith("xlsx")) {
      return SheetFormat.XLSX;
    }
    Response response = Response.status(Response.Status.BAD_REQUEST).entity("Portfolio upload only supports CSV " +
                                                                                "files and Excel worksheets").build();
    throw new WebApplicationException(response);
  }

  /**
   * This wraps the file upload input stream to work around a bug in {@code org.jvnet.mimepull} which is used by Jersey
   * Multipart.  The bug causes the {@code read()} method of the file upload stream to throw an exception if it is
   * called twice at the end of the stream which violates the contract of {@link InputStream}.  It ought to
   * keep returning {@code -1} indefinitely.  This class restores that behaviour.
   * TODO Check if this can be removed when we upgrade Jersey. It is a problem when the CSV file doesn't end with a new line
   * @see <a href="http://java.net/jira/browse/JAX_WS-965">The bug report</a>
   */
  private static class WorkaroundInputStream extends FilterInputStream {

    private boolean _ended;

    public WorkaroundInputStream(InputStream in) {
      super(in);
    }

    @Override
    public int read() throws IOException {
      if (_ended) {
        return -1;
      }
      int i = super.read();
      if (i == -1) {
        _ended = true;
      }
      return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
      if (_ended) {
        return -1;
      }
      int i = super.read(b);
      if (i == -1) {
        _ended = true;
      }
      return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (_ended) {
        return -1;
      }
      int i = super.read(b, off, len);
      if (i == -1) {
        _ended = true;
      }
      return i;
    }
  }
}
