/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Test the {@link ValuePropertiesBuilder} class.
 */
public class ValuePropertiesBuilderTest {
  
  private FudgeContext _context;
  private FudgeSerializationContext _serialization;
  private FudgeDeserializationContext _deserialization;
  
  @Before
  public void createContexts () {
    _context = OpenGammaFudgeContext.getInstance ();
    _serialization = new FudgeSerializationContext (_context);
    _deserialization = new FudgeDeserializationContext (_context);
  }
  
  private void test (final ValueProperties properties) {
    final FudgeFieldContainer message = _serialization.objectToFudgeMsg(properties);
    System.out.println (message);
    final ValueProperties converted = _deserialization.fudgeMsgToObject(ValueProperties.class, message);
    assertEquals (properties, converted);
  }
  
  @Test
  public void testEmptyProperties() {
    test (ValueProperties.none ());
  }  
  
  @Test
  public void testValues () {
    test (ValueProperties.builder ().withAny ("Any").with ("One", "a").with ("Two", "b", "c").get ());
  }
  
}