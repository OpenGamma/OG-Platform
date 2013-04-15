/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Named UnitTest as it should contain just tests for logic that doesn't call through
 * to Redis.
 */
@Test(groups = TestGroup.UNIT)
public class RedisLastKnownValueStoreUnitTest {
  
  public void redisDataParsingSimpleDouble() {
    assertTrue(RedisLastKnownValueStore.fromRedisTextValue("0.52") instanceof Double);
    assertEquals(0.5, (Double)RedisLastKnownValueStore.fromRedisTextValue("0.5"), 0.0000001);
  }
  
  public void redisDataParsingDoubleArray() {
    assertTrue(RedisLastKnownValueStore.fromRedisTextValue(Arrays.toString(new double[] {0.5, 15.1})) instanceof double[]);
    double[] results = (double[]) RedisLastKnownValueStore.fromRedisTextValue(Arrays.toString(new double[] {0.5, 15.1}));
    assertEquals(2, results.length);
    assertEquals(0.5, results[0], 0.000001);
    assertEquals(15.1, results[1], 0.000001);
  }

  public void redisDataParsingRawString() {
    assertEquals("Kirk Wylie", RedisLastKnownValueStore.fromRedisTextValue("Kirk Wylie"));
  }
}
