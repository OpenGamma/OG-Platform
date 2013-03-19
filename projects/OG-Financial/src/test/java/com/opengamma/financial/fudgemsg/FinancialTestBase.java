/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import com.opengamma.core.region.RegionSource;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Base class for testing OG-Financial objects to and from Fudge messages.
 */
public class FinancialTestBase {

  private static final Logger s_logger = LoggerFactory.getLogger(FinancialTestBase.class);

  private RegionSource _regionSource;
  private FudgeContext _fudgeContext;

  @BeforeMethod(groups = TestGroup.UNIT)
  public void createFudgeContext() {
    _fudgeContext = OpenGammaFudgeContext.getInstance();
    final RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.createPopulated(regionMaster);
    _regionSource = new MasterRegionSource(regionMaster);
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected RegionSource getRegionSource() {
    return _regionSource;
  }

  private FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] data = getFudgeContext().toByteArray(message);
    s_logger.info("{} bytes", data.length);
    return getFudgeContext().deserialize(data).getMessage();
  }

  protected <T> T cycleObject(final Class<T> clazz, final T object) {
    final T newObject = cycleGenericObject(clazz, object);
    assertEquals(object.getClass(), newObject.getClass());
    return newObject;
  }

  protected <T> T cycleGenericObject(final Class<T> clazz, final T object) {
    s_logger.info("object {}", object);
    final FudgeSerializer fudgeSerializationContext = new FudgeSerializer(getFudgeContext());
    final FudgeDeserializer fudgeDeserializationContext = new FudgeDeserializer(getFudgeContext());
    final MutableFudgeMsg messageIn = fudgeSerializationContext.newMessage();
    fudgeSerializationContext.addToMessageWithClassHeaders(messageIn, "test", null, object, clazz);
    s_logger.info("message {}", messageIn);
    final FudgeMsg messageOut = cycleMessage(messageIn);
    s_logger.info("message {}", messageOut);
    final T newObject = fudgeDeserializationContext.fieldValueToObject(clazz, messageOut.getByName("test"));
    assertNotNull(newObject);
    s_logger.info("object {}", newObject);
    assertTrue(clazz.isAssignableFrom(newObject.getClass()));
    return newObject;
  }

}
