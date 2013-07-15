/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CalculationJobSpecificationTest {
  
  public void testHashCode() {
    Instant valuationTime = Instant.now();
    CalculationJobSpecification spec1 = new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), "config", valuationTime, 1L);
    CalculationJobSpecification spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), "config", valuationTime, 1L);
    
    assertEquals(spec1.hashCode(), spec2.hashCode());
    
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle2"), "config", valuationTime, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config2", valuationTime, 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", valuationTime.plusMillis(1), 1L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", valuationTime, 2L);
    assertFalse(spec1.hashCode() == spec2.hashCode());
  }

  public void testEquals() {
    Instant valuationTime = Instant.now();
    CalculationJobSpecification spec1 = new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), "config", valuationTime, 1L);
    assertTrue(spec1.equals(spec1));
    assertFalse(spec1.equals(null));
    assertFalse(spec1.equals("Kirk"));
    CalculationJobSpecification spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), "config", valuationTime, 1L);
    assertTrue(spec1.equals(spec2));
    
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle2"), "config", valuationTime, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config2", valuationTime, 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", valuationTime.plusMillis(1), 1L);
    assertFalse(spec1.equals(spec2));
    spec2 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", valuationTime, 2L);
    assertFalse(spec1.equals(spec2));
  }
  
  public void fudgeEncoding() {
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    CalculationJobSpecification spec1 = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);
    FudgeSerializer serializationContext = new FudgeSerializer(context);
    MutableFudgeMsg inMsg = serializationContext.objectToFudgeMsg(spec1);
    FudgeMsg outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    FudgeDeserializer deserializationContext = new FudgeDeserializer(context);
    CalculationJobSpecification spec2 = deserializationContext.fudgeMsgToObject(CalculationJobSpecification.class, outMsg);
    assertEquals(spec1, spec2);
  }

}
