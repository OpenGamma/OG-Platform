/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataDuplicationException;
import com.opengamma.examples.marketdata.SimulatedHistoricalDataGenerator;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Creates random historical data for a randomly created security.
 */
/* package */class RandomHistoricalData {

  private static final Logger s_logger = LoggerFactory.getLogger(RandomHistoricalData.class);

  public static void createPriceSeries(final AbstractPortfolioGeneratorTool portfolioGenerator, final ManageableSecurity security, final double lastPrice) {
    final HistoricalTimeSeriesMaster htsMaster = portfolioGenerator.getToolContext().getHistoricalTimeSeriesMaster();
    final ExternalIdBundle identifiers = portfolioGenerator.getSecurityPersister().storeSecurity(security);
    final ExternalId identifier = identifiers.getExternalId(portfolioGenerator.getSecurityPersister().getScheme());
    final Random random = portfolioGenerator.getRandom();
    final ManageableHistoricalTimeSeriesInfo info = SimulatedHistoricalDataGenerator.getSimulatedTimeSeriesInfo("CLOSE", identifier);
    try {
      final HistoricalTimeSeriesInfoDocument addedDoc = htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      final LocalDateDoubleTimeSeries timeSeries = SimulatedHistoricalDataGenerator.getHistoricalDataPoints(random, lastPrice, 2);
      htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    } catch (DataDuplicationException e) {
      s_logger.warn("Couldn't add time series for {}", security);
    }
  }

}
