/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.MissingOutput;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ResultsCacheTest {

  private static final String CALC_CONFIG = "calcConfig";

  private final ComputationTargetSpecification _target1 = ComputationTargetSpecification.of(UniqueId.of("fake", "id1"));
  private final ComputationTargetSpecification _target2 = ComputationTargetSpecification.of(UniqueId.of("fake", "id2"));
  private final ValueRequirement _req1 = new ValueRequirement("req1", _target1);
  private final ValueRequirement _req2 = new ValueRequirement("req2", _target2);
  private final ValueSpecification _spec1 = new ValueSpecification(_req1.getValueName(), _target1, ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "fn1").get());
  private final ValueSpecification _spec2 = new ValueSpecification(_req2.getValueName(), _target2, ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "fn2").get());

  @Test
  public void putResultsNoHistory() {
    final String spec1value1 = "spec1value1";
    final String spec2value1 = "spec2value1";

    final InMemoryViewComputationResultModel results1 = new InMemoryViewComputationResultModel();
    results1.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    results1.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, spec1value1, AggregatedExecutionLog.EMPTY));
    results1.addValue(CALC_CONFIG, new ComputedValueResult(_spec2, spec2value1, AggregatedExecutionLog.EMPTY));

    final ResultsCache cache = new ResultsCache();
    cache.put(results1);

    final ResultsCache.Result result1_1 = cache.getResult(CALC_CONFIG, _spec1, String.class);
    final ResultsCache.Result result2_1 = cache.getResult(CALC_CONFIG, _spec2, String.class);
    assertEquals(spec1value1, result1_1.getValue());
    assertEquals(spec2value1, result2_1.getValue());
    assertNull(result1_1.getHistory());
    assertNull(result2_1.getHistory());
    assertTrue(result1_1.isUpdated());
    assertTrue(result2_1.isUpdated());

    final String spec1value2 = "spec1value2";
    final InMemoryViewComputationResultModel results2 = new InMemoryViewComputationResultModel();
    results2.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    results2.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, spec1value2, AggregatedExecutionLog.EMPTY));
    cache.put(results2);

    final ResultsCache.Result result1_2 = cache.getResult(CALC_CONFIG, _spec1, String.class);
    final ResultsCache.Result result2_2 = cache.getResult(CALC_CONFIG, _spec2, String.class);
    assertEquals(spec1value2, result1_2.getValue());
    assertEquals(spec2value1, result2_2.getValue());
    assertNull(result1_2.getHistory());
    assertNull(result2_2.getHistory());
    assertTrue(result1_2.isUpdated());
    assertFalse(result2_2.isUpdated());
  }

  @Test
  public void putResultsWithHistory() {
    final InMemoryViewComputationResultModel results1 = new InMemoryViewComputationResultModel();
    results1.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    results1.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, 1d, AggregatedExecutionLog.EMPTY));
    final ResultsCache cache = new ResultsCache();
    cache.put(results1);

    final ResultsCache.Result result1 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(1d, result1.getValue());
    assertEquals(1, result1.getHistory().size());
    assertEquals(1d, result1.getHistory().iterator().next());

    final InMemoryViewComputationResultModel results2 = new InMemoryViewComputationResultModel();
    results2.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    results2.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, 2d, AggregatedExecutionLog.EMPTY));
    cache.put(results2);

    final ResultsCache.Result result2 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(2d, result2.getValue());
    assertEquals(2, result2.getHistory().size());
    final List<Object> history = new ArrayList<Object>(result2.getHistory());
    assertEquals(1d, history.get(0));
    assertEquals(2d, history.get(1));
  }

  @Test
  public void errorValues() {
    final InMemoryViewComputationResultModel resultsModel1 = new InMemoryViewComputationResultModel();
    resultsModel1.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    resultsModel1.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, MissingOutput.EVALUATION_ERROR, AggregatedExecutionLog.EMPTY));
    final ResultsCache cache = new ResultsCache();
    cache.put(resultsModel1);

    final ResultsCache.Result result1 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(MissingOutput.EVALUATION_ERROR, result1.getValue());
    assertNull(result1.getHistory());

    final InMemoryViewComputationResultModel resultsModel2 = new InMemoryViewComputationResultModel();
    resultsModel2.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    resultsModel2.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, 1d, AggregatedExecutionLog.EMPTY));
    cache.put(resultsModel2);

    final InMemoryViewComputationResultModel resultsModel3 = new InMemoryViewComputationResultModel();
    resultsModel3.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).create());
    resultsModel3.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, MissingOutput.EVALUATION_ERROR, AggregatedExecutionLog.EMPTY));
    cache.put(resultsModel3);

    final ResultsCache.Result result2 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(MissingOutput.EVALUATION_ERROR, result2.getValue());
    final List<Object> history = Lists.newArrayList(result2.getHistory());
    assertNotNull(history);
    assertEquals(2, history.size());
    assertEquals(1d, history.get(0));
    assertEquals(MissingOutput.EVALUATION_ERROR, history.get(1));
  }
}
