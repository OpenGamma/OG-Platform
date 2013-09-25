/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.marketdata.manipulator.dsl.RemoteServer;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 *
 */
/* package */ class DataWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(DataWriter.class);

  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final PortfolioMaster _portfolioMaster;
  private final ConfigMaster _configMaster;
  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final File _outputDir;

  /* package */ DataWriter(File outputDir,
                           SecurityMaster securityMaster,
                           PositionMaster positionMaster,
                           PortfolioMaster portfolioMaster,
                           ConfigMaster configMaster,
                           HistoricalTimeSeriesMaster timeSeriesMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(outputDir, "outputDir");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    _timeSeriesMaster = timeSeriesMaster;
    _positionMaster = positionMaster;
    _portfolioMaster = portfolioMaster;
    _configMaster = configMaster;
    _outputDir = outputDir;
    _securityMaster = securityMaster;
    if (!outputDir.exists() || !outputDir.isDirectory()) {
      // TODO allow it to be non-existent and create it
      throw new IllegalArgumentException("Output directory " + outputDir + " must be an existing directory");
    }
  }

  public static void main(String[] args) throws IOException {

    try {
      RemoteServer server = RemoteServer.create("http://localhost:8080");
      DataWriter dataWriter = new DataWriter(new File("/Users/chris/tmp/regression"),
                                             server.getSecurityMaster(),
                                             server.getPositionMaster(),
                                             server.getPortfolioMaster(),
                                             server.getConfigMaster(),
                                             server.getHistoricalTimeSeriesMaster());
      //dataWriter.writeSecurities();
      //dataWriter.writePositions();
      //dataWriter.writePortfolios();
      //dataWriter.writeConfig();
      dataWriter.writeTimeSeries();
    } catch (Exception e) {
      s_logger.warn("Failed to write data", e);
    }
    System.exit(0);
  }

  private void writeSecurities() throws IOException {
    SecuritySearchResult result = _securityMaster.search(new SecuritySearchRequest());
    writeToFile(result.getSecurities(), "securities.xml");
  }

  private void writePositions() throws IOException {
    PositionSearchResult result = _positionMaster.search(new PositionSearchRequest());
    writeToFile(result.getPositions(), "positions.xml");
  }

  private void writeConfig() throws IOException {
    ConfigSearchResult<Object> result = _configMaster.search(new ConfigSearchRequest<>(Object.class));
    writeToFile(result.getValues(), "configs.xml");
  }

  private void writePortfolios() throws IOException {
    PortfolioSearchResult result = _portfolioMaster.search(new PortfolioSearchRequest());
    writeToFile(result.getPortfolios(), "portfolios.xml");
  }

  private void writeTimeSeries() throws IOException {
    List<Object> objects = Lists.newArrayList();
    HistoricalTimeSeriesInfoSearchResult infoResult = _timeSeriesMaster.search(new HistoricalTimeSeriesInfoSearchRequest());
    for (ManageableHistoricalTimeSeriesInfo info : infoResult.getInfoList()) {
      objects.add(_timeSeriesMaster.getTimeSeries(info.getTimeSeriesObjectId(), VersionCorrection.LATEST));
      objects.add(info);
    }
    writeToFile(objects, "timeseries.xml");
  }

  private void writeToFile(List<?> objects, String outputFileName) throws IOException {
    File outputFile = new File(_outputDir, outputFileName);
    s_logger.info("Writing to {}", outputFile.getAbsolutePath());
    // TODO wrap in a root element so it's a valid XML document?
    try (FileWriter writer = new FileWriter(outputFile)) {
      FudgeContext ctx = OpenGammaFudgeContext.getInstance();
      FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(ctx, writer);
      FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter);
      FudgeSerializer serializer = new FudgeSerializer(ctx);
      for (Object object : objects) {
        // TODO need to write class
        MutableFudgeMsg msg = serializer.objectToFudgeMsg(object);
        FudgeSerializer.addClassHeader(msg, object.getClass());
        fudgeMsgWriter.writeMessage(msg);
        writer.append("\n");
        s_logger.debug("Wrote object {}", object);
      }
      s_logger.info("Wrote {} objects to {}", objects.size(), outputFile.getAbsolutePath());
      fudgeMsgWriter.flush();
    }
  }
}
