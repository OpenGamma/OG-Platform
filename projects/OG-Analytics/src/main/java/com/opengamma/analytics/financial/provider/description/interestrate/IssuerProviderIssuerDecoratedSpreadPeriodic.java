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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddPeriodicZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a issuer provider created from a issuer provider where the discounting curve for one issuer is
 * shifted (decorated) by a a parallel spread to the zero-coupon periodic compounded rate.
 */
public class IssuerProviderIssuerDecoratedSpreadPeriodic implements IssuerProviderInterface {

  /**
   * The underlying issuer provider on which the multi-curves provider is based.
   */
  private final IssuerProviderDiscount _issuerProvider;
  /**
   * The issuer/currency pair to be shifted.
   */
  private final LegalEntity _issuer;

  /**
   * Constructor.
   * @param issuerProvider The underlying issuer provider on which the multi-curves provider is based, not null
   * @param issuer The issuer, not null
   * @param spread The spread
   * @param nPeriodsPerYear The number of periods per year.
   */
  public IssuerProviderIssuerDecoratedSpreadPeriodic(IssuerProviderInterface issuerProvider, LegalEntity issuer,
      double spread, int nPeriodsPerYear) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    ArgumentChecker.notNull(issuer, "issuer");
    ArgumentChecker.isTrue(nPeriodsPerYear > 0, "nPeriodsParYear should be greater than 0");
    ArgumentChecker
        .isTrue(issuerProvider instanceof IssuerProviderDiscount, "issuerProvider should be IssuerProviderDiscount");
    IssuerProviderDiscount casted = (IssuerProviderDiscount) issuerProvider;
    _issuer = issuer;
    _issuerProvider = spread == 0.0 ? casted : addSpread(casted, spread, nPeriodsPerYear);
  }

  private IssuerProviderDiscount addSpread(IssuerProviderDiscount curve, double spread, int nPeriodsPerYear) {
    IssuerProviderDiscount issuerDiscount = curve.copy();
    boolean isSubtract = spread < 0.0;
    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(Math.abs(spread));
    YieldCurve spreadCurve = new YieldCurve("spread", constantDoublesCurve);
    for (Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : issuerDiscount
        .getIssuerCurves().entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getFilteredData(_issuer))) {
        ArgumentChecker.isFalse(entry.getValue() instanceof YieldAndDiscountAddPeriodicZeroFixedCurve,
            "Underlying curve has spread");
        // wrap base curve in spread curve
        YieldAndDiscountCurve baseCurve = entry.getValue();
        YieldAndDiscountAddPeriodicZeroFixedCurve wrappedCurve = new YieldAndDiscountAddPeriodicZeroFixedCurve(
            baseCurve.getName(), isSubtract, baseCurve, spreadCurve, nPeriodsPerYear);
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
    return _issuerProvider.parameterSensitivity(name, pointSensitivity);
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
    result = prime * result + _issuer.hashCode();
    result = prime * result + _issuerProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IssuerProviderIssuerDecoratedSpreadPeriodic)) {
      return false;
    }
    final IssuerProviderIssuerDecoratedSpreadPeriodic other = (IssuerProviderIssuerDecoratedSpreadPeriodic) obj;
    if (!ObjectUtils.equals(_issuer, other._issuer)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    return true;
  }

}
