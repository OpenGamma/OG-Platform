/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Source of random, but reasonable, equity option security instances.
 */
public class EquityOptionSecurityGenerator extends com.opengamma.financial.generator.EquityOptionSecurityGenerator {

  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionSecurityGenerator.class);

  private final AbstractPortfolioGeneratorTool _tool;

  public EquityOptionSecurityGenerator(final AbstractPortfolioGeneratorTool tool, final EquitySecurity underlying) {
    super(underlying);
    ArgumentChecker.notNull(tool, "tool");
    _tool = tool;
  }

  protected AbstractPortfolioGeneratorTool getTool() {
    return _tool;
  }

  @Override
  public EquityOptionSecurity createSecurity() {
    final EquityOptionSecurity security = super.createSecurity();
    if (security != null) {
      final Pair<LocalDate, Double> value = getTool().getToolContext().getHistoricalTimeSeriesSource()
          .getLatestDataPoint(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId().toBundle(), null);
      if (value != null) {
        RandomHistoricalData.createPriceSeries(getTool(), security, value.getSecond());
      } else {
        s_logger.debug("No equity price series for underlying {}", security.getUnderlyingId());
      }
    }
    return security;
  }

}
