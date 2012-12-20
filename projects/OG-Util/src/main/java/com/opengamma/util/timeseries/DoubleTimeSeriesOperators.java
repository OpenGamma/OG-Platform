/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.OpenGammaRuntimeException;


/**
 * 
 * 
 */
@SuppressWarnings("synthetic-access")
public class DoubleTimeSeriesOperators {
  /**
   * Interface that should be satisfied by all binary operators. Public
   * so that it's possible
   * to provide custom operations to @method operate and @method
   * unionOperate
   */
  public interface BinaryOperator {
    double operate(double a, double b);
  }

  private static class AddOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a + b;
    }
  }

  private static class SubtractOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a - b;
    }
  }

  private static class MultiplyOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a * b;
    }
  }

  private static class DivideOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a / b;
    }
  }

  private static class PowerOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return Math.pow(a, b);
    }
  }

  private static class MinimumOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return Math.min(a, b);
    }
  }

  private static class MaximumOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return Math.max(a, b);
    }
  }

  private static class AverageOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return (a + b) / 2;
    }
  }

  private static class FirstOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return a;
    }
  }

  private static class SecondOperator implements BinaryOperator {
    public double operate(final double a, final double b) {
      return b;
    }
  }
  
  private static class NoIntersectionOperator implements BinaryOperator {
    public double operate(double a, double b) {
      throw new OpenGammaRuntimeException("No binary operation permitted");
    }
    
  }

  /**
   *         Interface to be implemented by any unary operation on time series.
   *         Public so
   *         that custom implementations can be passed to @method operate.
   */
  public interface UnaryOperator {
    double operate(double a);
  }

  private static class ReciprocalOperator implements UnaryOperator {
    public double operate(final double a) {
      return 1 / a;
    }
  }

  private static class NegateOperator implements UnaryOperator {
    public double operate(final double a) {
      return -a;
    }
  }

  private static class LogOperator implements UnaryOperator {
    public double operate(final double a) {
      return Math.log(a);
    }
  }

  private static class Log10Operator implements UnaryOperator {
    public double operate(final double a) {
      return Math.log10(a);
    }
  }

  private static class AbsOperator implements UnaryOperator {
    public double operate(final double a) {
      return Math.abs(a);
    }
  }
  
  

  // CSOFF: Self-Explainatory
  public static final AddOperator ADD_OPERATOR = new AddOperator();
  public static final SubtractOperator SUBTRACT_OPERATOR = new SubtractOperator();
  public static final MultiplyOperator MULTIPLY_OPERATOR = new MultiplyOperator();
  public static final DivideOperator DIVIDE_OPERATOR = new DivideOperator();
  public static final PowerOperator POWER_OPERATOR = new PowerOperator();
  public static final MinimumOperator MINIMUM_OPERATOR = new MinimumOperator();
  public static final MaximumOperator MAXIMUM_OPERATOR = new MaximumOperator();
  public static final AverageOperator AVERAGE_OPERATOR = new AverageOperator();
  public static final FirstOperator FIRST_OPERATOR = new FirstOperator();
  public static final SecondOperator SECOND_OPERATOR = new SecondOperator();
  public static final ReciprocalOperator RECIPROCAL_OPERATOR = new ReciprocalOperator();
  public static final NegateOperator NEGATE_OPERATOR = new NegateOperator();
  public static final LogOperator LOG_OPERATOR = new LogOperator();
  public static final Log10Operator LOG10_OPERATOR = new Log10Operator();
  public static final AbsOperator ABS_OPERATOR = new AbsOperator();
  public static final NoIntersectionOperator NO_INTERSECTION_OPERATOR = new NoIntersectionOperator();
  // CSON: Self-Explainatory
}
