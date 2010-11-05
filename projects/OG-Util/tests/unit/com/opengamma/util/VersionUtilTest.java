/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class VersionUtilTest {
  
  @Test
  public void validPropertyFile() {
    assertEquals("FIN-507", VersionUtil.getVersion("VersionUtilTest-1"));
  }
  
  @Test
  public void invalidPropertyFile() {
    checkLocalVersionOk(VersionUtil.getVersion("VersionUtilTest-2"));
  }
  
  @Test
  public void noPropertyFile() {
    checkLocalVersionOk(VersionUtil.getVersion("VersionUtilTest-3"));
  }
  
  private void checkLocalVersionOk(String version) {
    assertTrue(version.startsWith("local-"));
    
    long currentTimeMillis = Long.parseLong(version.substring("local-".length()));
    assertTrue(currentTimeMillis >= 0);
    assertTrue(currentTimeMillis <= System.currentTimeMillis());
  }

}
