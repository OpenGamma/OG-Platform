/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test PerformanceCounter.
 */
@Test(groups = TestGroup.UNIT)
public class PerformanceCounterTest {
  
  public void oneSecCounter() {
    PerformanceCounter counter = new PerformanceCounter(1, 5000);
    
    assertEquals(0.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
    
    counter.hit(5200);
    counter.hit(5500);
    
    assertEquals(2.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
    
    counter.hit(6100);
    counter.hit(6200);
    
    assertEquals(2.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
    
    counter.hit(7100);
    counter.hit(7100);
    counter.hit(7100);
    
    assertEquals(3.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
  }
  
  public void sixtySecondCounter() {
    PerformanceCounter counter = new PerformanceCounter(60, 3000);
    
    assertEquals(0.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
    assertEquals(0.0, counter.getHitsPerSecondAsOfLastHit(30), 0.001);
    assertEquals(0.0, counter.getHitsPerSecondAsOfLastHit(60), 0.001);
    
    counter.hit(3000);
    counter.hit(3100);
    counter.hit(3200);
    
    assertEquals(3 / 1.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
    assertEquals(3 / 30.0, counter.getHitsPerSecondAsOfLastHit(30), 0.001);
    assertEquals(3 / 60.0, counter.getHitsPerSecondAsOfLastHit(60), 0.001);
    
    counter.hit(7500);
    counter.hit(7600);
    
    assertEquals(2 / 1.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001);
    assertEquals(5 / 30.0, counter.getHitsPerSecondAsOfLastHit(30), 0.001);
    assertEquals(5 / 60.0, counter.getHitsPerSecondAsOfLastHit(60), 0.001);
    
    counter.hit(14999);
    counter.hit(15000);
    counter.hit(15000);
    counter.hit(15000);
    counter.hit(15100);
    
    assertEquals(4 / 1.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001); // 14999 ignored because it's on a different second to 15100
    assertEquals(5 / 2.0, counter.getHitsPerSecondAsOfLastHit(2), 0.001);
    assertEquals(10 / 30.0, counter.getHitsPerSecondAsOfLastHit(30), 0.001);
    assertEquals(10 / 60.0, counter.getHitsPerSecondAsOfLastHit(60), 0.001);
    
    // Now roll over
    counter.hit(62999);
    counter.hit(63000);
    counter.hit(63001);
    counter.hit(63002);
    counter.hit(63003);
    
    assertEquals(4 / 1.0, counter.getHitsPerSecondAsOfLastHit(1), 0.001); // 62999 ignored
    assertEquals(5 / 2.0, counter.getHitsPerSecondAsOfLastHit(2), 0.001);
    assertEquals(5 / 30.0, counter.getHitsPerSecondAsOfLastHit(30), 0.001);
    assertEquals(12 / 60.0, counter.getHitsPerSecondAsOfLastHit(60), 0.001); // the first 3 will have dropped off
    
    assertEquals(12 / 60.0, counter.getHitsPerSecond(60, 64000), 0.001);
    assertEquals(12 / 60.0, counter.getHitsPerSecond(60, 66000), 0.001);
    assertEquals(10 / 60.0, counter.getHitsPerSecond(60, 67000), 0.001);
    assertEquals(10 / 60.0, counter.getHitsPerSecond(60, 68000), 0.001);
    
    assertEquals(0 / 60.0, counter.getHitsPerSecond(60, 2000000), 0.001);
  }
  
  public void clockGoingBackwards() {
    PerformanceCounter counter = new PerformanceCounter(60, 0);
    
    counter.hit(1000);
    counter.hit(2000);
    
    assertEquals(2 / 60.0, counter.getHitsPerSecond(60, 3000), 0.001);
    assertEquals(0, counter.getHitsPerSecond(60, 1500), 0.001); // historical timestamp passed in - will reset
    
    counter.hit(4000);
    counter.hit(5000);
    assertEquals(2 / 60.0, counter.getHitsPerSecond(60, 6000), 0.001);
    
    counter.hit(1000); // historical timestamp passed in - will reset
    assertEquals(1 / 60.0, counter.getHitsPerSecond(60, 7000), 0.001);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroCounter() {
    new PerformanceCounter(0);    
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negativeCounter() {
    new PerformanceCounter(-8);    
  }

}
