/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator.function;

import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for the MarketDataPointRandomizingManipulator.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataPointRandomizingManipulatorTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLowerBoundRejected() {
    createManipulator(null, 1.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUpperBoundRejected() {
    createManipulator(0.9, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIdenticalBoundsRejected() {
    createManipulator(0.9, 0.9);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpperBoundMustBeGreaterThanLower() {
    createManipulator(1.1, 0.9);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeBoundsRejected() {
    createManipulator(-1.1, 0.9);
  }

  @Test
  public void testValuesAreDistributed() {
    StructureManipulator<Double> manipulator = createManipulator(0.9, 1.1);
    Set<Double> producedValues = new HashSet<>(10000);
    for (int i = 0; i < 10000; i++) {
      ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "notUsed").get();
      ValueSpecification valueSpec = new ValueSpecification("notUsed", ComputationTargetSpecification.NULL, properties);
      Double shifted = manipulator.execute(1000d, valueSpec, new FunctionExecutionContext());
      assertTrue("Expected shifted to be >= 900 but was " + shifted, shifted >= 900);
      assertTrue("Expected shifted to be < 1100 but was " + shifted, shifted < 1100);
      producedValues.add(shifted);
    }

    assertTrue("Expected there to be multiple values produced, but there were only " + producedValues.size(),
      producedValues.size() > 100);
  }

  private MarketDataPointRandomizingManipulator createManipulator(Double lowerBound, Double upperBound) {
    return new MarketDataPointRandomizingManipulator(lowerBound, upperBound);
  }
}
