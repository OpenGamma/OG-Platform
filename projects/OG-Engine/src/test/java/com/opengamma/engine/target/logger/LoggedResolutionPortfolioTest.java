/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LoggedResolutionPortfolio} class.
 */
@Test(groups = TestGroup.UNIT)
public class LoggedResolutionPortfolioTest {

  @SuppressWarnings("unchecked")
  public void getAttributes() {
    final Portfolio portfolio = Mockito.mock(Portfolio.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Portfolio logged = new LoggedResolutionPortfolio(portfolio, logger);
    final Map<String, String> result = Mockito.mock(Map.class);
    Mockito.when(portfolio.getAttributes()).thenReturn(result);
    assertSame(logged.getAttributes(), result);
    Mockito.verifyZeroInteractions(logger);
  }

  @SuppressWarnings("unchecked")
  public void setAttributes() {
    final Portfolio portfolio = Mockito.mock(Portfolio.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Portfolio logged = new LoggedResolutionPortfolio(portfolio, logger);
    final Map<String, String> param = Mockito.mock(Map.class);
    logged.setAttributes(param);
    Mockito.verify(portfolio).setAttributes(param);
    Mockito.verifyZeroInteractions(logger);
  }

  public void addAttribute() {
    final Portfolio portfolio = Mockito.mock(Portfolio.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Portfolio logged = new LoggedResolutionPortfolio(portfolio, logger);
    logged.addAttribute("Foo", "Bar");
    Mockito.verify(portfolio).addAttribute("Foo", "Bar");
    Mockito.verifyZeroInteractions(logger);
  }

  public void getRootNode() {
    final Portfolio portfolio = Mockito.mock(Portfolio.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Portfolio logged = new LoggedResolutionPortfolio(portfolio, logger);
    final PortfolioNode root = Mockito.mock(PortfolioNode.class);
    Mockito.when(root.getUniqueId()).thenReturn(UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.when(portfolio.getRootNode()).thenReturn(root);
    final PortfolioNode returnedRoot = logged.getRootNode();
    assertTrue(returnedRoot instanceof LoggedResolutionPortfolioNode);
    assertEquals(returnedRoot.getUniqueId(), UniqueId.of("Foo", "Bar", "Cow"));
    //Mockito.verify(logger).log(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Foo", "Bar")), UniqueId.of("Foo", "Bar", "Cow"));
    Mockito.verifyNoMoreInteractions(logger);
  }

  public void getName() {
    final Portfolio portfolio = Mockito.mock(Portfolio.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Portfolio logged = new LoggedResolutionPortfolio(portfolio, logger);
    Mockito.when(portfolio.getName()).thenReturn("Foo");
    assertEquals(logged.getName(), "Foo");
    Mockito.verifyZeroInteractions(logger);
  }

}
