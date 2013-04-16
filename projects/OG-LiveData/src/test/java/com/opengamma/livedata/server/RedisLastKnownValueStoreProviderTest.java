/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * A full unit test for {@link RedisLastKnownValueStoreProvider} and
 * also {@link RedisLastKnownValueStore}. 
 */
@Test(groups = TestGroup.INTEGRATION)
public class RedisLastKnownValueStoreProviderTest {
  // TODO kirk 2012-07-18 -- This needs to be moved to external properties.
  private static final String REDIS_SERVER = "redis-lx-1";
  
  static String generatePrefix(String testName) {
    StringBuilder sb = new StringBuilder();
    sb.append(System.getProperty("user.name"));
    sb.append("-");
    sb.append(testName);
    sb.append("-");
    sb.append(System.currentTimeMillis());
    sb.append("-");
    return sb.toString();
  }
  
  // TODO kirk 2012-07-18 -- This needs to be restricted to times where there's a Redis
  // server available for testing.
  @Test
  public void testBasicFunctionality() {
    RedisLastKnownValueStoreProvider provider = new RedisLastKnownValueStoreProvider();
    provider.setServer(REDIS_SERVER);
    provider.setGlobalPrefix(generatePrefix("testBasicFunctionality"));
    provider.setWriteThrough(true);
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    
    LastKnownValueStore store = provider.newInstance(ExternalId.of("Test", "testBasicFunctionality"), "no-norm");
    FudgeMsg currFields = store.getFields();
    assertTrue(currFields.isEmpty());
    
    msg.add("bid", 1.0d);
    msg.add("ask", 2.0d);
    
    store.updateFields(msg);
    
    currFields = store.getFields();
    assertEquals(1.0d, currFields.getDouble("bid"), 0.0005d);
    assertEquals(2.0d, currFields.getDouble("ask"), 0.0005d);
    
    // Reset the store.
    store = provider.newInstance(ExternalId.of("Test", "testBasicFunctionality"), "no-norm");
    currFields = store.getFields();
    assertEquals(1.0d, currFields.getDouble("bid"), 0.0005d);
    assertEquals(2.0d, currFields.getDouble("ask"), 0.0005d);
    
    // Write through another value and confirm it works.
    msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("bid", 5.0d);
    store.updateFields(msg);
    
    store = provider.newInstance(ExternalId.of("Test", "testBasicFunctionality"), "no-norm");
    currFields = store.getFields();
    assertEquals(5.0d, currFields.getDouble("bid"), 0.0005d);
    assertEquals(2.0d, currFields.getDouble("ask"), 0.0005d);
    
    // Reset the store. This time don't write through.
    provider.setWriteThrough(false);
    store = provider.newInstance(ExternalId.of("Test", "testBasicFunctionality"), "no-norm");
    currFields = store.getFields();
    assertEquals(5.0d, currFields.getDouble("bid"), 0.0005d);
    assertEquals(2.0d, currFields.getDouble("ask"), 0.0005d);
    
    msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("bid", 3.0d);
    msg.add("ask", 4.0d);
    store.updateFields(msg);
    currFields = store.getFields();
    assertEquals(3.0d, currFields.getDouble("bid"), 0.0005d);
    assertEquals(4.0d, currFields.getDouble("ask"), 0.0005d);

    // Reset the store.
    store = provider.newInstance(ExternalId.of("Test", "testBasicFunctionality"), "no-norm");
    currFields = store.getFields();
    assertEquals(5.0d, currFields.getDouble("bid"), 0.0005d);
    assertEquals(2.0d, currFields.getDouble("ask"), 0.0005d);
  }

}
