/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ViewDeltaResultModelImplTest {
  
  public void test() {
    InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    ViewComputationResultModelImplTest.checkModel(model);
    
    model.setPreviousCalculationTime(Instant.ofEpochMilli(200));
    assertEquals(Instant.ofEpochMilli(200), model.getPreviousResultTimestamp());
  }

}
