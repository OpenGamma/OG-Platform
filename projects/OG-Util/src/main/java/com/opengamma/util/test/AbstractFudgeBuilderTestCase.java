/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.BuilderTestProxyFactory.BuilderTestProxy;

/**
 * Base class for builder tests.
 */
public abstract class AbstractFudgeBuilderTestCase {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractFudgeBuilderTestCase.class);

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

  protected Logger getLogger() {
    return s_logger;
  }

  //-------------------------------------------------------------------------
  protected <T> void assertEncodeDecodeCycle(final Class<T> clazz, final T object) {
    assertEquals(object, cycleObjectProxy(clazz, object));
    assertEquals(object, cycleObjectBytes(clazz, object));
  }

  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    return cycleObjectProxy(clazz, object);
  }

  private <T> T cycleObjectProxy(final Class<T> clazz, final T object) {
    getLogger().debug("cycle object {} of class by proxy {}", object, clazz);

    final MutableFudgeMsg msgOut = getFudgeSerializer().newMessage();
    getFudgeSerializer().addToMessage(msgOut, "test", null, object);
    getLogger().debug("message out by proxy {}", msgOut);

    final FudgeMsg msgIn = _proxy.proxy(clazz, msgOut);
    getLogger().debug("message in by proxy {}", msgIn);

    final T cycled = getFudgeDeserializer().fieldValueToObject(clazz, msgIn.getByName("test"));
    getLogger().debug("created object by proxy {}", cycled);
    assertTrue(clazz.isAssignableFrom(cycled.getClass()));
    return cycled;
  }

  private <T> T cycleObjectBytes(final Class<T> clazz, final T object) {
    getLogger().debug("cycle object {} of class by bytes {}", object, clazz);

    final MutableFudgeMsg msgOut = getFudgeSerializer().newMessage();
    getFudgeSerializer().addToMessage(msgOut, "test", null, object);
    getLogger().debug("message out by bytes {}", msgOut);

    final FudgeMsg msgIn = cycleMessage(msgOut);
    getLogger().debug("message in by bytes {}", msgIn);

    final T cycled = getFudgeDeserializer().fieldValueToObject(clazz, msgIn.getByName("test"));
    getLogger().debug("created object by bytes {}", cycled);
    assertTrue(clazz.isAssignableFrom(cycled.getClass()));
    return cycled;
  }

  protected FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] data = getFudgeContext().toByteArray(message);
    s_logger.info("{} bytes", data.length);
    return getFudgeContext().deserialize(data).getMessage();
  }

  @SuppressWarnings("unchecked")
  protected <T> T cycleObjectOverBytes(final T object) {
    ByteArrayOutputStream _output = new ByteArrayOutputStream();
    FudgeObjectWriter _fudgeObjectWriter = getFudgeContext().createObjectWriter(_output);

    _fudgeObjectWriter.write(object);

    ByteArrayInputStream input = new ByteArrayInputStream(_output.toByteArray());

    FudgeObjectReader fudgeObjectReader = getFudgeContext().createObjectReader(input);

    return (T) fudgeObjectReader.read();
  }

  public static void isInstanceOf(Object parameter, Class<?> clazz) {
    if (!clazz.isInstance(parameter)) {
      throw new AssertionError("Expected an object to be instance of <" + clazz.getName() + "> but it was instance of <" + parameter.getClass().getName() + "> actually.");
    }
  }

}
