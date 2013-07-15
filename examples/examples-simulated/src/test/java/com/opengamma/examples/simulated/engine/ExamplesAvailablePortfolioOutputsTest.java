/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.engine;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

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

  private static final String SWAP_PORTFOLIO = "MultiCurrency Swap Portfolio";
  private static final String MIXED_EXAMPLE_PORTFOLIO = "Swap / Swaption Portfolio";

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
    assertPositionOutput(outputs, "SWAP", "Par Rate");
    assertPositionOutput(outputs, "SWAP", "Par Rate Curve Sensitivity");
    assertPositionOutput(outputs, "SWAP", "Par Rate Parallel Shift Sensitivity");
    assertPositionOutput(outputs, "SWAP", "P&L Series");
    assertPositionOutput(outputs, "SWAP", "Present Value");
    assertPositionOutput(outputs, "SWAP", "PV01");
    assertPositionOutput(outputs, "SWAP", "Value");
    assertPositionOutput(outputs, "SWAP", "Yield Curve Node Sensitivities");
  }

  @Test
  public void testMixedExamplePortfolio() {
    assertMixedExamplePortfolioOutputs(testPortfolio(MIXED_EXAMPLE_PORTFOLIO));
  }

  private static void assertMixedExamplePortfolioOutputs(final AvailableOutputs outputs) {
    assertPositionOutput(outputs, "SWAP", "Par Rate");
    assertPositionOutput(outputs, "SWAP", "Par Rate Curve Sensitivity");
    assertPositionOutput(outputs, "SWAP", "Par Rate Parallel Shift Sensitivity");
    assertPositionOutput(outputs, "SWAP", "Present Value");
    assertPositionOutput(outputs, "SWAP", "PV01");
    assertPositionOutput(outputs, "SWAP", "Value");
    assertPositionOutput(outputs, "SWAP", "Yield Curve Node Sensitivities");
    assertPositionOutput(outputs, "SWAPTION", "Present Value");
    assertPositionOutput(outputs, "SWAPTION", "Present Value Curve Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Present Value SABR Alpha Node Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Present Value SABR Alpha Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Present Value SABR Nu Node Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Present Value SABR Nu Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Present Value SABR Rho Node Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Present Value SABR Rho Sensitivity");
    assertPositionOutput(outputs, "SWAPTION", "Value");
    assertPositionOutput(outputs, "SWAPTION", "Vega Quote Cube");
    assertPositionOutput(outputs, "SWAPTION", "Yield Curve Node Sensitivities");
  }

}
