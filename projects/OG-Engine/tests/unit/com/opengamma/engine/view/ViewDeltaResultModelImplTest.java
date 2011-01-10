/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;

import javax.time.Instant;

import org.junit.Test;

/**
 * 
 */
public class ViewDeltaResultModelImplTest {
  
  @Test
  public void test() {
    InMemoryViewDeltaResultModel model = new InMemoryViewDeltaResultModel();
    ViewComputationResultModelImplTest.checkModel(model);
    
    model.setPreviousResultTimestamp(Instant.ofEpochMillis(200));
    assertEquals(Instant.ofEpochMillis(200), model.getPreviousResultTimestamp());
  }

}
