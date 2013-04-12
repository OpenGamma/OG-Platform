/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Unary and binary operators for time-series.
 */
public class ObjectTimeSeriesOperators {

  /**
   * Binary operator to return the first parameter.
   * 
   * @param <R>  the object type
   * @return the operator, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> BinaryOperator<R> firstOperator() {
    return (BinaryOperator<R>) FIRST_OPERATOR;
  }

  /**
   * Binary operator to return the second parameter.
   * 
   * @param <R>  the object type
   * @return the operator, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> BinaryOperator<R> secondOperator() {
    return (BinaryOperator<R>) SECOND_OPERATOR;
  }

  /**
   * Binary operator to prevent intersection.
   * 
   * @param <R>  the object type
   * @return the operator, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> BinaryOperator<R> noIntersectionOperator() {
    return (BinaryOperator<R>) NO_INTERSECTION_OPERATOR;
  }

  //-------------------------------------------------------------------------
  /**
   * A binary operator takes two parameters and produces a single result.
   * For example, the plus, minus and multiply operators are binary.
   * 
   * @param <T> the object type
   */
  public interface BinaryOperator<T> {
    /**
     * Performs an operation on the input to produce an output.
     * 
     * @param a  the first parameter
     * @param b  the second parameter
     * @return the result
     */
    T operate(T a, T b);
  }

  //-------------------------------------------------------------------------
  /**
   * A unary operator takes a single parameter and produces a result.
   * For example, the increment and decrement operators are unary.
   * 
   * @param <T> the object type
   */
  public interface UnaryOperator<T> {
    /**
     * Performs an operation on the input to produce an output.
     * 
     * @param a  the input parameter
     * @return the result
     */
    T operate(T a);
  }

  //-------------------------------------------------------------------------
  private static final FirstOperator<?> FIRST_OPERATOR = new FirstOperator<Object>();
  private static class FirstOperator<E> implements BinaryOperator<E> {
    public E operate(E a, E b) { 
      return a;
    }
  }

  private static final SecondOperator<?> SECOND_OPERATOR = new SecondOperator<Object>();
  private static class SecondOperator<E> implements BinaryOperator<E> {
    public E operate(E a, E b) { 
      return b;
    }
  }

  private static final NoIntersectionOperator<?> NO_INTERSECTION_OPERATOR = new NoIntersectionOperator<Object>();
  private static class NoIntersectionOperator<E> implements BinaryOperator<E> {
    public E operate(E a, E b) {
      throw new IllegalStateException("No binary operation permitted");
    }
  }

}
