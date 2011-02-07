/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import java.io.Serializable;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public abstract class DoubleFunction1D extends Function1D<Double, Double> {
  private static final double EPS = 1e-12;

  public DoubleFunction1D derivative() {
    class A extends DoubleFunction1D implements Serializable {

      @Override
      public Double evaluate(final Double x) {
        return (DoubleFunction1D.this.evaluate(x + EPS) - DoubleFunction1D.this.evaluate(x - EPS)) / 2 / EPS;
      }

    }
    return new A();
  }

  public DoubleFunction1D add(final DoubleFunction1D f) {
    Validate.notNull(f, "f");
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) + f.evaluate(x);
      }

    };
  }

  public DoubleFunction1D add(final double a) {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) + a;
      }

    };
  }

  public DoubleFunction1D divide(final DoubleFunction1D f) {
    Validate.notNull(f, "f");
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) / f.evaluate(x);
      }

    };
  }

  public DoubleFunction1D divide(final double a) {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) / a;
      }

    };
  }

  public DoubleFunction1D multiply(final DoubleFunction1D f) {
    Validate.notNull(f, "f");
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) * f.evaluate(x);
      }

    };
  }

  public DoubleFunction1D multiply(final double a) {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) * a;
      }

    };
  }

  public DoubleFunction1D subtract(final DoubleFunction1D f) {
    Validate.notNull(f, "f");
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) - f.evaluate(x);
      }

    };
  }

  public DoubleFunction1D subtract(final double a) {
    return new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        return DoubleFunction1D.this.evaluate(x) - a;
      }

    };
  }
}
