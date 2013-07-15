/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.math.BigDecimal;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.MutableExecutionLog;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ViewCalculationResultModelImplTest {

  public static final Position POSITION = new SimplePosition(UniqueId.of("PositionIdentifier", "testPosition"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  public static final ComputationTargetSpecification SPEC = ComputationTargetSpecification.of(POSITION);
  public static final ComputedValueResult COMPUTED_VALUE_RESULT;
  public static final SimplePortfolio PORTFOLIO;
  public static final SimplePortfolioNode PORTFOLIO_ROOT_NODE;

  static {
    PORTFOLIO = new SimplePortfolio("testportfolio");
    PORTFOLIO_ROOT_NODE = new SimplePortfolioNode(UniqueId.of("PortfolioIdentifier", "rootNode"), "rootNode");
    PORTFOLIO.setRootNode(PORTFOLIO_ROOT_NODE);
    PORTFOLIO_ROOT_NODE.addPosition(POSITION);

    final ExecutionLog executionLog = MutableExecutionLog.single(new SimpleLogEvent(LogLevel.INFO, "test"), ExecutionLogMode.FULL);
    final AggregatedExecutionLog aggregatedExecutionLog = DefaultAggregatedExecutionLog.indicatorLogMode(executionLog.getLogLevels());
    COMPUTED_VALUE_RESULT = new ComputedValueResult(new ValueSpecification("DATA", SPEC, ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId")
        .get()), "12345", aggregatedExecutionLog);
  }

  public void addValue() {
    final InMemoryViewResultModel resultModel = new InMemoryViewResultModel() {
      private static final long serialVersionUID = 1L;
    };
    ViewCalculationResultModelImpl calcResult = resultModel.getCalculationResultModelImpl("Default");
    assertNull(calcResult);

    resultModel.addValue("Default", COMPUTED_VALUE_RESULT);
    resultModel.addValue("Default", COMPUTED_VALUE_RESULT);

    calcResult = resultModel.getCalculationResultModelImpl("Default");
    assertNotNull(calcResult);
    final Map<Pair<String, ValueProperties>, ComputedValueResult> targetResults = calcResult.getValues(SPEC);
    assertEquals(1, targetResults.size());
    assertEquals("DATA", targetResults.keySet().iterator().next().getFirst());
    assertEquals(COMPUTED_VALUE_RESULT, targetResults.values().iterator().next());
    assertEquals(Sets.newHashSet(SPEC), Sets.newHashSet(calcResult.getAllTargets()));

    assertNull(calcResult.getValues(ComputationTargetSpecification.of(UniqueId.of("Test", "nonexistent"))));
  }

}
