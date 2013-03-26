/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ViewResultModelMergerTest {

  private static final String CONFIG_1 = "config1";
  private static final String CONFIG_2 = "config2";

  public void testDeltaMerger() {
    final ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    assertNull(merger.getLatestResult());

    final InMemoryViewDeltaResultModel deltaResult1 = new InMemoryViewDeltaResultModel();
    deltaResult1.addValue(CONFIG_1, getComputedValueResult("value1", 1));
    deltaResult1.addValue(CONFIG_1, getComputedValueResult("value2", 2));
    merger.merge(deltaResult1);
    assertResultsEqual(deltaResult1, merger.getLatestResult());

    final InMemoryViewDeltaResultModel deltaResult2 = new InMemoryViewDeltaResultModel();
    deltaResult2.addValue(CONFIG_1, getComputedValueResult("value1", 3));

    merger.merge(deltaResult1);
    merger.merge(deltaResult2);

    InMemoryViewDeltaResultModel expectedMergedResult = new InMemoryViewDeltaResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value1", 3));
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value2", 2));

    assertResultsEqual(expectedMergedResult, merger.getLatestResult());

    final InMemoryViewDeltaResultModel deltaResult3 = new InMemoryViewDeltaResultModel();
    deltaResult3.addValue(CONFIG_2, getComputedValueResult("value3", 4));

    merger.merge(deltaResult1);
    merger.merge(deltaResult3);

    expectedMergedResult = new InMemoryViewDeltaResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value1", 1));
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value2", 2));
    expectedMergedResult.addValue(CONFIG_2, getComputedValueResult("value3", 4));

    assertResultsEqual(expectedMergedResult, merger.getLatestResult());
  }

  public void testDeltaMergerHandlesPartiallyEmptyModels() {
    final ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    InMemoryViewDeltaResultModel deltaResult = new InMemoryViewDeltaResultModel();
    merger.merge(deltaResult);

    deltaResult = new InMemoryViewDeltaResultModel();
    merger.merge(deltaResult);

    deltaResult = new InMemoryViewDeltaResultModel();
    // Tests coping with expanding calculation configurations (e.g. if a new one has been added between computation
    // cycles)
    deltaResult.addValue(CONFIG_1, getComputedValueResult("value1", 1));
    merger.merge(deltaResult);
  }

  public void testDeltaMergerPassesThroughEmptyDelta() {
    final ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    final InMemoryViewDeltaResultModel deltaResult = new InMemoryViewDeltaResultModel();
    merger.merge(deltaResult);
    assertNotNull(merger.getLatestResult());
  }

  //-------------------------------------------------------------------------
  public void testFullMerger() {
    final ViewComputationResultModelMerger merger = new ViewComputationResultModelMerger();
    assertNull(merger.getLatestResult());

    final InMemoryViewComputationResultModel result1 = new InMemoryViewComputationResultModel();
    result1.addValue(CONFIG_1, getComputedValueResult("value1", 1));
    result1.addValue(CONFIG_1, getComputedValueResult("value2", 2));
    result1.addMarketData(getComputedValueResult("vod", 250));
    merger.merge(result1);
    assertResultsEqual(result1, merger.getLatestResult());

    final InMemoryViewComputationResultModel result2 = new InMemoryViewComputationResultModel();
    result2.addValue(CONFIG_1, getComputedValueResult("value1", 3));
    result2.addMarketData(getComputedValueResult("aapl", 400));
    merger.merge(result2);

    InMemoryViewComputationResultModel expectedMergedResult = new InMemoryViewComputationResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value1", 3));
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value2", 2));
    expectedMergedResult.addMarketData(getComputedValueResult("vod", 250));
    expectedMergedResult.addMarketData(getComputedValueResult("aapl", 400));

    assertResultsEqual(expectedMergedResult, merger.getLatestResult());

    final InMemoryViewComputationResultModel result3 = new InMemoryViewComputationResultModel();
    result3.addValue(CONFIG_2, getComputedValueResult("value3", 4));
    result3.addMarketData(getComputedValueResult("vod", 300));

    merger.merge(result1);
    merger.merge(result3);

    expectedMergedResult = new InMemoryViewComputationResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value1", 1));
    expectedMergedResult.addValue(CONFIG_1, getComputedValueResult("value2", 2));
    expectedMergedResult.addValue(CONFIG_2, getComputedValueResult("value3", 4));
    result3.addMarketData(getComputedValueResult("vod", 300));

    assertResultsEqual(expectedMergedResult, merger.getLatestResult());
  }

  //-------------------------------------------------------------------------
  private ComputedValueResult getComputedValueResult(final String valueName, final Object value) {
    final UniqueId uniqueId = UniqueId.of("Scheme", valueName);
    return new ComputedValueResult(new ValueSpecification(valueName, ComputationTargetSpecification.of(uniqueId),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "FunctionId").get()), value, AggregatedExecutionLog.EMPTY);
  }

  private void assertResultsEqual(final ViewResultModel expected, final ViewResultModel actual) {
    assertEquals(expected.getAllTargets(), actual.getAllTargets());
    assertEquals(expected.getCalculationConfigurationNames(), actual.getCalculationConfigurationNames());

    for (final String calcConfigName : expected.getCalculationConfigurationNames()) {
      final ViewCalculationResultModel expectedCalcResult = expected.getCalculationResult(calcConfigName);
      final ViewCalculationResultModel actualCalcResult = actual.getCalculationResult(calcConfigName);
      for (final ComputationTargetSpecification targetSpec : expected.getAllTargets()) {
        final Map<Pair<String, ValueProperties>, ComputedValueResult> expectedTargetValues = expectedCalcResult.getValues(targetSpec);
        final Map<Pair<String, ValueProperties>, ComputedValueResult> actualTargetValues = actualCalcResult.getValues(targetSpec);
        assertEquals(expectedTargetValues, actualTargetValues);
      }
    }
  }

}
