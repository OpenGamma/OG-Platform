/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * 
 *
 * @author yomi
 */
public class StringUtil {
  
  private StringUtil() {
  }
  
  /**
   * Remove all occurences of character c in string s
   * 
   * @param s
   * @param c
   * @return
   */
  public static String removeChar(String s, char c) {
    if (s == null) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < s.length(); i ++) {
       if (s.charAt(i) != c) {
         buf.append(s.charAt(i));
       }
    }
    return buf.toString();
  }
}
