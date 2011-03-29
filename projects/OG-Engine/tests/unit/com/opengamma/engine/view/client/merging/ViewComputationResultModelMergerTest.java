/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * Tests ViewComputationResultModelMerger
 */
public class ViewComputationResultModelMergerTest {

  @Test
  public void testMerger() {
    ReplacementMerger merger = new ReplacementMerger();
    assertEquals(null, merger.consume());
    
    ViewComputationResultModel result1 = mock(ViewComputationResultModel.class);
    merger.merge(result1);
    assertEquals(result1, merger.consume());
    assertEquals(result1, merger.consume());
    
    merger.merge(mock(ViewComputationResultModel.class));
    ViewComputationResultModel result2 = mock(ViewComputationResultModel.class);
    merger.merge(result2);
    assertEquals(result2, merger.consume());
    assertEquals(result2, merger.consume());
  }
  
}