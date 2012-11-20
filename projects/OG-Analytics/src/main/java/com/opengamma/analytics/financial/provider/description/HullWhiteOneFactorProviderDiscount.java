/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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
  public HullWhiteOneFactorProviderDiscount copy() {
    MulticurveProviderDiscount multicurveProvider = _multicurveProvider.copy();
    return new HullWhiteOneFactorProviderDiscount(multicurveProvider, _parameters, _ccyHW);
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return _multicurveProvider;
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

  public HullWhiteOneFactorProviderDiscount withDiscountFactor(Currency ccy, YieldAndDiscountCurve replacement) {
    MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withDiscountFactor(ccy, replacement);
    return new HullWhiteOneFactorProviderDiscount(decoratedMulticurve, _parameters, _ccyHW);
  }

  public HullWhiteOneFactorProviderDiscount withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withForward(index, replacement);
    return new HullWhiteOneFactorProviderDiscount(decoratedMulticurve, _parameters, _ccyHW);
  }

  public HullWhiteOneFactorProviderDiscount withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withForward(index, replacement);
    return new HullWhiteOneFactorProviderDiscount(decoratedMulticurve, _parameters, _ccyHW);
  }

}
