/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  private static final ValueRequirement REQ1 = new ValueRequirement("foo-1", "USD");
  private static final ValueRequirement REQ2 = new ValueRequirement("foo-2", "USD");
  private static final ValueSpecification SPEC1 = new ValueSpecification(REQ1, "mockFunctionId");
  private static final ValueSpecification SPEC2 = new ValueSpecification(REQ2, "mockFunctionId");
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

    assertEquals("1", inputs.getValue(REQ1));
    assertEquals("2", inputs.getValue(REQ2));
    assertNull(inputs.getValue("foo-3"));
  }
  
}
