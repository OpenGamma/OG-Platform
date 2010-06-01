/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class ViewDeltaResultModelImplTest {
  
  @Test
  public void test() {
    ViewDeltaResultModelImpl model = new ViewDeltaResultModelImpl();
    ViewComputationResultModelImplTest.checkModel(model);
    
    model.setPreviousResultTimestamp(200);
    assertEquals(200, model.getPreviousResultTimestamp());
  }

}
