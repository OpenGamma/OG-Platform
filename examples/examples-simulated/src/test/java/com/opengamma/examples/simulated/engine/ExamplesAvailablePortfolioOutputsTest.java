/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.engine;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailablePortfolioOutputs;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class ExamplesAvailablePortfolioOutputsTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ExamplesAvailablePortfolioOutputsTest.class);

  private static final String SWAP_PORTFOLIO = "Swap Portfolio";
  private static final String MIXED_EXAMPLE_PORTFOLIO = "Equity Option Portfolio";

  private ComponentRepository _repo;
  private CompiledFunctionRepository _functionRepository;
  private FunctionExclusionGroups _functionExclusionGroups;
  private MarketDataAvailabilityFilter _marketDataAvailability;
  private PortfolioMaster _portfolioMaster;
  private PositionSource _positionSource;
  private SecuritySource _securitySource;
  private ExecutorService _executorService;

  @BeforeClass
  public void initialise() {
    final ComponentManager manager = new ComponentManager("test");
    manager.start("classpath:/fullstack/fullstack-examplessimulated-test.properties");
    _repo = manager.getRepository();
    final CompiledFunctionService cfs = _repo.getInstance(CompiledFunctionService.class, "main");
    _functionRepository = cfs.compileFunctionRepository(Instant.now());
    _functionExclusionGroups = _repo.getInstance(FunctionExclusionGroups.class, "main");
    _portfolioMaster = _repo.getInstance(PortfolioMaster.class, "central");
    _positionSource = _repo.getInstance(PositionSource.class, "combined");
    _securitySource = _repo.getInstance(SecuritySource.class, "combined");
    _marketDataAvailability = new OptimisticMarketDataAvailabilityFilter();
    _executorService = Executors.newCachedThreadPool();
  }

  @AfterClass
  public void cleanup() {
    if (_repo != null) {
      _repo.stop();
    }
  }

  private Portfolio getPortfolio(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    searchRequest.setIncludePositions(false);
    final PortfolioSearchResult searchResult = _portfolioMaster.search(searchRequest);
    assertNotNull(searchResult.getFirstDocument());
    // Master doesn't return a Portfolio (a ManageablePortfolio), so use the position source
    return _positionSource.getPortfolio(searchResult.getFirstDocument().getUniqueId(), VersionCorrection.LATEST);
  }

  private AvailableOutputs testPortfolio(final String portfolioName) {
    final long t1 = System.nanoTime();
    Portfolio portfolio = getPortfolio(portfolioName);
    final long t2 = System.nanoTime();
    portfolio = PortfolioCompiler.resolvePortfolio(portfolio, _executorService, _securitySource);
    final long t3 = System.nanoTime();
    final AvailableOutputs outputs = new AvailablePortfolioOutputs(portfolio, _functionRepository, _functionExclusionGroups, _marketDataAvailability, "?");
    final long t4 = System.nanoTime();
    s_logger.info("Fetch={}ms, Resolve={}ms, Outputs={}ms", new Object[] {(t2 - t1) / 1e6, (t3 - t2) / 1e6, (t4 - t3) / 1e6 });
    s_logger.info("Outputs for {}", portfolio.getName());
    for (final AvailableOutput output : outputs.getOutputs()) {
      s_logger.info("{}", output);
    }
    s_logger.info("Portfolio node outputs for {}", portfolio.getName());
    for (final AvailableOutput output : outputs.getPortfolioNodeOutputs()) {
      s_logger.info("{}", output);
    }
    s_logger.info("Position outputs for {}", portfolio.getName());
    for (final AvailableOutput output : outputs.getPositionOutputs()) {
      s_logger.info("{}", output);
    }
    for (final String securityType : outputs.getSecurityTypes()) {
      s_logger.info("{} security outputs for {}", securityType, portfolio.getName());
      for (final AvailableOutput output : outputs.getPositionOutputs(securityType)) {
        s_logger.info("{}", output);
      }
    }
    System.err.println(portfolioName);
    for (String securityType : outputs.getSecurityTypes()) {
      final Set<String> values = new HashSet<String>();
      for (AvailableOutput output : outputs.getPositionOutputs(securityType)) {
        if (values.add(output.getValueName())) {
          System.err.println("\tassertPositionOutput(outputs, \"" + securityType + "\", \"" + output.getValueName() + "\");");
        }
      }
    }
    return outputs;
  }

  private static void assertPositionOutput(final AvailableOutputs outputs, final String securityType, final String valueName) {
    for (final AvailableOutput output : outputs.getPositionOutputs(securityType)) {
      if (valueName.equals(output.getValueName())) {
        return;
      }
    }
    fail(valueName + " not available for " + securityType);
  }

  @Test
  public void testSwapPortfolio() {
    assertSwapPortfolioOutputs(testPortfolio(SWAP_PORTFOLIO));
  }

  private static void assertSwapPortfolioOutputs(final AvailableOutputs outputs) {
    assertPositionOutput(outputs, "SWAP", "Type");
    assertPositionOutput(outputs, "SWAP", "Netted Fixed Cash-Flows");
    assertPositionOutput(outputs, "SWAP", "Swap Receive Leg Details");
    assertPositionOutput(outputs, "SWAP", "Swap Pay Leg Details");
    assertPositionOutput(outputs, "SWAP", "Quantity");
    assertPositionOutput(outputs, "SWAP", "Receive Fixed Cash-Flows");
    assertPositionOutput(outputs, "SWAP", "Product");
    assertPositionOutput(outputs, "SWAP", "Rate");
    assertPositionOutput(outputs, "SWAP", "Receive Floating Cash-Flows");
    assertPositionOutput(outputs, "SWAP", "Market Yield To Maturity");
    assertPositionOutput(outputs, "SWAP", "Par Rate Curve Sensitivity");
    assertPositionOutput(outputs, "SWAP", "Pay/Receive");
    assertPositionOutput(outputs, "SWAP", "Value");
    assertPositionOutput(outputs, "SWAP", "Start Date");
    assertPositionOutput(outputs, "SWAP", "P&L Series");
    assertPositionOutput(outputs, "SWAP", "Target");
    assertPositionOutput(outputs, "SWAP", "Market Dirty Price");
    assertPositionOutput(outputs, "SWAP", "Swap Receive Leg Present Value");
    assertPositionOutput(outputs, "SWAP", "Index");
    assertPositionOutput(outputs, "SWAP", "Market Clean Price");
    assertPositionOutput(outputs, "SWAP", "Par Rate");
    assertPositionOutput(outputs, "SWAP", "Par Rate Parallel Shift Sensitivity");
    assertPositionOutput(outputs, "SWAP", "Pay Floating Cash-Flows");
    assertPositionOutput(outputs, "SWAP", "Pay Fixed Cash-Flows");
    assertPositionOutput(outputs, "SWAP", "ISIN");
    assertPositionOutput(outputs, "SWAP", "Swap Pay Leg Present Value");
    assertPositionOutput(outputs, "SWAP", "Yield Curve Node Sensitivities");
    assertPositionOutput(outputs, "SWAP", "DV01");
    assertPositionOutput(outputs, "SWAP", "Bucketed PV01");
    assertPositionOutput(outputs, "SWAP", "ExternalId");
    assertPositionOutput(outputs, "SWAP", "Float Frequency");
    assertPositionOutput(outputs, "SWAP", "Maturity Date");
    assertPositionOutput(outputs, "SWAP", "PV01");
    assertPositionOutput(outputs, "SWAP", "Present Value");
    assertPositionOutput(outputs, "SWAP", "Frequency");
  }

  @Test
  public void testMixedExamplePortfolio() {
    assertMixedExamplePortfolioOutputs(testPortfolio(MIXED_EXAMPLE_PORTFOLIO));
  }

  private static void assertMixedExamplePortfolioOutputs(final AvailableOutputs outputs) {
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Alpha p-Values");
    assertPositionOutput(outputs, "EQUITY", "Value");
    assertPositionOutput(outputs, "EQUITY", "Maturity Date");
    assertPositionOutput(outputs, "EQUITY", "Index");
    assertPositionOutput(outputs, "EQUITY", "HistoricalVaR");
    assertPositionOutput(outputs, "EQUITY", "Mark-to-Market P&L");
    assertPositionOutput(outputs, "EQUITY", "Daily PnL");
    assertPositionOutput(outputs, "EQUITY", "FairValue");
    assertPositionOutput(outputs, "EQUITY", "ExternalId");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Beta Standard Error");
    assertPositionOutput(outputs, "EQUITY", "HistoricalCVaR");
    assertPositionOutput(outputs, "EQUITY", "HistoricalVaR Standard Deviation");
    assertPositionOutput(outputs, "EQUITY", "Product");
    assertPositionOutput(outputs, "EQUITY", "ISIN");
    assertPositionOutput(outputs, "EQUITY", "Sharpe Ratio");
    assertPositionOutput(outputs, "EQUITY", "Start Date");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Alpha Standard Error");
    assertPositionOutput(outputs, "EQUITY", "Float Frequency");
    assertPositionOutput(outputs, "EQUITY", "Market Yield To Maturity");
    assertPositionOutput(outputs, "EQUITY", "ValueDelta");
    assertPositionOutput(outputs, "EQUITY", "Total Risk Alpha");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Beta Residual");
    assertPositionOutput(outputs, "EQUITY", "Pay/Receive");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Beta");
    assertPositionOutput(outputs, "EQUITY", "Present Value");
    assertPositionOutput(outputs, "EQUITY", "Target");
    assertPositionOutput(outputs, "EQUITY", "Market Clean Price");
    assertPositionOutput(outputs, "EQUITY", "Type");
    assertPositionOutput(outputs, "EQUITY", "Rate");
    assertPositionOutput(outputs, "EQUITY", "PnL");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Alpha");
    assertPositionOutput(outputs, "EQUITY", "Delta");
    assertPositionOutput(outputs, "EQUITY", "Forward Price");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Alpha Residual");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Adjusted R-Squared");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Beta p-Values");
    assertPositionOutput(outputs, "EQUITY", "Treynor Ratio");
    assertPositionOutput(outputs, "EQUITY", "Underlying Market Price");
    assertPositionOutput(outputs, "EQUITY", "Quantity");
    assertPositionOutput(outputs, "EQUITY", "Security Market Price");
    assertPositionOutput(outputs, "EQUITY", "P&L Series");
    assertPositionOutput(outputs, "EQUITY", "Frequency");
    assertPositionOutput(outputs, "EQUITY", "Jensen's Alpha");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Alpha t-Stats");
    assertPositionOutput(outputs, "EQUITY", "CAPM Beta");
    assertPositionOutput(outputs, "EQUITY", "Market Dirty Price");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression R-Squared");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Beta t-Stats");
    assertPositionOutput(outputs, "EQUITY", "CAPM Regression Mean Square Error");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VarianceUltima");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Start Date");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VegaBleed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Weighted Vega");
    assertPositionOutput(outputs, "EQUITY_OPTION", "GammaPBleed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ValueSpeed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Quantity");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Type");
    assertPositionOutput(outputs, "EQUITY_OPTION", "GammaBleed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "DriftlessTheta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Rate");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Total Risk Alpha");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Alpha Residual");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Beta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Beta p-Values");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Alpha Standard Error");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Elasticity");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Security Market Price");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Sharpe Ratio");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Alpha p-Values");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Value");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ISIN");
    assertPositionOutput(outputs, "EQUITY_OPTION", "HistoricalVaR");
    assertPositionOutput(outputs, "EQUITY_OPTION", "FairValue");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Frequency");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Pay/Receive");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Index");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VarianceVanna");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Market Dirty Price");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ZommaP");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VegaP");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Vomma");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Gamma");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Market Yield To Maturity");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Maturity Date");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Float Frequency");
    assertPositionOutput(outputs, "EQUITY_OPTION", "dVanna_dVol");
    assertPositionOutput(outputs, "EQUITY_OPTION", "HistoricalVaR Standard Deviation");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Product");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Treynor Ratio");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ValueDelta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Speed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Theta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Beta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "SpeedP");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ValueVega");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ValueGamma");
    assertPositionOutput(outputs, "EQUITY_OPTION", "HistoricalCVaR");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VarianceVega");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Vega");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Beta t-Stats");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VommaP");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Market Clean Price");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ExternalId");
    assertPositionOutput(outputs, "EQUITY_OPTION", "VarianceVomma");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Beta Standard Error");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ZetaBleed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Zomma");
    assertPositionOutput(outputs, "EQUITY_OPTION", "DeltaBleed");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Alpha t-Stats");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Mark-to-Market P&L");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Forward Price");
    assertPositionOutput(outputs, "EQUITY_OPTION", "dZeta_dVol");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Mean Square Error");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Beta Residual");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Phi");
    assertPositionOutput(outputs, "EQUITY_OPTION", "ValueTheta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Ultima");
    assertPositionOutput(outputs, "EQUITY_OPTION", "StrikeGamma");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Delta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Adjusted R-Squared");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Underlying Market Price");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression R-Squared");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Zeta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "P&L Series");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Vanna");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CarryRho");
    assertPositionOutput(outputs, "EQUITY_OPTION", "CAPM Regression Alpha");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Rho");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Jensen's Alpha");
    assertPositionOutput(outputs, "EQUITY_OPTION", "Target");
    assertPositionOutput(outputs, "EQUITY_OPTION", "StrikeDelta");
    assertPositionOutput(outputs, "EQUITY_OPTION", "GammaP");
  }
}
