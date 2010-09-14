/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class RiskRewardCrossTest {
  private static final double RISK_FREE_RETURN = 0.03;
  private static final double ASSET_RETURN = 0.174;
  private static final double MARKET_RETURN = 0.11;
  private static final double ASSET_STANDARD_DEVIATION = 0.24;
  private static final double MARKET_STANDARD_DEVIATION = 0.17;
  private static final double BETA = 1.3;
  private static final TotalRiskAlphaCalculator TRA = new TotalRiskAlphaCalculator();
  private static final SharpeRatioCalculator SHARPE = new SharpeRatioCalculator();
  private static final RiskAdjustedPerformanceCalculator RAP = new RiskAdjustedPerformanceCalculator();
  private static final MSquaredPerformanceCalculator M2 = new MSquaredPerformanceCalculator();
  private static final TreynorRatioCalculator TREYNOR = new TreynorRatioCalculator();
  private static final JensenAlphaCalculator JENSEN = new JensenAlphaCalculator();
  private static final MarketRiskAdjustedPerformanceCalculator MRAP = new MarketRiskAdjustedPerformanceCalculator();
  private static final TSquaredPerformanceCalculator T2 = new TSquaredPerformanceCalculator();
  private static final double EPS = 1e-15;

  @Test
  public void testTRAAndSharpeRatio() {
    final double tra = TRA.calculate(ASSET_RETURN, RISK_FREE_RETURN, MARKET_RETURN, ASSET_STANDARD_DEVIATION, MARKET_STANDARD_DEVIATION);
    final double srAsset = SHARPE.calculate(ASSET_RETURN, RISK_FREE_RETURN, ASSET_STANDARD_DEVIATION);
    final double srMarket = SHARPE.calculate(MARKET_RETURN, RISK_FREE_RETURN, MARKET_STANDARD_DEVIATION);
    assertEquals(tra, ASSET_STANDARD_DEVIATION * (srAsset - srMarket), EPS);
  }

  @Test
  public void testRAPAndSharpeRatio() {
    final double rap = RAP.calculate(ASSET_RETURN, RISK_FREE_RETURN, ASSET_STANDARD_DEVIATION, MARKET_STANDARD_DEVIATION);
    final double srAsset = SHARPE.calculate(ASSET_RETURN, RISK_FREE_RETURN, ASSET_STANDARD_DEVIATION);
    assertEquals(rap, MARKET_STANDARD_DEVIATION * srAsset + RISK_FREE_RETURN, EPS);
  }

  @Test
  public void testM2TRAAndSharpeRatio() {
    final double tra = TRA.calculate(ASSET_RETURN, RISK_FREE_RETURN, MARKET_RETURN, ASSET_STANDARD_DEVIATION, MARKET_STANDARD_DEVIATION);
    final double srAsset = SHARPE.calculate(ASSET_RETURN, RISK_FREE_RETURN, ASSET_STANDARD_DEVIATION);
    final double srMarket = SHARPE.calculate(MARKET_RETURN, RISK_FREE_RETURN, MARKET_STANDARD_DEVIATION);
    final double m2 = M2.calculate(ASSET_RETURN, RISK_FREE_RETURN, MARKET_RETURN, ASSET_STANDARD_DEVIATION, MARKET_STANDARD_DEVIATION);
    assertEquals(m2, tra * MARKET_STANDARD_DEVIATION / ASSET_STANDARD_DEVIATION, EPS);
    assertEquals(m2, MARKET_STANDARD_DEVIATION * (srAsset - srMarket), EPS);
  }

  @Test
  public void testJensenAndTreynor() {
    final double ja = JENSEN.calculate(ASSET_RETURN, RISK_FREE_RETURN, BETA, MARKET_RETURN);
    final double trAsset = TREYNOR.calculate(ASSET_RETURN, RISK_FREE_RETURN, BETA);
    final double trMarket = TREYNOR.calculate(MARKET_RETURN, RISK_FREE_RETURN, 1);
    assertEquals(trAsset, ja / BETA + trMarket, EPS);
  }

  @Test
  public void testMRAPAndTreynor() {
    final double mrap = MRAP.calculate(ASSET_RETURN, RISK_FREE_RETURN, BETA);
    final double trAsset = TREYNOR.calculate(ASSET_RETURN, RISK_FREE_RETURN, BETA);
    assertEquals(mrap, trAsset + RISK_FREE_RETURN, EPS);
  }

  @Test
  public void testT2AndJensen() {
    final double ja = JENSEN.calculate(ASSET_RETURN, RISK_FREE_RETURN, BETA, MARKET_RETURN);
    final double t2 = T2.calculate(ASSET_RETURN, RISK_FREE_RETURN, MARKET_RETURN, BETA);
    assertEquals(t2, ja / BETA, EPS);
  }
}
