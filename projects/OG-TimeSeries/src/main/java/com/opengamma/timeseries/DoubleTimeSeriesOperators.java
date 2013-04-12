/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Unary and binary operators for time-series.
 */
@SuppressWarnings("synthetic-access")
public class DoubleTimeSeriesOperators {

  /**
   * Binary operator to add the input parameters.
   */
  public static final BinaryOperator ADD_OPERATOR = new AddOperator();
  /**
   * Binary operator to subtract the second parameter from the first.
   */
  public static final BinaryOperator SUBTRACT_OPERATOR = new SubtractOperator();
  /**
   * Binary operator to multiply the input parameters.
   */
  public static final BinaryOperator MULTIPLY_OPERATOR = new MultiplyOperator();
  /**
   * Binary operator to divide the first parameter by the second.
   */
  public static final BinaryOperator DIVIDE_OPERATOR = new DivideOperator();
  /**
   * Binary operator to raise the first parameter to the power of the second parameter.
   */
  public static final BinaryOperator POWER_OPERATOR = new PowerOperator();
  /**
   * Binary operator to return the minimum of the two input parameters.
   */
  public static final BinaryOperator MINIMUM_OPERATOR = new MinimumOperator();
  /**
   * Binary operator to return the maximum of the two input parameters.
   */
  public static final BinaryOperator MAXIMUM_OPERATOR = new MaximumOperator();
  /**
   * Binary operator to return the average of the two input parameters.
   */
  public static final BinaryOperator AVERAGE_OPERATOR = new AverageOperator();
  /**
   * Binary operator to return the first parameter.
   */
  public static final BinaryOperator FIRST_OPERATOR = new FirstOperator();
  /**
   * Binary operator to return the second parameter.
   */
  public static final BinaryOperator SECOND_OPERATOR = new SecondOperator();
  /**
   * Binary operator that always throws an exception.
   */
  public static final BinaryOperator NO_INTERSECTION_OPERATOR = new NoIntersectionOperator();

  /**
   * Unary operator that returns the reciprocal of the input parameter.
   */
  public static final UnaryOperator RECIPROCAL_OPERATOR = new ReciprocalOperator();
  /**
   * Unary operator that returns the negation of the input parameter.
   */
  public static final UnaryOperator NEGATE_OPERATOR = new NegateOperator();
  /**
   * Unary operator that returns the log of the input parameter.
   */
  public static final UnaryOperator LOG_OPERATOR = new LogOperator();
  /**
   * Unary operator that returns the log 10 of the input parameter.
   */
  public static final UnaryOperator LOG10_OPERATOR = new Log10Operator();
  /**
   * Unary operator that returns the absolute value of the input parameter.
   */
  public static final UnaryOperator ABS_OPERATOR = new AbsOperator();

  //-------------------------------------------------------------------------
  /**
   * A binary operator takes two parameters and produces a single result.
   * For example, the plus, minus and multiply operators are binary.
   */
  public interface BinaryOperator {
    /**
     * Performs an operation on the input to produce an output.
     * 
     * @param a  the first parameter
     * @param b  the second parameter
     * @return the result
     */
    double operate(double a, double b);
  }

  //-------------------------------------------------------------------------
  private static class AddOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a + b;
    }
  }

  private static class SubtractOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a - b;
    }
  }

  private static class MultiplyOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a * b;
    }
  }

  private static class DivideOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a / b;
    }
  }

  private static class PowerOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return Math.pow(a, b);
    }
  }

  private static class MinimumOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return Math.min(a, b);
    }
  }

  private static class MaximumOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return Math.max(a, b);
    }
  }

  private static class AverageOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return (a + b) / 2;
    }
  }

  private static class FirstOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return a;
    }
  }

  private static class SecondOperator implements BinaryOperator {
    public double operate(double a, double b) {
      return b;
    }
  }
  
  private static class NoIntersectionOperator implements BinaryOperator {
    public double operate(double a, double b) {
      throw new IllegalStateException("No binary operation permitted");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A unary operator takes a single parameter and produces a result.
   * For example, the increment and decrement operators are unary.
   */
  public interface UnaryOperator {
    /**
     * Performs an operation on the input to produce an output.
     * 
     * @param a  the input parameter
     * @return the result
     */
    double operate(double a);
  }

  //-------------------------------------------------------------------------
  private static class ReciprocalOperator implements UnaryOperator {
    public double operate(double a) {
      return 1 / a;
    }
  }

  private static class NegateOperator implements UnaryOperator {
    public double operate(double a) {
      return -a;
    }
  }

  private static class LogOperator implements UnaryOperator {
    public double operate(double a) {
      return Math.log(a);
    }
  }

  private static class Log10Operator implements UnaryOperator {
    public double operate(double a) {
      return Math.log10(a);
    }
  }

  private static class AbsOperator implements UnaryOperator {
    public double operate(double a) {
      return Math.abs(a);
    }
  }

}
