/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class ComputationTargetTest {
  
  @Test(expected=NullPointerException.class)
  public void nullType() {
    new ComputationTarget(null, "Foo");
  }
  
  @Test(expected=NullPointerException.class)
  public void nullValueButRequired() {
    ComputationTarget.checkValueValid(ComputationTargetType.POSITION, null);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void illegalSecurity() {
    ComputationTarget.checkValueValid(ComputationTargetType.SECURITY, "foo");
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalPosition() {
    ComputationTarget.checkValueValid(ComputationTargetType.POSITION, "foo");
  }

  @Test(expected=IllegalArgumentException.class)
  public void illegalAggregate() {
    ComputationTarget.checkValueValid(ComputationTargetType.MULTIPLE_POSITIONS, "foo");
  }
  
  @Test
  public void legalPrimitives() {
    new ComputationTarget(ComputationTargetType.PRIMITIVE, null);
    new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
  }

}
