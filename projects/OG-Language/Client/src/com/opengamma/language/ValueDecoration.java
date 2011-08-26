/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language;

import com.opengamma.util.ArgumentChecker;

/**
 * Decoration metadata for language specific extensions to {@link Value} objects.
 */
public abstract class ValueDecoration {

  private final ValueDecorator<ValueDecoration> _decorator;

  @SuppressWarnings("unchecked")
  protected ValueDecoration(final ValueDecorator<? extends ValueDecoration> decorator) {
    ArgumentChecker.notNull(decorator, "decorator");
    _decorator = (ValueDecorator<ValueDecoration>) decorator;
  }

  protected ValueDecorator<ValueDecoration> getDecorator() {
    return _decorator;
  }

  public final Value applyTo(Value value) {
    return getDecorator().applyTo(this, value);
  }

}
