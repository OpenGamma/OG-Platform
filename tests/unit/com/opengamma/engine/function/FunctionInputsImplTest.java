/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

public class FunctionInputsImplTest {
  private static final ValueSpecification SPEC1 = new ValueSpecification(new ValueRequirement("foo-1", "USD"), "mockFunctionId");
  private static final ValueSpecification SPEC2 = new ValueSpecification(new ValueRequirement("foo-2", "USD"), "mockFunctionId");
  private static final ComputedValue VALUE1 = new ComputedValue(SPEC1, "1");
  private static final ComputedValue VALUE2 = new ComputedValue(SPEC2, "2");

  @Test(expected=IllegalArgumentException.class)
  public void nullValue() {
    (new FunctionInputsImpl()).addValue(null);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void cyclicalValue() {
    (new FunctionInputsImpl()).addValue(new ComputedValue(SPEC1, new ComputedValue(SPEC1, "")));
  }
  
  @Test
  public void getAll() {
    FunctionInputsImpl inputs = new FunctionInputsImpl(VALUE1);
    inputs.addValue(VALUE2);

    Collection<ComputedValue> values = inputs.getAllValues();
    assertNotNull(values);
    assertEquals(2, values.size());
    assertTrue(values.contains(VALUE1));
    assertTrue(values.contains(VALUE2));
  }
  
  @Test
  public void getByName() {
    FunctionInputsImpl inputs = new FunctionInputsImpl(VALUE1);
    inputs.addValue(VALUE2);

    assertEquals("1", inputs.getValue("foo-1"));
    assertEquals("2", inputs.getValue("foo-2"));
    assertNull(inputs.getValue("foo-3"));
  }
  
  @Test
  public void getBySpec() {
    FunctionInputsImpl inputs = new FunctionInputsImpl(VALUE1);
    inputs.addValue(VALUE2);

    assertEquals("1", inputs.getValue(SPEC1.getRequirementSpecification()));
    assertEquals("2", inputs.getValue(SPEC2.getRequirementSpecification()));
    assertNull(inputs.getValue("foo-3"));
  }
  
}
