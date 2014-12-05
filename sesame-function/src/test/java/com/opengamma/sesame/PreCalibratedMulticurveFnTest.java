/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.sesame.function.scenarios.FilteredScenarioDefinition;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.sources.BondMockSources;
import com.opengamma.sesame.trade.BondTrade;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

@Test(groups = TestGroup.UNIT)
public class PreCalibratedMulticurveFnTest {

  private static final Environment ENV = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class));
  private static final TradeWrapper<?> TRADE = createTrade();

  private static TradeWrapper<?> createTrade() {
    SimpleTrade trade = new SimpleTrade();
    trade.setQuantity(BigDecimal.ONE);
    trade.setTradeDate(LocalDate.of(2011, 3, 8));
    SimpleSecurityLink securityLink = new SimpleSecurityLink();
    securityLink.setTarget(BondMockSources.GOVERNMENT_BOND_SECURITY);
    trade.setSecurityLink(securityLink);
    return new BondTrade(trade);
  }

  @Test
  public void noCurvesForTrade() {
    CurveSelector curveSelector = mock(CurveSelector.class);
    when(curveSelector.getMulticurveNames(TRADE.getTrade())).thenReturn(Collections.<String>emptySet());
    PreCalibratedMulticurveFn fn = new PreCalibratedMulticurveFn(curveSelector);
    assertFalse(fn.getMulticurveBundle(ENV, TRADE).isSuccess());
  }

  @Test
  public void wrongNumberOfScenarioArgs() {
    Map<String, MulticurveBundle> emptyCurves = Collections.emptyMap();
    PreCalibratedMulticurveArguments args1 = new PreCalibratedMulticurveArguments(emptyCurves);
    PreCalibratedMulticurveArguments args2 = new PreCalibratedMulticurveArguments(emptyCurves);
    FilteredScenarioDefinition scenarioDefinition = new FilteredScenarioDefinition(args1, args2);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class), scenarioDefinition);
    Set<String> curveNames = ImmutableSet.of("a curve");
    CurveSelector curveSelector = mock(CurveSelector.class);
    when(curveSelector.getMulticurveNames(TRADE.getTrade())).thenReturn(curveNames);

    PreCalibratedMulticurveFn fn = new PreCalibratedMulticurveFn(curveSelector);
    assertFalse(fn.getMulticurveBundle(env, TRADE).isSuccess());
  }

  @Test
  public void expectedCurveNotAvailable() {
    Map<String, MulticurveBundle> curves = new HashMap<>();
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> linkedMap = new LinkedHashMap<>();
    MulticurveBundle multicurveBundle = new MulticurveBundle(new MulticurveProviderDiscount(),
                                                             new CurveBuildingBlockBundle(linkedMap));
    curves.put("a curve", multicurveBundle);
    PreCalibratedMulticurveArguments args = new PreCalibratedMulticurveArguments(curves);
    FilteredScenarioDefinition scenarioDefinition = new FilteredScenarioDefinition(args);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class), scenarioDefinition);
    Set<String> curveNames = ImmutableSet.of("a different curve");
    CurveSelector curveSelector = mock(CurveSelector.class);
    when(curveSelector.getMulticurveNames(TRADE.getTrade())).thenReturn(curveNames);

    PreCalibratedMulticurveFn fn = new PreCalibratedMulticurveFn(curveSelector);
    assertFalse(fn.getMulticurveBundle(env, TRADE).isSuccess());
  }

  @Test
  public void singleCurveFound() {
    Map<String, MulticurveBundle> curves = new HashMap<>();
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> linkedMap = new LinkedHashMap<>();
    MulticurveBundle multicurveBundle = new MulticurveBundle(new MulticurveProviderDiscount(),
                                                             new CurveBuildingBlockBundle(linkedMap));
    curves.put("a curve", multicurveBundle);
    PreCalibratedMulticurveArguments args = new PreCalibratedMulticurveArguments(curves);
    FilteredScenarioDefinition scenarioDefinition = new FilteredScenarioDefinition(args);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class), scenarioDefinition);
    Set<String> curveNames = ImmutableSet.of("a curve");
    CurveSelector curveSelector = mock(CurveSelector.class);
    when(curveSelector.getMulticurveNames(TRADE.getTrade())).thenReturn(curveNames);

    PreCalibratedMulticurveFn fn = new PreCalibratedMulticurveFn(curveSelector);
    assertTrue(fn.getMulticurveBundle(env, TRADE).isSuccess());
  }

  @Test
  public void multipleCurvesFound() {
    Map<String, MulticurveBundle> curves = new HashMap<>();
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> linkedMap1 = new LinkedHashMap<>();
    MulticurveBundle multicurveBundle1 = new MulticurveBundle(new MulticurveProviderDiscount(),
                                                              new CurveBuildingBlockBundle(linkedMap1));
    curves.put("curve1", multicurveBundle1);
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> linkedMap2 = new LinkedHashMap<>();
    MulticurveBundle multicurveBundle2 = new MulticurveBundle(new MulticurveProviderDiscount(),
                                                              new CurveBuildingBlockBundle(linkedMap2));
    curves.put("curve2", multicurveBundle2);

    PreCalibratedMulticurveArguments args = new PreCalibratedMulticurveArguments(curves);
    FilteredScenarioDefinition scenarioDefinition = new FilteredScenarioDefinition(args);
    SimpleEnvironment env = new SimpleEnvironment(ZonedDateTime.now(), mock(MarketDataBundle.class), scenarioDefinition);
    Set<String> curveNames = ImmutableSet.of("curve1", "curve2");
    CurveSelector curveSelector = mock(CurveSelector.class);
    when(curveSelector.getMulticurveNames(TRADE.getTrade())).thenReturn(curveNames);

    PreCalibratedMulticurveFn fn = new PreCalibratedMulticurveFn(curveSelector);
    Result<MulticurveBundle> result = fn.getMulticurveBundle(env, TRADE);
    assertTrue(result.isSuccess());
  }
}
