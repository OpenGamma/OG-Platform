/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


/**
 * 
 *
 * @author yomi
 */
public class StringUtilTest {
  
  @Test
  public void removeChar() throws Exception {
    assertEquals("20100116", StringUtil.removeChar("2010-01-16", '-'));
    assertEquals("20100116", StringUtil.removeChar("2010/01/16", '/'));
    String result = StringUtil.removeChar(null, 'a');
    assertNull(result); 
    assertEquals("20100116", StringUtil.removeChar(" 2010 01 16 ", ' '));
    assertEquals(" 20100116 ", StringUtil.removeChar(" 2010-01-16 ", '-'));
  }

}
