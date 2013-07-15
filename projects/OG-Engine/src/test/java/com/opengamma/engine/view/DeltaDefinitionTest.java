/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test DeltaDefinition. 
 */
@Test(groups = TestGroup.UNIT)
public class DeltaDefinitionTest {
  
  public void testEmptyDefinition() {
    DeltaDefinition dd = new DeltaDefinition();
    doBasicTests(dd);
  }
  
  public void testWithCustomNumberComparer() {
    DeltaDefinition dd = new DeltaDefinition();
    dd.setNumberComparer(new NumberDeltaComparer(3));
    
    doBasicTests(dd);
    
    // Just check that the comparer is being used - its own tests check that it actually works 
    assertFalse(dd.isDelta(createComputedValue(123.1234567), createComputedValue(123.1239999)));
    assertTrue(dd.isDelta(createComputedValue(123.1234567), createComputedValue(123.12555555)));
  }
  
  private void doBasicTests(DeltaDefinition dd) {
    assertFalse(dd.isDelta(null, null));
    assertFalse(dd.isDelta(createComputedValue("abc"), createComputedValue("abc")));
    assertFalse(dd.isDelta(createComputedValue(123.0), createComputedValue(123.0)));
    assertTrue(dd.isDelta(createComputedValue(123.0), createComputedValue(123.1)));
    
    // Different specifications only should indicate a delta
    assertTrue(dd.isDelta(createComputedValue("test1", 123), createComputedValue("test2", 123)));
  }
  
  private ComputedValue createComputedValue(Object value) {
    return createComputedValue("test", value);
  }
  
  private ComputedValue createComputedValue(String valueName, Object value) {
    return new ComputedValue(createValueSpecification(valueName), value);
  }
  
  private ValueSpecification createValueSpecification(String valueName) {
    return new ValueSpecification(valueName, ComputationTargetSpecification.of(UniqueId.of("foo", "bar")), ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
  }
}
