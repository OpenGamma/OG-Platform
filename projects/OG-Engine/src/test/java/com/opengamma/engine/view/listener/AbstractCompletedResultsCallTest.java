/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AbstractCompletedResultsCall} class.
 */
@Test(groups = TestGroup.UNIT)
public abstract class AbstractCompletedResultsCallTest {

  protected abstract AbstractCompletedResultsCall create(ViewComputationResultModel full, ViewDeltaResultModel delta);

  public void testInitialValues() {
    final ViewComputationResultModel full = new InMemoryViewComputationResultModel();
    final ViewDeltaResultModel delta = new InMemoryViewDeltaResultModel();
    AbstractCompletedResultsCall instance = create(null, null);
    assertNull(instance.getViewComputationResultModel());
    assertNull(instance.getViewDeltaResultModel());
    instance = create(full, null);
    assertSame(instance.getViewComputationResultModel(), full);
    assertNull(instance.getViewDeltaResultModel());
    instance = create(null, delta);
    assertNull(instance.getViewComputationResultModel());
    assertSame(instance.getViewDeltaResultModel(), delta);
    instance = create(full, delta);
    assertSame(instance.getViewComputationResultModel(), full);
    assertSame(instance.getViewDeltaResultModel(), delta);
  }

  private ValueSpecification value(final int i) {
    return new ValueSpecification(Integer.toString(i), ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get());
  }

  private ViewComputationResultModel createFull1(final Instant now) {
    final InMemoryViewComputationResultModel model = new InMemoryViewComputationResultModel();
    model.setCalculationTime(now);
    model.addMarketData(new ComputedValue(value(1), "A"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(10), "A"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(2), "A"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(20), "A"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(3), "A"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(30), "A"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private ViewComputationResultModel createFull2a(final Instant now) {
    final InMemoryViewComputationResultModel model = new InMemoryViewComputationResultModel();
    model.setCalculationTime(now.plusSeconds(1L));
    model.addMarketData(new ComputedValue(value(1), "A"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(10), "A"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(2), "B"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(20), "B"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(3), "A"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(30), "A"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private ViewComputationResultModel createFull2b(final Instant now) {
    final InMemoryViewComputationResultModel model = new InMemoryViewComputationResultModel();
    model.setCalculationTime(now.plusSeconds(1L));
    model.addMarketData(new ComputedValue(value(1), "B"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(10), "B"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(2), "B"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(20), "B"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(3), "B"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(30), "B"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private ViewComputationResultModel createFull3(final Instant now) {
    final InMemoryViewComputationResultModel model = new InMemoryViewComputationResultModel();
    model.setCalculationTime(now.plusSeconds(2L));
    model.addMarketData(new ComputedValue(value(1), "B"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(10), "B"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(2), "C"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(20), "C"), AggregatedExecutionLog.EMPTY));
    model.addMarketData(new ComputedValue(value(3), "B"));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(30), "B"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private ViewDeltaResultModel createDelta2a(final Instant now) {
    final InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    model.setCalculationTime(now.plusSeconds(1L));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(20), "B"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private ViewDeltaResultModel createDelta2b(final Instant now) {
    final InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    model.setCalculationTime(now.plusSeconds(1L));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(10), "B"), AggregatedExecutionLog.EMPTY));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(30), "B"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private ViewDeltaResultModel createDelta3(final Instant now) {
    final InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    model.setCalculationTime(now.plusSeconds(2L));
    model.addValue("Default", new ComputedValueResult(new ComputedValue(value(20), "C"), AggregatedExecutionLog.EMPTY));
    return model;
  }

  private Set<ComputedValue> values(final ViewResultModel model) {
    final Set<ComputedValue> values = new HashSet<ComputedValue>();
    for (ComputedValueResult result : model.getTargetResult(ComputationTargetSpecification.NULL).getAllValues("Default")) {
      values.add(new ComputedValue(result.getSpecification(), result.getValue()));
    }
    return values;
  }

  public void testNormalOrder() {
    final Instant now = Instant.now();
    final AbstractCompletedResultsCall instance = create(createFull1(now), null);
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "A"), new ComputedValue(value(2), "A"), new ComputedValue(value(3), "A")));
    assertEquals(values(instance.getViewComputationResultModel()), ImmutableSet.of(new ComputedValue(value(10), "A"), new ComputedValue(value(20), "A"), new ComputedValue(value(30), "A")));
    instance.update(createFull2a(now), createDelta2a(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "A"), new ComputedValue(value(2), "B"), new ComputedValue(value(3), "A")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(20), "B")));
    instance.update(createFull2b(now), createDelta2b(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "B"), new ComputedValue(value(2), "B"), new ComputedValue(value(3), "B")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "B"), new ComputedValue(value(30), "B")));
    instance.update(createFull3(now), createDelta3(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "B"), new ComputedValue(value(2), "C"), new ComputedValue(value(3), "B")));
    assertEquals(values(instance.getViewComputationResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "C"), new ComputedValue(value(30), "B")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "C"), new ComputedValue(value(30), "B")));
  }

  public void testOutOfOrderResult1() {
    final Instant now = Instant.now();
    final AbstractCompletedResultsCall instance = create(createFull1(now), null);
    instance.update(createFull2b(now), createDelta2b(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "B"), new ComputedValue(value(2), "B"), new ComputedValue(value(3), "B")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(30), "B")));
    instance.update(createFull2a(now), createDelta2a(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "A"), new ComputedValue(value(2), "B"), new ComputedValue(value(3), "A")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "B"), new ComputedValue(value(30), "B")));
    instance.update(createFull3(now), createDelta3(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "B"), new ComputedValue(value(2), "C"), new ComputedValue(value(3), "B")));
    assertEquals(values(instance.getViewComputationResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "C"), new ComputedValue(value(30), "B")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "C"), new ComputedValue(value(30), "B")));
  }

  public void testOutOfOrderResult2() {
    final Instant now = Instant.now();
    final AbstractCompletedResultsCall instance = create(createFull1(now), null);
    instance.update(createFull2b(now), createDelta2b(now));
    instance.update(createFull3(now), createDelta3(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "B"), new ComputedValue(value(2), "C"), new ComputedValue(value(3), "B")));
    assertEquals(values(instance.getViewDeltaResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "C"), new ComputedValue(value(30), "B")));
    instance.update(createFull2a(now), createDelta2a(now));
    assertEquals(instance.getViewComputationResultModel().getAllMarketData(), ImmutableSet.of(new ComputedValue(value(1), "B"), new ComputedValue(value(2), "C"), new ComputedValue(value(3), "B")));
    assertEquals(values(instance.getViewComputationResultModel()), ImmutableSet.of(new ComputedValue(value(10), "B"), new ComputedValue(value(20), "C"), new ComputedValue(value(30), "B")));
  }

}
