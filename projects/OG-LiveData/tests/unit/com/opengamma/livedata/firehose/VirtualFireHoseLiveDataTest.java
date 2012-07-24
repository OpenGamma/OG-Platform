/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test(groups = "unit")
public class VirtualFireHoseLiveDataTest {

  private static class Impl extends AbstractFireHoseLiveData {

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
      return false;
    }
  }

  private static class ValueUpdateListener implements FireHoseLiveData.ValueUpdateListener {
    private String _uniqueId;

    @Override
    public void updatedValue(final String uniqueId, final FudgeMsg msg) {
      _uniqueId = uniqueId;
    }

    public String getUniqueId() {
      return _uniqueId;
    }
  }

  private static class DataStateListener implements FireHoseLiveData.DataStateListener {
    private boolean _refreshed;

    @Override
    public void valuesRefreshed() {
      _refreshed = true;
    }

    public boolean isRefreshed() {
      return _refreshed;
    }
  }

  public void testBasicOperation() {
    final AbstractFireHoseLiveData fireHose = new Impl();
    assertNull(fireHose.getLatestValue("Foo"));
    assertFalse(fireHose.isMarketDataComplete());
    fireHose.storeValue("Foo", FudgeContext.GLOBAL_DEFAULT.newMessage());
    assertNotNull(fireHose.getLatestValue("Foo"));
    fireHose.setMarketDataComplete(true);
    assertTrue(fireHose.isMarketDataComplete());
  }

  public void testValueUpdateListener() {
    final AbstractFireHoseLiveData fireHose = new Impl();
    fireHose.storeValue("Foo", FudgeContext.GLOBAL_DEFAULT.newMessage());
    final ValueUpdateListener listener = new ValueUpdateListener();
    fireHose.setValueUpdateListener(listener);
    assertNull(listener.getUniqueId());
    fireHose.storeValue("Foo", FudgeContext.GLOBAL_DEFAULT.newMessage());
    assertEquals(listener.getUniqueId(), "Foo");
    fireHose.storeValue("Bar", FudgeContext.GLOBAL_DEFAULT.newMessage());
    assertEquals(listener.getUniqueId(), "Bar");
    fireHose.setValueUpdateListener(null);
    fireHose.storeValue("Foo", FudgeContext.GLOBAL_DEFAULT.newMessage());
    assertEquals(listener.getUniqueId(), "Bar");
  }

  public void testMarketDataCompleteCallback1() {
    final AbstractFireHoseLiveData fireHose = new Impl();
    fireHose.setMarketDataComplete(true);
    final DataStateListener listener = new DataStateListener();
    assertFalse(listener.isRefreshed());
    fireHose.setDataStateListener(listener);
    assertTrue(listener.isRefreshed());
    fireHose.setDataStateListener(null);
  }

  public void testMarketDataCompleteCallback2() {
    final AbstractFireHoseLiveData fireHose = new Impl();
    final DataStateListener listener = new DataStateListener();
    fireHose.setDataStateListener(listener);
    assertFalse(listener.isRefreshed());
    fireHose.setMarketDataComplete(true);
    assertTrue(listener.isRefreshed());
    fireHose.setMarketDataComplete(false);
    // Not excellent behavior at the moment, but if it changes we want the test to fail to alert us as other things might require changes
    assertTrue(listener.isRefreshed());
  }

}
