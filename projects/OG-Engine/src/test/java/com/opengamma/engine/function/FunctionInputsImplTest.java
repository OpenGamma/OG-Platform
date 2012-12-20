/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

@Test
public class FunctionInputsImplTest {
  private static final ValueRequirement REQ1 = new ValueRequirement("foo-1", Currency.USD);
  private static final ValueRequirement REQ2 = new ValueRequirement("foo-2", Currency.USD);
  private static final ValueSpecification SPEC1 = new ValueSpecification(REQ1, "mockFunctionId");
  private static final ValueSpecification SPEC2 = new ValueSpecification(REQ2, "mockFunctionId");
  private static final ComputedValue VALUE1 = new ComputedValue(SPEC1, "1");
  private static final ComputedValue VALUE2 = new ComputedValue(SPEC2, "2");

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void nullValue() {
    (new FunctionInputsImpl()).addValue(null);
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void cyclicalValue() {
    (new FunctionInputsImpl()).addValue(new ComputedValue(SPEC1, new ComputedValue(SPEC1, "")));
  }
  
  public void getAll() {
    FunctionInputsImpl inputs = new FunctionInputsImpl(VALUE1);
    inputs.addValue(VALUE2);

    Collection<ComputedValue> values = inputs.getAllValues();
    assertNotNull(values);
    assertEquals(2, values.size());
    assertTrue(values.contains(VALUE1));
    assertTrue(values.contains(VALUE2));
  }
  
  public void getByName() {
    FunctionInputsImpl inputs = new FunctionInputsImpl(VALUE1);
    inputs.addValue(VALUE2);

    assertEquals("1", inputs.getValue("foo-1"));
    assertEquals("2", inputs.getValue("foo-2"));
    assertNull(inputs.getValue("foo-3"));
  }
  
  public void getBySpec() {
    FunctionInputsImpl inputs = new FunctionInputsImpl(VALUE1);
    inputs.addValue(VALUE2);

    assertEquals("1", inputs.getValue(REQ1));
    assertEquals("2", inputs.getValue(REQ2));
    assertNull(inputs.getValue("foo-3"));
  }
  
}
