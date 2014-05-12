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

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.integration.copier.portfolio.ResolvingPortfolioCopier;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePositionReader;
import com.opengamma.integration.copier.portfolio.rowparser.ExchangeTradedRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.portfolio.writer.MasterPositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.tool.portfolio.xml.SchemaRegister;
import com.opengamma.integration.tool.portfolio.xml.XmlFileReader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.security.SecurityProvider;
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

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;
  private final HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  private final SecurityProvider _securityProvider;
  private final HistoricalTimeSeriesProvider _historicalTimeSeriesProvider;
  private final ReferenceDataProvider _referenceDataProvider;

  /**
   * Creates an instance.
   * 
   * @param portfolioMaster  the master, not null
   * @param positionMaster  the master, not null
   * @param securityMaster  the master, not null
   * @param historicalTimeSeriesMaster  the master, not null
   * @param securityProvider  the provider, not null
   * @param historicalTimeSeriesProvider  the provider, not null
   * @param bloombergReferenceDataProvider  the provider, not null
   */
  public PortfolioLoaderResource(PortfolioMaster portfolioMaster,
                                 PositionMaster positionMaster,
                                 SecurityMaster securityMaster,
                                 HistoricalTimeSeriesMaster historicalTimeSeriesMaster,
                                 SecurityProvider securityProvider,
                                 HistoricalTimeSeriesProvider historicalTimeSeriesProvider,
                                 ReferenceDataProvider bloombergReferenceDataProvider) {
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    ArgumentChecker.notNull(historicalTimeSeriesProvider, "historicalTimeSeriesProvider");
    ArgumentChecker.notNull(bloombergReferenceDataProvider, "referenceDataProvider");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
    _securityProvider = securityProvider;
    _historicalTimeSeriesProvider = historicalTimeSeriesProvider;
    _referenceDataProvider = bloombergReferenceDataProvider;
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  @SuppressWarnings("resource")
  public Response uploadPortfolio(FormDataMultiPart formData) throws IOException {
    FormDataBodyPart fileBodyPart = getBodyPart(formData, "file");
    FormDataBodyPart filexmlBodyPart = getBodyPart(formData, "filexml");

    if (filexmlBodyPart.getFormDataContentDisposition().getFileName().toLowerCase().endsWith("xml")) {
      // xml can contain multiple portfolios
      Object filexmlEntity = filexmlBodyPart.getEntity();
      InputStream filexmlStream = new WorkaroundInputStream(((BodyPartEntity) filexmlEntity).getInputStream());
      for (PositionReader positionReader : returnPorfolioReader(filexmlStream)) {
        xmlPortfolioCopy(positionReader);
      }
      return Response.ok("Upload complete").build();
    } else {
      Object fileEntity = fileBodyPart.getEntity();
      String fileName = fileBodyPart.getFormDataContentDisposition().getFileName();
      InputStream fileStream = new WorkaroundInputStream(((BodyPartEntity) fileEntity).getInputStream());
      String dataField = getString(formData, "dataField");
      String dataProvider = getString(formData, "dataProvider");
      String portfolioName = getString(formData, "portfolioName");
      String dateFormatName = getString(formData, "dateFormat");
      // fields can be separated by whitespace or a comma with whitespace
      String[] dataFields = dataField.split("(\\s*,\\s*|\\s+)");

      s_logger.info("Portfolio uploaded. fileName: {}, portfolioName: {}, dataField: {}, dataProvider: {}",
                    fileName, portfolioName, dataField, dataProvider);

      if (fileEntity == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
      final ResolvingPortfolioCopier copier = new ResolvingPortfolioCopier(_historicalTimeSeriesMaster,
                                                                           _historicalTimeSeriesProvider,
                                                                           _referenceDataProvider,
                                                                           dataProvider,
                                                                           dataFields);
      final PositionWriter positionWriter =
          new MasterPositionWriter(portfolioName, _portfolioMaster, _positionMaster, _securityMaster, false, false, true);
      SheetFormat format = getFormatForFileName(fileName);
      ExchangeTradedRowParser.DateFormat dateFormat = Enum.valueOf(ExchangeTradedRowParser.DateFormat.class, dateFormatName);
      RowParser rowParser = new ExchangeTradedRowParser(_securityProvider, dateFormat);
      final PositionReader positionReader = new SingleSheetSimplePositionReader(format, fileStream, rowParser);
      StreamingOutput streamingOutput = new StreamingOutput() {
        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
          // TODO callback for progress updates as portoflio is copied
          copier.copy(positionReader, positionWriter);
          output.write("Upload complete".getBytes());
        }
      };
      return Response.ok(streamingOutput).build();
    }
  }

  private void xmlPortfolioCopy(PositionReader positionReader) {

    SimplePortfolioCopier copier = new SimplePortfolioCopier(null);
    final PositionWriter positionWriter = new MasterPositionWriter(positionReader.getPortfolioName(),
                                                                      _portfolioMaster, _positionMaster,
                                                                      _securityMaster, false, false, true);
    // Call the portfolio loader with the supplied arguments
    copier.copy(positionReader, positionWriter);
    // close stuff
    positionReader.close();
    positionWriter.close();
  }

  private Iterable<? extends PositionReader> returnPorfolioReader(InputStream fileStream) {
    return new XmlFileReader(fileStream, new SchemaRegister());
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
    }

    Response response = Response.status(Response.Status.BAD_REQUEST).entity("Portfolio upload only supports CSV/XLS" +
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
