/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.text;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.util.ArgumentChecker;

/**
 * Maps ordinals to a text description.
 */
public final class Ordinal {

  // TODO: replace this if there is a standard library somewhere else that can do it

  private static final Ordinal s_instance = new Ordinal();

  private final String _default;
  private final Map<Integer, String> _words = new HashMap<Integer, String>();

  private Ordinal() {
    final ResourceBundle resource = ResourceBundle.getBundle(Ordinal.class.getName());
    String defaultSuffix = null;
    for (String key : resource.keySet()) {
      final String value = resource.getString(key);
      if ("default".equals(key)) {
        defaultSuffix = value;
      } else {
        _words.put(Integer.parseInt(key), value);
      }
    }
    ArgumentChecker.notNull(defaultSuffix, "default");
    _default = defaultSuffix;
  }

  private String getImpl(final int n) {
    final String value = _words.get(n);
    if (value != null) {
      return value;
    } else {
      return n + _default;
    }
  }

  public static String get(final int n) {
    return s_instance.getImpl(n);
  }

}
