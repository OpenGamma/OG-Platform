/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SecurityCurveProvider extends CurveProvider {
  private static final CurveConfigurationForSecurityVisitor PER_SECURITY_VISITOR = new CurveConfigurationForSecurityVisitor();

  public Set<ValueRequirement> getValueRequirements(final Object target, final InstrumentExposureConfiguration exposureConfiguration) {
    FinancialSecurity security;
    if (target instanceof FinancialSecurity) {
      security = (FinancialSecurity) target;
    } else if (target instanceof Trade) {
      security = (FinancialSecurity) ((Trade) target).getSecurity();
    } else {
      throw new OpenGammaRuntimeException("Unhandled type " + target);
    }
    final List<List<ExternalId>> ids = security.accept(PER_SECURITY_VISITOR);
    for (final List<ExternalId> idList : ids) {
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final ExternalId id : idList) {
        requirements.addAll(getValueRequirements(id, exposureConfiguration));
      }
      if (!requirements.isEmpty()) {
        return requirements;
      }
    }
    throw new OpenGammaRuntimeException("Could not get yield curve value requirements for " + target);
  }

  public MulticurveProviderDiscount getCurveProvider(final Object target, final InstrumentExposureConfiguration exposureConfiguration, final FXMatrix fxMatrix,
      final FunctionInputs inputs) {
    FinancialSecurity security;
    if (target instanceof FinancialSecurity) {
      security = (FinancialSecurity) target;
    } else if (target instanceof Trade) {
      security = (FinancialSecurity) ((Trade) target).getSecurity();
    } else {
      throw new OpenGammaRuntimeException("Unhandled type " + target);
    }
    final List<List<ExternalId>> ids = security.accept(PER_SECURITY_VISITOR);
    for (final List<ExternalId> idList : ids) {
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final ExternalId id : idList) {
        requirements.addAll(getValueRequirements(id, exposureConfiguration));
      }
      if (!requirements.isEmpty()) {
        break;
      }
    }
    final FinancialSecurityVisitor<MulticurveProviderDiscount> visitor = new FinancialSecurityVisitorAdapter<MulticurveProviderDiscount>() {

      @Override
      public MulticurveProviderDiscount visitCashSecurity(final CashSecurity security) {
        final Map<Currency, YieldAndDiscountCurve> discountingCurves = new HashMap<>();
        final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new HashMap<>();
        final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new HashMap<>();
        final Currency currency = security.getCurrency();
        return null;
      }
    };
    return security.accept(visitor);
  }

  private static class CurveMapVisitor implements CurveConfigurationVisitor<Map<?, YieldAndDiscountCurve>> {

    @Override
    public Map<?, YieldAndDiscountCurve> visitDiscountingCurveConfiguration(final DiscountingCurveConfiguration configuration) {
      return null;
    }

    @Override
    public Map<?, YieldAndDiscountCurve> visitOvernightCurveConfiguration(final OvernightCurveConfiguration configuration) {
      return null;
    }

    @Override
    public Map<?, YieldAndDiscountCurve> visitForwardIborCurveConfiguration(final ForwardIborCurveConfiguration configuration) {
      return null;
    }

  }
}
