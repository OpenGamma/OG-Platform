/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class CalculationJobSpecificationTest {
  
  @Test
  public void testHashCode() {
    CalculationJobSpecification spec1 = new CalculationJobSpecification("view", "config", 1L, 1L);
    CalculationJobSpecification spec2 = new CalculationJobSpecification("view", "config", 1L, 1L);
    
    assertEquals(spec1.hashCode(), spec2.hashCode());
    
    spec2 = new CalculationJobSpecification("view2", "config", 1L, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification("view", "config2", 1L, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification("view", "config", 2L, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification("view", "config", 1L, 2L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
  }

  @Test
  public void testEquals() {
    CalculationJobSpecification spec1 = new CalculationJobSpecification("view", "config", 1L, 1L);
    assertTrue(spec1.equals(spec1));
    assertFalse(spec1.equals(null));
    assertFalse(spec1.equals("Kirk"));
    CalculationJobSpecification spec2 = new CalculationJobSpecification("view", "config", 1L, 1L);
    assertTrue(spec1.equals(spec2));
    
    spec2 = new CalculationJobSpecification("view2", "config", 1L, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification("view", "config2", 1L, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification("view", "config", 2L, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification("view", "config", 1L, 2L);
    assertFalse(spec1.equals(spec2));
  }

}
