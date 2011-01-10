/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

/**
 * 
 *
 * @author jim
 */
public class ObjectTimeSeriesOperators {
  public interface BinaryOperator<T> {
    public T operate(T a, T b);
  }
  public interface UnaryOperator<T> {
    public T operate(T a);
  }
  
  private static class FirstOperator<E> implements BinaryOperator<E> {
    public E operate(final E a, final E b) { 
      return a;
    }
  }
  
  private static class SecondOperator<E> implements BinaryOperator<E> {
    public E operate(final E a, final E b) { 
      return b;
    }
  }
  
  private static final FirstOperator<?> FIRST_OPERATOR = new FirstOperator<Object>();
  private static final SecondOperator<?> SECOND_OPERATOR = new SecondOperator<Object>();
  
  @SuppressWarnings("unchecked")
  public static <E> FirstOperator<E> makeFirstOperator() {
    return (FirstOperator<E>) FIRST_OPERATOR;
  }
  
  @SuppressWarnings("unchecked")
  public static <E> SecondOperator<E> makeSecondOperator() {
    return (SecondOperator<E>) SECOND_OPERATOR;
  }
}
