/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.BuilderTestProxyFactory.BuilderTestProxy;

/**
 * Base class for builder tests.
 */
public abstract class AbstractBuilderTestCase {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBuilderTestCase.class);

  private FudgeContext _context;
  private FudgeSerializer _serializer;
  private FudgeDeserializer _deserializer;
  private BuilderTestProxy _proxy;

  @BeforeMethod
  public void createContexts() {
    _context = OpenGammaFudgeContext.getInstance();
    _serializer = new FudgeSerializer(_context);
    _deserializer = new FudgeDeserializer(_context);
    _proxy = new BuilderTestProxyFactory().getProxy();
  }

  protected FudgeContext getFudgeContext() {
    return _context;
  }

  protected FudgeSerializer getFudgeSerializer() {
    return _serializer;
  }

  protected FudgeDeserializer getFudgeDeserializer() {
    return _deserializer;
  }

  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    getLogger().debug("cycle object {} of class {}", object, clazz);
    final FudgeMsg message = getFudgeSerializer().objectToFudgeMsg(object);
    getLogger().debug("message {}", message);
    
    final FudgeMsg proxiedMessage = _proxy.proxy(clazz, message);
    getLogger().debug("message after proxy {}", proxiedMessage);
    
    final T cycled = getFudgeDeserializer().fudgeMsgToObject(clazz, proxiedMessage);
    getLogger().debug("created object {}", cycled);
    return cycled;
  }

  protected <T> void assertEncodeDecodeCycle(final Class<T> clazz, final T object) {
    assertEquals(object, cycleObject(clazz, object));
  }

  protected Logger getLogger() {
    return s_logger;
  }

}
