/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.sensitivity.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class HullWhiteOneFactorProviderDiscount implements HullWhiteOneFactorProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderDiscount _multicurveProvider;
  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyHW;

  /**
   * Constructor from exiting multicurveProvider and Hull-White parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider.
   * @param parameters The Hull-White one factor parameters.
   * @param ccyHW The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  public HullWhiteOneFactorProviderDiscount(final MulticurveProviderDiscount multicurves, HullWhiteOneFactorPiecewiseConstantParameters parameters, Currency ccyHW) {
    _multicurveProvider = multicurves;
    _parameters = parameters;
    _ccyHW = ccyHW;
  }

  @Override
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameters() {
    return _parameters;
  }

  @Override
  public Currency getHullWhiteCurrency() {
    return _ccyHW;
  }

  @Override
  public Integer getNumberOfParameters(String name) {
    return _multicurveProvider.getNumberOfParameters(name);
  }

  @Override
  public HullWhiteOneFactorProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = _multicurveProvider.copy();
    return new HullWhiteOneFactorProviderDiscount(multicurveProvider, _parameters, _ccyHW);
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return _multicurveProvider;
  }

  //     =====     Methods related to MulticurveProvider     =====

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    return _multicurveProvider.getDiscountFactor(ccy, time);
  }

  @Override
  public String getName(final Currency ccy) {
    return _multicurveProvider.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _multicurveProvider.getCurrencies();
  }

  @Override
  public double[] parameterSensitivity(final Currency ccy, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(ccy, pointSensitivity);
  }

  @Override
  public int getNumberOfParameters(final Currency ccy) {
    return _multicurveProvider.getNumberOfParameters(ccy);
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IborIndex index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _multicurveProvider.getIndexesIbor();
  }

  @Override
  public double[] parameterSensitivity(final IborIndex index, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(index, pointSensitivity);
  }

  @Override
  public int getNumberOfParameters(final IborIndex index) {
    return _multicurveProvider.getNumberOfParameters(index);
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IndexON index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _multicurveProvider.getIndexesON();
  }

  @Override
  public double[] parameterSensitivity(final IndexON index, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(index, pointSensitivity);
  }

  @Override
  public int getNumberOfParameters(final IndexON index) {
    return _multicurveProvider.getNumberOfParameters(index);
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
    return _multicurveProvider.getCurve(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    return _multicurveProvider.getCurve(index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    return _multicurveProvider.getCurve(index);
  }

  @Override
  /**
   * Returns all curves names. The order is the natural order of String.
   */
  public Set<String> getAllNames() {
    return _multicurveProvider.getAllNames();
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(ccy, curve);
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(index, curve);
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(index, curve);
  }

  /**
   * Set all the curves contains in another provider. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other provider.
   * TODO: REVIEW: Should we check that the curve are already present? Should we update the HW parameters.
   */
  public void setAll(final HullWhiteOneFactorProviderDiscount other) {
    ArgumentChecker.notNull(other, "Inflation provider");
    _multicurveProvider.setAll(other.getMulticurveProvider());
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   *  @throws IllegalArgumentException if curve name NOT already present 
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(index, curve);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _multicurveProvider.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  @Override
  public FXMatrix getFxRates() {
    return _multicurveProvider.getFxRates();
  }

}
