/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link CycleCompletedCall} class.
 */
@Test(groups = TestGroup.UNIT)
public class CycleCompletedCallTest extends AbstractCompletedResultsCallTest {

  @Override
  protected CycleCompletedCall create(final ViewComputationResultModel full, final ViewDeltaResultModel delta) {
    return new CycleCompletedCall(full, delta);
  }

  @Override
  public void testInitialValues() {
    super.testInitialValues();
    final ViewComputationResultModel full = new InMemoryViewComputationResultModel();
    final ViewDeltaResultModel delta = new InMemoryViewDeltaResultModel();
    CycleCompletedCall instance = create(full, delta);
    assertSame(instance.getFullResult(), full);
    assertSame(instance.getDeltaResult(), delta);
  }

  public void testApply() {
    final ViewComputationResultModel full = new InMemoryViewComputationResultModel();
    final ViewDeltaResultModel delta = new InMemoryViewDeltaResultModel();
    CycleCompletedCall instance = create(full, delta);
    final ViewResultListener mock = Mockito.mock(ViewResultListener.class);
    assertNull(instance.apply(mock));
    Mockito.verify(mock, Mockito.only()).cycleCompleted(full, delta);
  }

}
