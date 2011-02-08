/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.apache.commons.lang.Validate;

import com.opengamma.math.differentiation.FiniteDifferenceType;

/**
 * 
 */
public abstract class DoubleFunction1D extends Function1D<Double, Double> {
  private static final double EPS = 1e-5;

  public DoubleFunction1D derivative() {
    return derivative(FiniteDifferenceType.CENTRAL, EPS);
  }

  public DoubleFunction1D derivative(final FiniteDifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType, "difference type");
    switch (differenceType) {
      case CENTRAL:
        return new DoubleFunction1D() {

          @Override
          public Double evaluate(final Double x) {
            return (DoubleFunction1D.this.evaluate(x + eps) - DoubleFunction1D.this.evaluate(x - eps)) / 2 / eps;
          }

        };
      case BACKWARD:
        return new DoubleFunction1D() {

          @Override
          public Double evaluate(final Double x) {
            return (DoubleFunction1D.this.evaluate(x) - DoubleFunction1D.this.evaluate(x - eps)) / eps;
          }

        };
      case FORWARD:
        return new DoubleFunction1D() {

          @Override
          public Double evaluate(final Double x) {
            return (DoubleFunction1D.this.evaluate(x + eps) - DoubleFunction1D.this.evaluate(x)) / eps;
          }

        };
      default:
        throw new IllegalArgumentException("Unhandled FiniteDifferenceType " + differenceType);
    }
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
