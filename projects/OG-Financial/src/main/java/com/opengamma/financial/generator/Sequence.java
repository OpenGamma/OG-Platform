/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

/**
 * Internal class for creating a predicatable sequence of values rather than a random set.
 */
/* package */class Sequence {

  public static final class Entry {

    private final int _sequence;
    private int _scale = 1;

    private Entry(final int sequence) {
      _sequence = sequence;
    }

    public int next(final int range) {
      final int value = (_sequence / _scale) % range;
      _scale *= range;
      return value;
    }

    public int getSequence() {
      return _sequence;
    }

  }

  private int _sequence;

  public Entry entry() {
    return new Entry(_sequence++);
  }

}
