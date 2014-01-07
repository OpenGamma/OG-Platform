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
public class CycleFragmentCompletedCallTest extends AbstractCompletedResultsCallTest {

  @Override
  protected CycleFragmentCompletedCall create(final ViewComputationResultModel full, final ViewDeltaResultModel delta) {
    return new CycleFragmentCompletedCall(full, delta);
  }

  @Override
  public void testInitialValues() {
    super.testInitialValues();
    final ViewComputationResultModel full = new InMemoryViewComputationResultModel();
    final ViewDeltaResultModel delta = new InMemoryViewDeltaResultModel();
    CycleFragmentCompletedCall instance = create(full, delta);
    assertSame(instance.getFullFragment(), full);
    assertSame(instance.getDeltaFragment(), delta);
  }

  public void testApply() {
    final ViewComputationResultModel full = new InMemoryViewComputationResultModel();
    final ViewDeltaResultModel delta = new InMemoryViewDeltaResultModel();
    CycleFragmentCompletedCall instance = create(full, delta);
    final ViewResultListener mock = Mockito.mock(ViewResultListener.class);
    assertNull(instance.apply(mock));
    Mockito.verify(mock, Mockito.only()).cycleFragmentCompleted(full, delta);
  }

}
