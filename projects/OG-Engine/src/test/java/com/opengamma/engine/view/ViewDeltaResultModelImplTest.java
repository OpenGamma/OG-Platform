/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.Instant;

import org.testng.annotations.Test;

/**
 * 
 */
@Test
public class ViewDeltaResultModelImplTest {
  
  public void test() {
    InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    ViewComputationResultModelImplTest.checkModel(model);
    
    model.setPreviousCalculationTime(Instant.ofEpochMillis(200));
    assertEquals(Instant.ofEpochMillis(200), model.getPreviousResultTimestamp());
  }

}
