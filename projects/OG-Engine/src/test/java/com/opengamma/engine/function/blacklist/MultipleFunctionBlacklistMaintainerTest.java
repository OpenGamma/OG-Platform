/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MultipleFunctionBlacklistMaintainer} class.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleFunctionBlacklistMaintainerTest {

  private final CalculationJobItem _item1 = new CalculationJobItem("F1", new EmptyFunctionParameters(), ComputationTargetSpecification.NULL,
      Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS);
  private final CalculationJobItem _item2 = new CalculationJobItem("F2", new EmptyFunctionParameters(), ComputationTargetSpecification.NULL,
      Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS);

  public void testNone() {
    final MultipleFunctionBlacklistMaintainer m = new MultipleFunctionBlacklistMaintainer(Collections.<FunctionBlacklistMaintainer>emptySet());
    m.failedJobItem(_item1);
    m.failedJobItems(Arrays.asList(_item1, _item2));
  }

  public void testOne() {
    final FunctionBlacklistMaintainer u = Mockito.mock(FunctionBlacklistMaintainer.class);
    final MultipleFunctionBlacklistMaintainer m = new MultipleFunctionBlacklistMaintainer(Collections.singleton(u));
    m.failedJobItem(_item1);
    Mockito.verify(u).failedJobItem(_item1);
    m.failedJobItems(Arrays.asList(_item1, _item2));
    Mockito.verify(u).failedJobItems(Arrays.asList(_item1, _item2));
  }

  public void testTwo() {
    final FunctionBlacklistMaintainer u1 = Mockito.mock(FunctionBlacklistMaintainer.class);
    final FunctionBlacklistMaintainer u2 = Mockito.mock(FunctionBlacklistMaintainer.class);
    final MultipleFunctionBlacklistMaintainer m = new MultipleFunctionBlacklistMaintainer(Arrays.asList(u1, u2));
    m.failedJobItem(_item1);
    Mockito.verify(u1).failedJobItem(_item1);
    Mockito.verify(u2).failedJobItem(_item1);
    m.failedJobItems(Arrays.asList(_item1, _item2));
    Mockito.verify(u1).failedJobItems(Arrays.asList(_item1, _item2));
    Mockito.verify(u2).failedJobItems(Arrays.asList(_item1, _item2));
  }

}
