/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.install.feedback;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appender that will route messages back to the installation scripts.
 */
public class Logback<E> extends UnsynchronizedAppenderBase<E> {

  private final Feedback _feedback = new Feedback();
  
  private Layout<E> _layout;

  /**
   * Gets the layout.
   * @return the layout
   */
  public Layout<E> getLayout() {
    return _layout;
  }

  /**
   * Sets the layout.
   * @param layout  the layout
   */
  public void setLayout(Layout<E> layout) {
    _layout = layout;
  }

  @Override
  protected void append(final E eventObject) {
    if (isStarted()) {
      final String text = getLayout().doLayout(eventObject);
      synchronized (this) {
        _feedback.report(text);
      }
    }
  }

}
