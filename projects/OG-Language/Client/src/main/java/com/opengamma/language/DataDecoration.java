/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language;

import com.opengamma.util.ArgumentChecker;

/**
 * Decoration metadata for language specific extensions to {@link Data} objects.
 */
public abstract class DataDecoration {

  private final DataDecorator<DataDecoration> _decorator;

  @SuppressWarnings("unchecked")
  protected DataDecoration(final DataDecorator<? extends DataDecoration> decorator) {
    ArgumentChecker.notNull(decorator, "decorator");
    _decorator = (DataDecorator<DataDecoration>) decorator;
  }

  protected DataDecorator<DataDecoration> getDecorator() {
    return _decorator;
  }

  public final Data applyTo(Data data) {
    return getDecorator().applyTo(this, data);
  }

}
