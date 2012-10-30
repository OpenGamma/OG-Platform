/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.text;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for creating a single string from a fixed number of others with
 * correct escape characters.
 */
public class CompositeStringUtil {

  /**
   * Default separator character.
   */
  public static final char DEFAULT_SEPARATOR = '_';

  /**
   * Default escape character.
   */
  public static final char DEFAULT_ESCAPE = '$';

  private final int _numComponents;
  private final char[] _separators;
  private final char[] _escapes;

  /**
   * Creates a new composite string utility.
   * 
   * @param numComponents number of string components
   * @param escapeLast true if the last component is also escaped, false to use the
   *        whole tail of the string "as-is" for the final component
   */
  public CompositeStringUtil(final int numComponents, final boolean escapeLast) {
    ArgumentChecker.notNegativeOrZero(numComponents, "numComponents");
    _numComponents = numComponents;
    final int len = escapeLast ? numComponents : (numComponents - 1);
    _separators = new char[len];
    _escapes = new char[len];
    for (int i = 0; i < len; i++) {
      _separators[i] = DEFAULT_SEPARATOR;
      _escapes[i] = DEFAULT_ESCAPE;
    }
  }

  public void setSeparators(final char... chars) {
    final char[] dest = getSeparators();
    ArgumentChecker.isTrue(chars.length == dest.length, "chars");
    System.arraycopy(chars, 0, dest, 0, chars.length);
  }

  public void setEscapeCharacters(final char... chars) {
    final char[] dest = getEscapes();
    ArgumentChecker.isTrue(chars.length == dest.length, "chars");
    System.arraycopy(chars, 0, dest, 0, chars.length);
  }

  protected char[] getSeparators() {
    return _separators;
  }

  protected char[] getEscapes() {
    return _escapes;
  }

  public int getNumComponents() {
    return _numComponents;
  }

  public boolean isEscapeLast() {
    return getNumComponents() == getEscapes().length;
  }

  private static void append(final StringBuilder sb, final char separator, final char escape, final String component) {
    if ((component.indexOf(separator) < 0) && (component.indexOf(escape) < 0)) {
      sb.append(component);
    } else {
      for (int j = 0; j < component.length(); j++) {
        final char c = component.charAt(j);
        if (c == separator) {
          sb.append(escape).append(c);
        } else if (c == escape) {
          sb.append(c).append(c);
        } else {
          sb.append(c);
        }
      }
    }
  }

  public String create(final String... components) {
    ArgumentChecker.isTrue(components.length == getNumComponents(), "components");
    final StringBuilder sb = new StringBuilder();
    int i;
    for (i = 0; i < components.length - 1; i++) {
      final char separator = getSeparators()[i];
      append(sb, separator, getEscapes()[i], components[i]);
      sb.append(separator);
    }
    if (isEscapeLast()) {
      append(sb, getSeparators()[i], getEscapes()[i], components[i]);
    } else {
      sb.append(components[i]);
    }
    return sb.toString();
  }

  public String[] parse(final String composite) {
    final String[] result = new String[getNumComponents()];
    final StringBuilder sb = new StringBuilder();
    int component = 0;
    char separator = getSeparators()[component];
    char escape = getEscapes()[component];
    for (int i = 0; i < composite.length(); i++) {
      final char c = composite.charAt(i);
      if (c == separator) {
        result[component++] = sb.toString();
        sb.delete(0, sb.length());
        if (component < getSeparators().length) {
          if (component < getNumComponents()) {
            separator = getSeparators()[component];
            escape = getEscapes()[component];
          } else {
            return result;
          }
        } else {
          if (component < getNumComponents()) {
            // Unescaped last component
            result[component] = composite.substring(i + 1);
          }
          return result;
        }
      } else if (c == escape) {
        if ((++i) >= composite.length()) {
          sb.append(c);
        } else {
          sb.append(composite.charAt(i));
        }
      } else {
        sb.append(c);
      }
    }
    result[component] = sb.toString();
    return result;
  }

  public boolean validate(final String[] parsed) {
    for (int i = 0; i < parsed.length; i++) {
      if (parsed[i] == null) {
        return false;
      }
    }
    return true;
  }

}
