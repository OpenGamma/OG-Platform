/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.install.feedback;

import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appender that will route messages back to the installation scripts.
 */
public class Logback<E> extends UnsynchronizedAppenderBase<E> {

  @Override
  protected void append(final E eventObject) {
    if (isStarted()) {
      final String text = getLayout().doLayout(eventObject);
      synchronized (this) {
        Feedback.status(text);
      }
    }
  }

}
