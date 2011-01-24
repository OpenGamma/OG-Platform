/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Test ValueRequirement.
 */
public class ValueRequirementTest {

  private static final UniqueIdentifier USD = UniqueIdentifier.of("currency", "USD");  
  private static final UniqueIdentifier GBP = UniqueIdentifier.of("currency", "GBP");
  private static final Position POSITION = new PositionImpl(UniqueIdentifier.of("A", "B"), new BigDecimal(1), IdentifierBundle.EMPTY);
  private static final ComputationTargetSpecification SPEC = new ComputationTargetSpecification(POSITION);

  @Test
  public void test_constructor_Position() {
    ValueRequirement test = new ValueRequirement("DATA", SPEC);
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetSpecification());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_nullValue() {
    new ValueRequirement(null, SPEC);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_nullSpec() {
    new ValueRequirement("DATA", null);
  }

  @Test
  public void test_constructor_TypeUniqueIdentifier_Position() {
    ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetSpecification());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_TypeUniqueIdentifier_nullValue() {
    new ValueRequirement(null, ComputationTargetType.POSITION, POSITION.getUniqueId());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_TypeUniqueIdentifier_nullType() {
    new ValueRequirement("DATA", null, POSITION.getUniqueId());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_TypeIdentifier_nullValue() {
    new ValueRequirement(null, ComputationTargetType.PRIMITIVE, USD);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_TypeIdentifier_nullType() {
    new ValueRequirement("DATA", null, USD);
  }

  @Test
  public void test_constructor_Object_Position() {
    ValueRequirement test = new ValueRequirement("DATA", POSITION);
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetSpecification());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals() {
    ValueRequirement req1 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    assertTrue(req1.equals(req1));
    assertFalse(req1.equals(null));
    assertFalse(req1.equals("Rubbish"));
    
    ValueRequirement req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    assertTrue(req1.equals(req2));
    assertTrue(req2.equals(req1));
    
    req2 = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, USD);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, GBP);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, (UniqueIdentifier) null);
    assertFalse(req1.equals(req2));
  }

  @Test
  public void test_hashCode() {
    ValueRequirement req1 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    ValueRequirement req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    
    assertTrue(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, USD);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, GBP);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, (UniqueIdentifier) null);
    assertFalse(req1.hashCode() == req2.hashCode());
  }

  @Test
  public void test_toString() {
    ValueRequirement valueReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    String toString = valueReq.toString();
    assertNotNull(toString);
    assertTrue(toString.contains("USD"));
    assertTrue(toString.contains(ValueRequirementNames.DISCOUNT_CURVE));
    assertTrue(toString.contains(ComputationTargetType.PRIMITIVE.toString()));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_fudgeEncoding() {
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(context);
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(context);
    ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.PRIMITIVE, USD);
    MutableFudgeFieldContainer inMsg = serializationContext.objectToFudgeMsg(test);
    assertNotNull(inMsg);
    assertEquals(3, inMsg.getNumFields());
    FudgeFieldContainer outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    ValueRequirement decoded = deserializationContext.fudgeMsgToObject(ValueRequirement.class, outMsg);
    assertEquals(test, decoded);
    test = new ValueRequirement("DATA", ComputationTargetType.PRIMITIVE, USD, ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo").get ());
    inMsg = serializationContext.objectToFudgeMsg(test);
    assertNotNull(inMsg);
    assertEquals(4, inMsg.getNumFields());
    outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    decoded = deserializationContext.fudgeMsgToObject(ValueRequirement.class, outMsg);
    assertEquals(test, decoded);
  }

}
