/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * An inflation provider where the underlying multi-curve provider is overridden by another multi-curve provider.
 */
public class InflationProviderDecoratedMulticurve implements InflationProviderInterface {
  
  /** The base inflation provider. All the inflation information will come from this provider. */
  private final InflationProviderInterface _inflationBase;
  /** The multi-curve provider decorating the inflation. All the information regarding discounting and forward 
   * rates will come from this provider. */
  private final MulticurveProviderInterface _multicurveDecorating;
  
  /**
   * @param inflationBase The base inflation provider. All the inflation information will come from this provider.
   * @param multicurveDecorating The multi-curve provider decorating the inflation. All the information regarding 
   * discounting and forward rates will come from this provider.
   */
  public InflationProviderDecoratedMulticurve(InflationProviderInterface inflationBase, 
      MulticurveProviderInterface multicurveDecorating) {
    _inflationBase = inflationBase;
    _multicurveDecorating = multicurveDecorating;
  }

  @Override
  public InflationProviderInterface getInflationProvider() {
    return this;
  }

  @Override
  public double[] parameterInflationSensitivity(String name, List<DoublesPair> pointSensitivity) {
    return _inflationBase.parameterInflationSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterSensitivity(String name, List<DoublesPair> pointSensitivity) {
    return _multicurveDecorating.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(String name, List<ForwardSensitivity> pointSensitivity) {
    return _multicurveDecorating.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    throw new UnsupportedOperationException("not supported for decorated providers"); //TODO
  }

  @Override
  public InflationProviderInterface copy() {
    throw new UnsupportedOperationException("Copy not supported for decorated providers");
  }

  @Override
  public double getPriceIndex(IndexPrice index, Double time) {
    return _inflationBase.getPriceIndex(index, time);
  }

  @Override
  public String getName(IndexPrice index) {
    return _inflationBase.getName(index);
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _inflationBase.getPriceIndexes();
  }

  @Override
  @Deprecated
  public Set<String> getAllNames() {
    throw new UnsupportedOperationException("not supported for decorated providers"); //TODO
  }

  @Override
  public Integer getNumberOfParameters(String name) {
    throw new UnsupportedOperationException("not supported for decorated providers"); //TODO
  }

  @Override
  public List<String> getUnderlyingCurvesNames(String name) {
    throw new UnsupportedOperationException("not supported for decorated providers"); //TODO
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveDecorating;
  }

  @Override
  public double getDiscountFactor(Currency ccy, Double time) {
    return _multicurveDecorating.getDiscountFactor(ccy, time);
  }

  @Override
  public double getForwardRate(IborIndex index, double startTime, double endTime, double accrualFactor) {
    return _multicurveDecorating.getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getForwardRate(IndexON index, double startTime, double endTime, double accrualFactor) {
    return _multicurveDecorating.getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getFxRate(Currency ccy1, Currency ccy2) {
    return _multicurveDecorating.getFxRate(ccy1, ccy2);
  }

  @Override
  public String getName(Currency ccy) {
    return _multicurveDecorating.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _multicurveDecorating.getCurrencies();
  }

  @Override
  public String getName(IborIndex index) {
    return _multicurveDecorating.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _multicurveDecorating.getIndexesIbor();
  }

  @Override
  public String getName(IndexON index) {
    return _multicurveDecorating.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _multicurveDecorating.getIndexesON();
  }

  @Override
  public FXMatrix getFxRates() {
    return _multicurveDecorating.getFxRates();
  }

  @Override
  public InflationProviderInterface withDiscountFactor(Currency ccy, YieldAndDiscountCurve replacement) {
    throw new UnsupportedOperationException("withDiscountFactor not supported for decorated providers");
  }

  @Override
  public InflationProviderInterface withForward(IborIndex index, YieldAndDiscountCurve replacement) {
    throw new UnsupportedOperationException("withForward not supported for decorated providers");
  }

  @Override
  public InflationProviderInterface withForward(IndexON index, YieldAndDiscountCurve replacement) {
    throw new UnsupportedOperationException("withForward not supported for decorated providers");
  }
  
}
