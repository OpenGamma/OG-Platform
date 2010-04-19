/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;

/**
 * 
 *
 * @author kirk
 */
public class ComputationTargetTest {
  
  @Test(expected=NullPointerException.class)
  public void nullType() {
    Identifier id = new Identifier(new IdentificationScheme("foo"), "bar");
    new ComputationTarget(null, id);
  }
  
  @Test(expected=NullPointerException.class)
  public void nullValue() {
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
    Identifier id = new Identifier(new IdentificationScheme("foo"), "bar");
    new ComputationTarget(ComputationTargetType.PRIMITIVE, id);
  }

}
