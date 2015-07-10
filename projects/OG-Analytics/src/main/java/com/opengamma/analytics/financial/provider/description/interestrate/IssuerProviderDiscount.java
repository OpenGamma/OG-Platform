/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a provider with multi-curves and issuer specific curves.
 * The forward rate are computed as the ratio of discount factors stored in {@link YieldAndDiscountCurve}.
 */
public class IssuerProviderDiscount extends IssuerProvider {

  /**
   * Constructs an empty multi-curve provider and issuer curve map.
   */
  public IssuerProviderDiscount() {
    super();
  }

  /**
   * Constructs and empty multi-curve provider and issuer curve map.
   * @param fxMatrix The FX matrix, not null
   */
  public IssuerProviderDiscount(final FXMatrix fxMatrix) {
    super(fxMatrix);
  }

  /**
   * @param discountingCurves A map from currency to yield curve, not null
   * @param forwardIborCurves A map from ibor index to yield curve, not null
   * @param forwardONCurves A map from overnight index to yield curve, not null
   * @param fxMatrix The FX matrix, not null
   */
  public IssuerProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final FXMatrix fxMatrix) {
    super(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
  }

  /**
   * @param discountingCurves A map from currency to yield curve, not null
   * @param forwardIborCurves A map from ibor index to yield curve, not null
   * @param forwardONCurves A map from overnight index to yield curve, not null
   * @param issuerCurves The issuer curves, not null
   * @param fxMatrix The FX matrix, not null
   */
  public IssuerProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves, final FXMatrix fxMatrix) {
    super(discountingCurves, forwardIborCurves, forwardONCurves, issuerCurves, fxMatrix);
  }

  /**
   * Constructor from exiting multi-curve provider and issuer map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider.
   * @param issuerCurves The issuer specific curves.
   */
  public IssuerProviderDiscount(final MulticurveProviderDiscount multicurve, final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves) {
    super(multicurve, issuerCurves);
  }

  /**
   * Constructs a provider from an existing multi-curve provider. The maps are not copied
   * @param multicurve The multi-curve provider, notn ull
   */
  public IssuerProviderDiscount(final MulticurveProviderDiscount multicurve) {
    super(multicurve);
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return (MulticurveProviderDiscount) super.getMulticurveProvider();
  }

  @Override
  public IssuerProviderDiscount getIssuerProvider() {
    return this;
  }

  /**
   * Gets a named issuer curve.
   * @param name The name
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final String name) {
    if (this.getAllNames().contains(name)) {
      if (getMulticurveProvider().getAllCurveNames().contains(name)) {
        return getMulticurveProvider().getCurve(name);
      }
      return getIssuerProvider().getIssuerCurve(name);
    } else {
      throw new IllegalArgumentException("the following curve is not in the provider: " + name);
    }
  }

  @Override
  public IssuerProviderDiscount copy() {
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurvesNew = new HashMap<>(getIssuerCurves());
    return new IssuerProviderDiscount(getMulticurveProvider().copy(), issuerCurvesNew);
  }

  /**
   * Sets the discounting curve for a currency.
   * @param ccy The currency, not null
   * @param curve The curve, not null
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().setCurve(ccy, curve);
    // TODO: Should we make sure that we don't set the _multicurveProvider directly (without the list update)
    setAllCurves();
  }

  /**
   * Sets the forward curve for an ibor index.
   * @param index The index, not null
   * @param curve The curve, not null
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().setCurve(index, curve);
    setAllCurves();
  }

  /**
   * Sets the overnight curve for an overnight index.
   * @param index The index, not null
   * @param curve The curve, not null
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().setCurve(index, curve);
    setAllCurves();
  }

  /**
   * Set all the curves contains in another provider. If a currency, index, or issuer is already present in the map, the associated curve is changed.
   * @param other The other provider.
   */
  public void setAll(final IssuerProviderDiscount other) {
    ArgumentChecker.notNull(other, "other");
    getMulticurveProvider().setAll(other.getMulticurveProvider());
    getIssuerCurves().putAll(other.getIssuerCurves());
    setAllCurves();
  }

  @Override
  public IssuerProviderDiscount withIssuerCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> ic, final YieldAndDiscountCurve replacement) {
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> newIssuerCurves = new LinkedHashMap<>(getIssuerCurves());
    newIssuerCurves.put(ic, replacement);
    return new IssuerProviderDiscount(getMulticurveProvider(), newIssuerCurves);
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().replaceCurve(index, curve);
  }

  /**
   * Replaces the forward curve for a given ON index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    getMulticurveProvider().replaceCurve(index, curve);
  }
}
