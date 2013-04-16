/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test ValueRequirement.
 */
@Test(groups = TestGroup.UNIT)
public class ValueRequirementTest {

  private static final UniqueId USD = UniqueId.of("currency", "USD");  
  private static final UniqueId GBP = UniqueId.of("currency", "GBP");
  private static final Position POSITION = new SimplePosition(UniqueId.of("A", "B"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final ComputationTargetSpecification SPEC = ComputationTargetSpecification.of(POSITION);

  public void test_constructor_Position() {
    ValueRequirement test = new ValueRequirement("DATA", SPEC);
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetReference());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_nullValue() {
    new ValueRequirement(null, SPEC);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_nullSpec() {
    new ValueRequirement("DATA", (ComputationTargetSpecification) null);
  }

  public void test_constructor_TypeUniqueId_Position() {
    ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetReference());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_TypeUniqueId_nullValue() {
    new ValueRequirement(null, ComputationTargetType.POSITION, POSITION.getUniqueId());
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test_constructor_TypeUniqueId_nullType() {
    new ValueRequirement("DATA", null, POSITION.getUniqueId());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_TypeIdentifier_nullValue() {
    new ValueRequirement(null, ComputationTargetType.PRIMITIVE, USD);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test_constructor_TypeIdentifier_nullType() {
    new ValueRequirement("DATA", null, USD);
  }

  public void test_constructor_Object_Position() {
    ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetReference());
  }

  //-------------------------------------------------------------------------
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
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.NULL, (UniqueId) null);
    assertFalse(req1.equals(req2));
  }

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
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.NULL, (UniqueId) null);
    assertFalse(req1.hashCode() == req2.hashCode());
  }

  public void test_toString() {
    ValueRequirement valueReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    String toString = valueReq.toString();
    assertNotNull(toString);
    assertTrue(toString.contains("USD"));
    assertTrue(toString.contains(ValueRequirementNames.DISCOUNT_CURVE));
    assertTrue(toString.contains(ComputationTargetType.PRIMITIVE.toString()));
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding() {
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    FudgeSerializer serializationContext = new FudgeSerializer(context);
    FudgeDeserializer deserializationContext = new FudgeDeserializer(context);
    ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.PRIMITIVE, USD);
    MutableFudgeMsg inMsg = serializationContext.objectToFudgeMsg(test);
    assertNotNull(inMsg);
    assertEquals(3, inMsg.getNumFields());
    FudgeMsg outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    ValueRequirement decoded = deserializationContext.fudgeMsgToObject(ValueRequirement.class, outMsg);
    assertEquals(test, decoded);
    test = new ValueRequirement("DATA", ComputationTargetType.PRIMITIVE, USD, ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo").get());
    inMsg = serializationContext.objectToFudgeMsg(test);
    assertNotNull(inMsg);
    assertEquals(4, inMsg.getNumFields());
    outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    decoded = deserializationContext.fudgeMsgToObject(ValueRequirement.class, outMsg);
    assertEquals(test, decoded);
  }

}
