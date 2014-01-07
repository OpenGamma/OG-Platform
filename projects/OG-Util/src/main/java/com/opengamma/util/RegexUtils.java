/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods to simplify comparisons.
 * <p>
 * This is a static thread-safe utility class.
 */
public final class RegexUtils {

  /**
   * Restricted constructor.
   */
  private RegexUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a simple wildcard style pattern to a regex pattern.
   * <p>
   * The asterisk (<code>*</code>) matches zero or more characters.<br />
   * The question mark (<code>?</code>) matches one character.<br />
   * <p>
   * The returned pattern will be setup to match a whole string using
   * <code>^</code> and <code>$</code>.
   * 
   * @param text  the text to match, not null
   * @return the pattern, not null
   */
  public static Pattern wildcardsToPattern(final String text) {
    ArgumentChecker.notNull(text, "text");
    StringTokenizer tkn = new StringTokenizer(text, "?*", true);
    StringBuilder buf = new StringBuilder(text.length() + 10);
    buf.append('^');
    boolean lastStar = false;
    while (tkn.hasMoreTokens()) {
      String str = tkn.nextToken();
      if (str.equals("?")) {
        buf.append('.');
        lastStar = false;
      } else if (str.equals("*")) {
        if (lastStar == false) {
          buf.append(".*");
        }
        lastStar = true;
      } else {
        buf.append(Pattern.quote(str));
        lastStar = false;
      }
    }
    buf.append('$');
    return Pattern.compile(buf.toString(), Pattern.CASE_INSENSITIVE);
  }

  /**
   * Checks if a string matches a potentially wildcard string.
   * <p>
   * The asterisk (<code>*</code>) matches zero or more characters.<br />
   * The question mark (<code>?</code>) matches one character.<br />
   * 
   * @param searchCriteriaWithWildcard  the search criteria text with wildcards, null returns false
   * @param textToMatchAgainst  the text without wildcards to match against, null returns false
   * @return true if the text
   */
  public static boolean wildcardMatch(final String searchCriteriaWithWildcard, final String textToMatchAgainst) {
    if (searchCriteriaWithWildcard == null || textToMatchAgainst == null) {
      return false;
    }
    return wildcardsToPattern(searchCriteriaWithWildcard).matcher(textToMatchAgainst).matches();
  }
  
  /**
   * Determine whether the given string contains a wildcard.
   * 
   * @param searchCriteria The string to check
   * @return true if either <code>*</code> or <code>?</code> is present
   */
  public static boolean containsWildcard(final String searchCriteria) {
    ArgumentChecker.notNull(searchCriteria, "searchCriteria");
    return searchCriteria.contains("*") || searchCriteria.contains("?");
  }

  /**
   * Extracts first group from matched regex
   * @param string input string
   * @param regex regex string
   * @return extracted text
   */
  public static String extract(String string, String regex) {
    return extract(string, Pattern.compile(regex), 1);
  }
  
  /**
   * Extracts given group from matched regex
   * @param string input string
   * @param regex regex string
   * @param group group index
   * @return extracted text
   */
  public static String extract(String string, String regex, int group) {
    return extract(string, Pattern.compile(regex), group);
  }

  /**
   * Extracts given group from matched regex
   * @param string input string
   * @param pattern pattern object
   * @param group group index
   * @return extracted text
   */
  public static String extract(String string, Pattern pattern, int group) {
    Matcher m = pattern.matcher(string);
    if (m.find()) {
      return m.group(group);
    }
    return null;
  }

  /**
   * Returns true if given input string matches given pattern
   * @param input the input
   * @param pattern the pattern
   * @return true if given input string matches given pattern
   */
  public static boolean matches(String input, Pattern pattern) {    
    Matcher m = pattern.matcher(input);
    return m.matches();    
  }

}
