/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a issuer provider created from a issuer provider, where underlying curves should be based on 
 * zero-coupon annually compounded rates being described by {@link YieldAndDiscountCurve}. 
 * The discounting curve for issuer can be shifted by a parallel spread (in the annually compounded rate), where 
 * the underlying curve is {@link YieldPeriodicAddZeroFixedCurve}. 
 */
public class IssuerProviderIssuerAnnuallyCompoundeding implements IssuerProviderInterface {

  /**
   * The underlying issuer provider on which the multi-curves provider is based.
   */
  private final IssuerProviderDiscount _issuerProvider;
  /**
   * The issuer to be shifted.
   */
  private final LegalEntity _issuer;

  /**
   * Construct the issuerProvidor without spread
   * @param issuerProvider The underlying issuer provider on which the multi-curves provider is based, not null
   */
  public IssuerProviderIssuerAnnuallyCompoundeding(IssuerProviderDiscount issuerProvider) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    checkUnderlyingCurve(issuerProvider);
    _issuer = null;
    _issuerProvider = issuerProvider;
  }

  /**
   * Construct the issuerProvidor with spread from unshifted IssuerProviderIssuerAnnuallyCompoundedRate
   * @param issuerProvider IssuerProviderIssuerAnnuallyCompoundedRate without spread, not null
   * @param issuer The issuer, not null
   * @param spread The spread
   */
  public IssuerProviderIssuerAnnuallyCompoundeding(IssuerProviderIssuerAnnuallyCompoundeding issuerProvider,
      LegalEntity issuer, double spread) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    if (spread == 0.0) {
      _issuer = null;
      _issuerProvider = issuerProvider.getIssuerProvider();
    } else {
      ArgumentChecker.notNull(issuer, "issuer");
      _issuer = issuer;
      _issuerProvider = addSpread(issuerProvider.getIssuerProvider(), spread);
    }
  }

  /**
   * Construct the issuerProvidor with spread from the underlying issuer Provider. 
   * @param issuerProvider The underlying issuer provider on which the multi-curves provider is based, not null
   * @param issuer The issuer, not null
   * @param spread The spread
   */
  public IssuerProviderIssuerAnnuallyCompoundeding(IssuerProviderDiscount issuerProvider, LegalEntity issuer,
      double spread) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    checkUnderlyingCurve(issuerProvider);
    if (spread == 0.0) {
      _issuer = null;
      _issuerProvider = issuerProvider.getIssuerProvider();
    } else {
      ArgumentChecker.notNull(issuer, "issuer");
      _issuer = issuer;
      _issuerProvider = addSpread(issuerProvider.getIssuerProvider(), spread);
    }
  }

  private void checkUnderlyingCurve(IssuerProviderDiscount issuerProvider) {
    for (Map.Entry<Currency, YieldAndDiscountCurve> entry : issuerProvider.getMulticurveProvider()
        .getDiscountingCurves().entrySet()) {
      ArgumentChecker.isTrue(entry.getValue() instanceof YieldPeriodicCurve ||
          entry.getValue() instanceof YieldPeriodicAddZeroFixedCurve,
          "Underlying curve should be YieldPeriodicCurve");
    }
    for (Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : issuerProvider
        .getIssuerCurves().entrySet()) {
      ArgumentChecker.isTrue(entry.getValue() instanceof YieldPeriodicCurve ||
          entry.getValue() instanceof YieldPeriodicAddZeroFixedCurve,
          "Underlying curve should be YieldPeriodicCurve");
    }
  }

  private IssuerProviderDiscount addSpread(IssuerProviderDiscount curve, double spread) {
    IssuerProviderDiscount issuerDiscount = curve.copy();
    boolean isSubtract = spread < 0.0;
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(Math.abs(spread));
    YieldCurve spreadCurve = new YieldCurve("spread", constantDoublesCurve);
    for (Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : issuerDiscount
        .getIssuerCurves().entrySet()) {
      ArgumentChecker.isTrue(entry.getValue() instanceof YieldPeriodicCurve,
          "Underlying curve should be YieldPeriodicCurve, but {}", entry.getValue().getClass());
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getFilteredData(_issuer))) {
        ArgumentChecker.isFalse(entry.getValue() instanceof YieldPeriodicAddZeroFixedCurve,
            "Underlying curve has spread");
        // wrap base curve in spread curve
        YieldPeriodicCurve baseCurve = (YieldPeriodicCurve) entry.getValue();
        YieldAndDiscountAddZeroFixedCurve wrappedCurve = new YieldPeriodicAddZeroFixedCurve(baseCurve.getName(),
            isSubtract, baseCurve, spreadCurve);
        issuerDiscount.getIssuerCurves().put(entry.getKey(), wrappedCurve);
      }
    }
    return issuerDiscount;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderDiscount getIssuerProvider() {
    return _issuerProvider;
  }

  @Override
  public IssuerProviderInterface copy() {
    throw new UnsupportedOperationException("Copy not supported for decorated providers");
  }

  @Override
  public double getDiscountFactor(final LegalEntity issuer, final Double time) {
    return _issuerProvider.getDiscountFactor(issuer, time);
  }

  @Override
  public String getName(final Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy) {
    return _issuerProvider.getName(issuerCcy);
  }

  @Override
  public String getName(final LegalEntity issuerCcy) {
    return _issuerProvider.getName(issuerCcy);
  }

  @Override
  public Set<String> getAllNames() {
    return _issuerProvider.getAllNames();
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    throw new UnsupportedOperationException("parameterSensitivity not supported");
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _issuerProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    return _issuerProvider.getNumberOfParameters(name);
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _issuerProvider.getUnderlyingCurvesNames(name);
  }

  @Override
  public Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuerProvider.getIssuers();
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _issuerProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_issuer == null) ? 0 : _issuer.hashCode());
    result = prime * result + _issuerProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IssuerProviderIssuerAnnuallyCompoundeding)) {
      return false;
    }
    IssuerProviderIssuerAnnuallyCompoundeding other = (IssuerProviderIssuerAnnuallyCompoundeding) obj;
    if (_issuer == null) {
      if (other._issuer != null) {
        return false;
      }
    } else if (!ObjectUtils.equals(_issuer, other._issuer)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    return true;
  }

}
