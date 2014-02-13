/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.commodity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.curve.CommodityForwardCurve;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a "market" with discounting, forward, forward commodity and credit curves.
 * The forward rate (for the discount curve only) are computed as the ratio of discount factors stored in {@link YieldAndDiscountCurve}.
 */
public class CommodityProviderDiscount implements CommodityProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderDiscount _multicurveProvider;
  /**
   * A map with one commodity forward curve by commodity underlying.
   */
  private final Map<CommodityUnderlying, CommodityForwardCurve> _commodityForwardCurves;

  /**
   * Map of all curves used in the provider. The order is ???
   */
  private Map<String, CommodityForwardCurve> _allCurves;

  /**
   * Constructor with empty maps for discounting, forward and commodity forward curves.
   */
  public CommodityProviderDiscount() {
    _multicurveProvider = new MulticurveProviderDiscount();
    _commodityForwardCurves = new LinkedHashMap<>();
    setCommodityForwardCurves();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix.
   */
  public CommodityProviderDiscount(final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(fxMatrix);
    _commodityForwardCurves = new LinkedHashMap<>();
    setCommodityForwardCurves();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardIborCurves A map with one (forward) curve by Ibor index.
   * @param forwardONCurves A map with one (forward) curve by ON index.
   * @param commodityForwardCurves A map with one price curve by price index.
   * @param fxMatrix The FXMatrix.
   */
  public CommodityProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<CommodityUnderlying, CommodityForwardCurve> commodityForwardCurves, final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(commodityForwardCurves, "commodityForwardCurves");
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _commodityForwardCurves = commodityForwardCurves;
    setCommodityForwardCurves();
  }

  /**
   * Constructor from exiting multicurveProvider and inflation map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider.
   * @param commodityForwardCurves The map with commodity forward curves.
   */
  public CommodityProviderDiscount(final MulticurveProviderDiscount multicurve, final Map<CommodityUnderlying, CommodityForwardCurve> commodityForwardCurves) {
    _multicurveProvider = multicurve;
    _commodityForwardCurves = commodityForwardCurves;
    setCommodityForwardCurves();
  }

  /**
   * Adds all commodity forward curves to a single map.
   */
  private void setCommodityForwardCurves() {
    _allCurves = new LinkedHashMap<>();

    final Set<CommodityUnderlying> indexSet = _commodityForwardCurves.keySet();
    for (final CommodityUnderlying index : indexSet) {
      final String name = _commodityForwardCurves.get(index).getName();
      _allCurves.put(name, _commodityForwardCurves.get(index));
    }

  }

  @Override
  public CommodityProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = _multicurveProvider.copy();
    final LinkedHashMap<CommodityUnderlying, CommodityForwardCurve> commodityForwardCurves = new LinkedHashMap<>(_commodityForwardCurves);
    return new CommodityProviderDiscount(multicurveProvider, commodityForwardCurves);
  }

  @Override
  public double getForwardValue(final CommodityUnderlying commodityUnderlying, final Double time) {
    if (_commodityForwardCurves.containsKey(commodityUnderlying)) {
      return _commodityForwardCurves.get(commodityUnderlying).getForwardValue(time);
    }
    throw new IllegalArgumentException("Price index curve not found: " + commodityUnderlying);
  }

  @Override
  public String getName(final CommodityUnderlying commodityUnderlying) {
    if (_commodityForwardCurves.containsKey(commodityUnderlying)) {
      return _commodityForwardCurves.get(commodityUnderlying).getFwdCurve().getName();
    }
    throw new IllegalArgumentException("Price index curve not found: " + commodityUnderlying);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public CommodityForwardCurve getCurve(final IndexPrice index) {
    if (_commodityForwardCurves.containsKey(index)) {
      return _commodityForwardCurves.get(index);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  @Override
  public Set<CommodityUnderlying> getCommodityUnderlyings() {
    return _commodityForwardCurves.keySet();
  }

  /**
   * Sets the price index curve for a price index.
   * @param commodityUnderlying The price index.
   * @param curve The curve.
   */
  public void setCurve(final CommodityUnderlying commodityUnderlying, final CommodityForwardCurve curve) {
    ArgumentChecker.notNull(commodityUnderlying, "commodity underlying");
    ArgumentChecker.notNull(curve, "curve");
    if (_commodityForwardCurves.containsKey(commodityUnderlying)) {
      throw new IllegalArgumentException("Price index curve already set: " + commodityUnderlying.toString());
    }
    _commodityForwardCurves.put(commodityUnderlying, curve);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    final CommodityForwardCurve curve = _allCurves.get(name);
    return curve.getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _allCurves.get(name).getUnderlyingCurvesNames();
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
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
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
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IndexON index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _multicurveProvider.getIndexesON();
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
  public Set<String> getAllNames() {
    return getAllCurveNames();
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
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   */
  public void setAll(final CommodityProviderDiscount other) {
    ArgumentChecker.notNull(other, "Inflation provider");
    _multicurveProvider.setAll(other.getMulticurveProvider());
    _commodityForwardCurves.putAll(other._commodityForwardCurves);
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(index, curve);
  }

  /**
   * Replaces the discounting curve for a price index.
   * @param commodityUnderlying The price index.
   * @param curve The price curve for the index.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final CommodityUnderlying commodityUnderlying, final CommodityForwardCurve curve) {
    ArgumentChecker.notNull(commodityUnderlying, "commodity underlying");
    ArgumentChecker.notNull(curve, "curve");
    if (!_commodityForwardCurves.containsKey(commodityUnderlying)) {
      throw new IllegalArgumentException("Price index curve not in set: " + commodityUnderlying);
    }
    _commodityForwardCurves.put(commodityUnderlying, curve);
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

  @Override
  public CommodityProviderDiscount withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withDiscountFactor(ccy, replacement);
    return new CommodityProviderDiscount(decoratedMulticurve, _commodityForwardCurves);
  }

  @Override
  public CommodityProviderInterface withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    throw new NotImplementedException();
  }

  @Override
  public CommodityProviderInterface withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    throw new NotImplementedException();
  }

  @Override
  public Set<String> getAllCurveNames() {
    final Set<String> names = new TreeSet<>();
    names.addAll(_multicurveProvider.getAllNames());
    final Set<CommodityUnderlying> priceSet = _commodityForwardCurves.keySet();
    for (final CommodityUnderlying price : priceSet) {
      names.add(_commodityForwardCurves.get(price).getName());
    }
    return Collections.unmodifiableSet(names);
  }

  @Override
  public double[] parameterCommoditySensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    final CommodityForwardCurve curve = _allCurves.get(name);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final DoublesPair timeAndS : pointSensitivity) {
        final double[] sensi1Point = curve.getCommodityForwardParameterSensitivity(timeAndS.getFirst());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += timeAndS.getSecond() * sensi1Point[loopparam];
        }
      }
    }
    return result;
  }

  @Override
  public CommodityProviderInterface getCommodityProvider() {
    return this;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _commodityForwardCurves.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommodityProviderDiscount)) {
      return false;
    }
    final CommodityProviderDiscount other = (CommodityProviderDiscount) obj;
    if (!ObjectUtils.equals(_commodityForwardCurves, other._commodityForwardCurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    return true;
  }

}
