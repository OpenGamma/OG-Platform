/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.util.ArgumentChecker;

/**
 * Always returns the same name.
 */
public class StaticNameGenerator implements NameGenerator {

  private final String _name;

  public StaticNameGenerator(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  @Override
  public String createName() {
    return _name;
  }

}
