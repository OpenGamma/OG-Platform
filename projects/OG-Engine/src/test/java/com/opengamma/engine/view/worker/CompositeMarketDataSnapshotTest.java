/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.worker.CompositeMarketDataSnapshot;
import com.opengamma.engine.view.worker.SnapshottingViewExecutionDataProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CompositeMarketDataSnapshotTest {

  private static final ComputationTargetSpecification TARGET = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("scheme", "value"));
  private static final ValueRequirement REQUIREMENT1 = new ValueRequirement("r1", TARGET);
  private static final ValueRequirement REQUIREMENT2 = new ValueRequirement("r2", TARGET);
  private static final ValueSpecification SPECIFICATION1 = new ValueSpecification(REQUIREMENT1.getValueName(), TARGET, ValueProperties.with(ValuePropertyNames.FUNCTION, "F").get());
  private static final ValueSpecification SPECIFICATION1_EXT = new ValueSpecification(REQUIREMENT1.getValueName(), TARGET, ValueProperties.with(ValuePropertyNames.FUNCTION, "F")
      .with(ValuePropertyNames.DATA_PROVIDER, "0").get());
  private static final ValueSpecification SPECIFICATION2 = new ValueSpecification(REQUIREMENT2.getValueName(), TARGET, ValueProperties.with(ValuePropertyNames.FUNCTION, "F").get());
  private static final ValueSpecification SPECIFICATION2_EXT = new ValueSpecification(REQUIREMENT2.getValueName(), TARGET, ValueProperties.with(ValuePropertyNames.FUNCTION, "F")
      .with(ValuePropertyNames.DATA_PROVIDER, "1").get());
  private static final Object VALUE1 = "V1";
  private static final Object VALUE2 = "V2";
  private static final ValueSpecification UNKNOWN_SPECIFICATION = new ValueSpecification("u", TARGET, ValueProperties.with(ValuePropertyNames.FUNCTION, "F").get());

  private MarketDataSnapshot _delegate1;
  private MarketDataSnapshot _delegate2;
  private CompositeMarketDataSnapshot _snapshot;

  @BeforeMethod
  public void setUp() throws Exception {
    _delegate1 = mock(MarketDataSnapshot.class);
    _delegate2 = mock(MarketDataSnapshot.class);
    _snapshot = new CompositeMarketDataSnapshot(Lists.newArrayList(_delegate1, _delegate2), new SnapshottingViewExecutionDataProvider.ValueSpecificationProvider(2));
    stub(_delegate1.query(SPECIFICATION1)).toReturn(VALUE1);
    stub(_delegate2.query(SPECIFICATION2)).toReturn(VALUE2);
    stub(_delegate1.query(Sets.newHashSet(SPECIFICATION1))).toReturn(ImmutableMap.of(SPECIFICATION1, VALUE1));
    stub(_delegate2.query(Sets.newHashSet(SPECIFICATION2))).toReturn(ImmutableMap.of(SPECIFICATION2, VALUE2));
    stub(_delegate1.getSnapshotTime()).toReturn(null);
    stub(_delegate2.getSnapshotTime()).toReturn(Instant.now());
    stub(_delegate1.getSnapshotTimeIndication()).toReturn(null);
    stub(_delegate2.getSnapshotTimeIndication()).toReturn(Instant.now());
  }

  @Test
  public void init() {
    // check all delegates are initialized
    _snapshot.init();
    verify(_delegate1).init();
    verify(_delegate2).init();
  }

  /**
   * initialize the snapshot with a set of requirements that is a subset of the requirements in the underlying snapshots
   */
  @SuppressWarnings("unchecked")
  @Test
  public void initMultiSubset() {
    // check all delegates are initialized with the appropriate subset of requirements
    final Set<ValueSpecification> specs = Sets.newHashSet(SPECIFICATION1_EXT, UNKNOWN_SPECIFICATION);
    _snapshot.init(specs, 0, TimeUnit.MILLISECONDS);
    verify(_delegate1).init(Sets.newHashSet(SPECIFICATION1), 0, TimeUnit.MILLISECONDS);
    verify(_delegate1, never()).init();
    verify(_delegate2).init();
    verify(_delegate2, never()).init(anySet(), anyLong(), (TimeUnit) anyObject());
  }

  /**
   * initialize the snapshot with a set of requirements that includes all the requirements in the underlying snapshots
   */
  @Test
  public void initMultiAll() {
    // check all delegates are initialized with the appropriate subset of requirements
    final Set<ValueSpecification> specs = Sets.newHashSet(SPECIFICATION1_EXT, SPECIFICATION2_EXT, UNKNOWN_SPECIFICATION);
    _snapshot.init(specs, 0, TimeUnit.MILLISECONDS);
    verify(_delegate1).init(Sets.newHashSet(SPECIFICATION1), 0, TimeUnit.MILLISECONDS);
    verify(_delegate2).init(Sets.newHashSet(SPECIFICATION2), 0, TimeUnit.MILLISECONDS);
  }

  @Test
  public void queryOne() {
    // check a requirement is found from
    // * the first delegate
    // * another delegate
    // check null is returned for an unknown requirement
    assertEquals(VALUE1, _snapshot.query(SPECIFICATION1_EXT));
    assertEquals(VALUE2, _snapshot.query(SPECIFICATION2_EXT));
    assertNull(_snapshot.query(UNKNOWN_SPECIFICATION));
  }

  /**
   * query the snapshot with a set of requirements that is a subset of the requirements in the underlying snapshots
   */
  @Test
  public void queryMultiSubset() {
    final Set<ValueSpecification> specs = Sets.newHashSet(SPECIFICATION1_EXT, UNKNOWN_SPECIFICATION);
    final Map<ValueSpecification, Object> result = _snapshot.query(specs);
    assertEquals(VALUE1, result.get(SPECIFICATION1_EXT));
    assertNull(result.get(SPECIFICATION2_EXT));
    assertNull(result.get(UNKNOWN_SPECIFICATION));
  }

  /**
   * query the snapshot with a set of requirements that includes all the requirements in the underlying snapshots
   */
  @Test
  public void queryMultiAll() {
    final Set<ValueSpecification> specs = Sets.newHashSet(SPECIFICATION1_EXT, SPECIFICATION2_EXT, UNKNOWN_SPECIFICATION);
    final Map<ValueSpecification, Object> result = _snapshot.query(specs);
    assertEquals(VALUE1, result.get(SPECIFICATION1_EXT));
    assertEquals(VALUE2, result.get(SPECIFICATION2_EXT));
    assertNull(result.get(UNKNOWN_SPECIFICATION));
  }

  /**
   * If the first underlying provider doesn't have a snapshot time then the others should be tried
   */
  @Test
  public void snapshotTime() {
    assertNotNull(_snapshot.getSnapshotTime());
  }

  /**
   * If the first underlying provider doesn't have a snapshot time indication then the others should be tried
   */
  @Test
  public void snapshotTimeIndication() {
    assertNotNull(_snapshot.getSnapshotTimeIndication());
  }

}
