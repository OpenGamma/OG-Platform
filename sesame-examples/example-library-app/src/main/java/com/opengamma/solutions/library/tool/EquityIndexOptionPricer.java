/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

import java.io.IOException;
import java.util.HashMap;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.util.EquityIndexOptionViewUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Sample Equity Index Option pricing method
 */
public class EquityIndexOptionPricer {

  private final Engine _engine;

  /**
   * Create an instance of the Equity Index Option Pricer
   * @param engine the calculation engine.
   */
  @Inject
  public EquityIndexOptionPricer(Engine engine) {
    _engine = ArgumentChecker.notNull(engine, "engine");
  }

  /**
   * Equity Index Option price function
   * @param valuationTime ZoneDateTime valuation time
   * @param trades the path to the input trades file
   * @param discountingCurves the path to the discounting curve file
   * @param forwardCurves the path to the forward curve file
   * @param volatilitySurfaces the path to the volatility surface file
   * @return Results containing PV for an Equity Index Option
   */
  public Results price(ZonedDateTime valuationTime,
                       String trades,
                       String discountingCurves,
                       String forwardCurves,
                       String volatilitySurfaces) throws IOException {

    CalculationArguments calculationArguments = CalculationArguments.builder().valuationTime(valuationTime).build();
    HashMap<Object, String> portfolio = EquityIndexOptionViewUtils.parsePortfolio(trades);
    ViewConfig viewConfig = EquityIndexOptionViewUtils.createViewConfig(portfolio.values());

    MarketDataEnvironmentBuilder marketData = new MarketDataEnvironmentBuilder();
    EquityIndexOptionViewUtils.parseDiscountingCurves(marketData, discountingCurves);
    EquityIndexOptionViewUtils.parseForwardCurves(marketData, forwardCurves);
    EquityIndexOptionViewUtils.parseVolatilitySurfaces(marketData, volatilitySurfaces);
    marketData.valuationTime(valuationTime);

    return _engine.runView(viewConfig, calculationArguments, marketData.build(), Lists.newArrayList(portfolio.keySet()));
  }

}
