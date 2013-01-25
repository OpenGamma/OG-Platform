/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

/**
 * 
 */
@Test
public class ViewDeltaResultModelImplTest {
  
  public void test() {
    InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    ViewComputationResultModelImplTest.checkModel(model);
    
    model.setPreviousCalculationTime(Instant.ofEpochMilli(200));
    assertEquals(Instant.ofEpochMilli(200), model.getPreviousResultTimestamp());
  }

}
