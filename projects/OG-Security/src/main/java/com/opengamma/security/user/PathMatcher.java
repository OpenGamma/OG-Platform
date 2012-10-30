/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.util.regex.Pattern;

import com.opengamma.util.ArgumentChecker;

/**
 * In OpenGamma, permissions can be regular expressions, for example /MarketData/Bloomberg/&#42;/View.
 * This class provides support for evaluating these regular expressions.   
 *
 * @see Authority
 */
public class PathMatcher {

  /**
   * This method provides Ant-style regular expression matching for paths (/Foo/Bar/Baz). The only special 
   * regular expression characters are:
   * <ul>
   * <li>&#42;&#42; matches anything
   * <li>&#42; matches anything, but only within single path element
   * </ul>
   * The path separator is forward slash ('/').
   * <p> 
   * For example:
   * <ul>
   * <li>/MarketData/Bloomberg/&#42;/View will match /MarketData/Bloomberg/AAPL/View 
   * <li>/Portf&#42;/&#42;&#42; will match both /Portfolio/2DFS/View and /Portfolio/3XYZ/Modify
   * </ul>
   * 
   * @param input The path to match
   * @param regex The regular expression to match against
   * @return true if the input string matches the regex
   */
  public static boolean matches(String input, String regex) {
    ArgumentChecker.notNull(input, "Path to match");
    ArgumentChecker.notNull(regex, "Regular expression to match against");
    
    // We need to modify the regex in two ways.
    // 1. Escape the regexp - don't want $, ^, etc., in the regex string contaminating the results.
    // 2. Change * syntax to Java 1.4 regex syntax (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html). 
    // E.g., /MarketData/Bloomberg/*/View  -> \Q/MarketData/Bloomberg/\E[^/]*\Q/View\E 
    StringBuffer quotedRegexBuffer = new StringBuffer();
    String[] parts = regex.split("\\*", -1);
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (part.length() > 0) {
        String escapedPart = Pattern.quote(part);
        quotedRegexBuffer.append(escapedPart);
      }
      
      if (i < parts.length - 1) {
        if (parts[i + 1].isEmpty() && (i + 2) != parts.length) { // this means there are two stars in sequence - **
          // replace ** with .*
          quotedRegexBuffer.append(".*");
          // skip over the next star
          i++;
        } else { // this means there is just a single *
          // replace * with [^/]*
          quotedRegexBuffer.append("[^/]*");                    
        }
      } 
    }
    
    String quotedRegex = quotedRegexBuffer.toString();
    return input.matches(quotedRegex);
  }

}
