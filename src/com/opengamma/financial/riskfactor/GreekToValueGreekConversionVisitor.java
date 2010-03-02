/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import com.opengamma.financial.greeks.AbstractGreekVisitor;

/**
 * @author emcleod
 * 
 */
public class GreekToValueGreekConversionVisitor extends AbstractGreekVisitor<Double> {
  private final ValueGreekDataBundle _data;

  public GreekToValueGreekConversionVisitor(final ValueGreekDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data bundle was null");
    _data = data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDelta()
   */
  @Override
  public Double visitDelta() {
    final Double s = _data.getDataForType(ValueGreekDataBundle.DataType.UNDERLYING_PRICE);
    if (s == null)
      throw new IllegalArgumentException("Could not get spot price");
    final Double pv = _data.getDataForType(ValueGreekDataBundle.DataType.OPTION_POINT_VALUE);
    if (pv == null)
      throw new IllegalArgumentException("Could not get option point value");
    final Double n = _data.getDataForType(ValueGreekDataBundle.DataType.NUMBER_OF_CONTRACTS);
    if (n == null)
      throw new IllegalArgumentException("Could not get number of contracts");
    return s * pv * n;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGamma()
   */
  @Override
  public Double visitGamma() {
    final Double s = _data.getDataForType(ValueGreekDataBundle.DataType.UNDERLYING_PRICE);
    if (s == null)
      throw new IllegalArgumentException("Could not get spot price");
    final Double pv = _data.getDataForType(ValueGreekDataBundle.DataType.OPTION_POINT_VALUE);
    if (pv == null)
      throw new IllegalArgumentException("Could not get option point value");
    final Double n = _data.getDataForType(ValueGreekDataBundle.DataType.NUMBER_OF_CONTRACTS);
    if (n == null)
      throw new IllegalArgumentException("Could not get number of contracts");
    return s * s * pv * n;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitPrice()
   */
  @Override
  public Double visitPrice() {
    final Double s = _data.getDataForType(ValueGreekDataBundle.DataType.UNDERLYING_PRICE);
    if (s == null)
      throw new IllegalArgumentException("Could not get spot price");
    final Double pv = _data.getDataForType(ValueGreekDataBundle.DataType.OPTION_POINT_VALUE);
    if (pv == null)
      throw new IllegalArgumentException("Could not get option point value");
    final Double n = _data.getDataForType(ValueGreekDataBundle.DataType.NUMBER_OF_CONTRACTS);
    if (n == null)
      throw new IllegalArgumentException("Could not get number of contracts");
    return pv * n;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitRho()
   */
  @Override
  public Double visitRho() {
    final Double pv = _data.getDataForType(ValueGreekDataBundle.DataType.OPTION_POINT_VALUE);
    if (pv == null)
      throw new IllegalArgumentException("Could not get option point value");
    final Double n = _data.getDataForType(ValueGreekDataBundle.DataType.NUMBER_OF_CONTRACTS);
    if (n == null)
      throw new IllegalArgumentException("Could not get number of contracts");
    return pv * n;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitTheta()
   */
  @Override
  public Double visitTheta() {
    final Double pv = _data.getDataForType(ValueGreekDataBundle.DataType.OPTION_POINT_VALUE);
    if (pv == null)
      throw new IllegalArgumentException("Could not get option point value");
    final Double n = _data.getDataForType(ValueGreekDataBundle.DataType.NUMBER_OF_CONTRACTS);
    if (n == null)
      throw new IllegalArgumentException("Could not get number of contracts");
    return pv * n;
  }
}
