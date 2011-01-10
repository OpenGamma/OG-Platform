/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Base class for builder tests.
 */
public abstract class AbstractBuilderTestCase {
  
  private static final Logger s_logger = LoggerFactory.getLogger (AbstractBuilderTestCase.class);
  
  private FudgeContext _context;
  private FudgeSerializationContext _serialization;
  private FudgeDeserializationContext _deserialization;
  
  @Before
  public void createContexts () {
    _context = OpenGammaFudgeContext.getInstance ();
    _serialization = new FudgeSerializationContext (_context);
    _deserialization = new FudgeDeserializationContext (_context);
  }
  
  protected FudgeContext getFudgeContext () {
    return _context;
  }
  
  protected FudgeSerializationContext getFudgeSerializationContext () {
    return _serialization;
  }
  
  protected FudgeDeserializationContext getFudgeDeserializationContext () {
    return _deserialization;
  }
  
  protected <T> T cycleObject (final Class<T> clazz, final T object) {
    getLogger ().debug ("cycle object {} of class {}", object, clazz);
    final FudgeFieldContainer message = getFudgeSerializationContext ().objectToFudgeMsg(object);
    getLogger ().debug ("message {}", message);
    final T cycled = getFudgeDeserializationContext ().fudgeMsgToObject(clazz, message);
    getLogger ().debug ("created object {}", cycled);
    return cycled;
  }
  
  protected <T> void assertEncodeDecodeCycle (final Class<T> clazz, final T object) {
    assertEquals (object, cycleObject (clazz, object));
  }
  
  protected Logger getLogger () {
    return s_logger;
  }
  
}