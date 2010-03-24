/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Currency;

import org.junit.Test;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * 
 *
 * @author kirk
 */
public class ComputationTargetTest {
  
  @Test(expected=NullPointerException.class)
  public void nullType() {
    DomainSpecificIdentifier id = new DomainSpecificIdentifier(new IdentificationDomain("foo"), "bar");
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
    DomainSpecificIdentifier id = new DomainSpecificIdentifier(new IdentificationDomain("foo"), "bar");
    new ComputationTarget(ComputationTargetType.PRIMITIVE, id);
  }

}
