/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

/**
 * Interface defining the manipulation of a structured object (yield curve, vol surface etc) to be
 * undertaken.
 *
 * @param <T> the type of structure (yield curve, vol surface etc)
 */
public interface StructureManipulator<T> {

  /**
   * Transforms a structured object into another structured object of the same type but with the
   * values manipulated in some way. The input object should be unaltered and a new object output.
   *
   * For example, take a YieldCurve and shift it by 10%.
   *
   * @param structure the structured object to transform
   * @return a transformed structure
   */
  T execute(T structure);
}
