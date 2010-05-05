/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;

/**
 * 
 *
 * @author kirk
 */
public class ValueRequirementTest {
  
  private final static Identifier USD = new Identifier(new IdentificationScheme("currency"), "USD");  
  private final static Identifier GBP = new Identifier(new IdentificationScheme("currency"), "GBP");
  
  @Test(expected=NullPointerException.class)
  public void nullValueType() {
    new ValueRequirement(null, ComputationTargetType.PRIMITIVE, USD);
  }

  @Test(expected=NullPointerException.class)
  public void nullComputationTargetType() {
    new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, null, USD);
  }
  
  @Test
  public void validConstructors() {
    new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, (Identifier) null);
  }
  
  @Test
  public void toStringTest() {
    ValueRequirement valueReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    String toString = valueReq.toString();
    assertNotNull(toString);
    assertTrue(toString.contains("USD"));
    assertTrue(toString.contains(ValueRequirementNames.DISCOUNT_CURVE));
    assertTrue(toString.contains(ComputationTargetType.PRIMITIVE.toString()));
  }
  
  @Test
  public void hashCodeTest() {
    ValueRequirement req1 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    ValueRequirement req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    
    assertTrue(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, USD);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.SECURITY, USD);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, GBP);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, (Identifier) null);
    assertFalse(req1.hashCode() == req2.hashCode());
  }
  
  @Test
  public void equalsTest() {
    ValueRequirement req1 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    
    assertTrue(req1.equals(req1));
    assertFalse(req1.equals(null));
    
    ValueRequirement req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    assertTrue(req1.equals(req2));
    
    req2 = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, USD);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.SECURITY, USD);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, GBP);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, (Identifier) null);
    assertFalse(req1.equals(req2));
    
  }

}
