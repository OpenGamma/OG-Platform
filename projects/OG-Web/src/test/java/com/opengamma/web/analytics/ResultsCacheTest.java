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

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;
import com.opengamma.id.UniqueId;

public class ResultsCacheTest {

  private static final String CALC_CONFIG = "calcConfig";

  private final ValueRequirement _req1 = new ValueRequirement("req1", ComputationTargetType.POSITION, UniqueId.of("fake", "id1"));
  private final ValueRequirement _req2 = new ValueRequirement("req2", ComputationTargetType.POSITION, UniqueId.of("fake", "id2"));
  private final ValueSpecification _spec1 = new ValueSpecification(_req1, "fn1");
  private final ValueSpecification _spec2 = new ValueSpecification(_req2, "fn2");

  @Test
  public void putResultsNoHistory() {
    String spec1value1 = "spec1value1";
    String spec2value1 = "spec2value1";

    InMemoryViewComputationResultModel results1 = new InMemoryViewComputationResultModel();
    results1.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, spec1value1, ExecutionLog.EMPTY));
    results1.addValue(CALC_CONFIG, new ComputedValueResult(_spec2, spec2value1, ExecutionLog.EMPTY));

    ResultsCache cache = new ResultsCache();
    cache.put(results1);

    ResultsCache.Result result1_1 = cache.getResult(CALC_CONFIG, _spec1, String.class);
    ResultsCache.Result result2_1 = cache.getResult(CALC_CONFIG, _spec2, String.class);
    assertEquals(spec1value1, result1_1.getValue());
    assertEquals(spec2value1, result2_1.getValue());
    assertNull(result1_1.getHistory());
    assertNull(result2_1.getHistory());
    assertTrue(result1_1.isUpdated());
    assertTrue(result2_1.isUpdated());

    String spec1value2 = "spec1value2";
    InMemoryViewComputationResultModel results2 = new InMemoryViewComputationResultModel();
    results2.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, spec1value2, ExecutionLog.EMPTY));
    cache.put(results2);

    ResultsCache.Result result1_2 = cache.getResult(CALC_CONFIG, _spec1, String.class);
    ResultsCache.Result result2_2 = cache.getResult(CALC_CONFIG, _spec2, String.class);
    assertEquals(spec1value2, result1_2.getValue());
    assertEquals(spec2value1, result2_2.getValue());
    assertNull(result1_2.getHistory());
    assertNull(result2_2.getHistory());
    assertTrue(result1_2.isUpdated());
    assertFalse(result2_2.isUpdated());
  }

  @Test
  public void putResultsWithHistory() {
    InMemoryViewComputationResultModel results1 = new InMemoryViewComputationResultModel();
    results1.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, 1d, ExecutionLog.EMPTY));
    ResultsCache cache = new ResultsCache();
    cache.put(results1);

    ResultsCache.Result result1 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(1d, result1.getValue());
    assertEquals(1, result1.getHistory().size());
    assertEquals(1d, result1.getHistory().iterator().next());

    InMemoryViewComputationResultModel results2 = new InMemoryViewComputationResultModel();
    results2.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, 2d, ExecutionLog.EMPTY));
    cache.put(results2);

    ResultsCache.Result result2 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(2d, result2.getValue());
    assertEquals(2, result2.getHistory().size());
    List<Object> history = new ArrayList<Object>(result2.getHistory());
    assertEquals(1d, history.get(0));
    assertEquals(2d, history.get(1));
  }

  @Test
  public void errorValues() {
    InMemoryViewComputationResultModel resultsModel1 = new InMemoryViewComputationResultModel();
    resultsModel1.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, NotCalculatedSentinel.EVALUATION_ERROR, ExecutionLog.EMPTY));
    ResultsCache cache = new ResultsCache();
    cache.put(resultsModel1);

    ResultsCache.Result result1 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertNull(result1.getValue());
    assertNotNull(result1.getHistory());
    assertEquals(0, result1.getHistory().size());

    InMemoryViewComputationResultModel resultsModel2 = new InMemoryViewComputationResultModel();
    resultsModel2.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, 1d, ExecutionLog.EMPTY));
    cache.put(resultsModel2);

    InMemoryViewComputationResultModel resultsModel3 = new InMemoryViewComputationResultModel();
    resultsModel3.addValue(CALC_CONFIG, new ComputedValueResult(_spec1, NotCalculatedSentinel.EVALUATION_ERROR, ExecutionLog.EMPTY));
    cache.put(resultsModel3);

    ResultsCache.Result result2 = cache.getResult(CALC_CONFIG, _spec1, Double.class);
    assertEquals(NotCalculatedSentinel.EVALUATION_ERROR, result2.getValue());
    List<Object> history = Lists.newArrayList(result2.getHistory());
    assertNotNull(history);
    assertEquals(2, history.size());
    assertEquals(1d, history.get(0));
    assertEquals(NotCalculatedSentinel.EVALUATION_ERROR, history.get(1));
  }
}
