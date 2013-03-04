/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CurveProviderHelper {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveProviderHelper.class);

  /**
   * 
   */
  public CurveProviderHelper() {
  }

  public static Map<Currency, YieldAndDiscountCurve> getDiscountingCurves(final FinancialSecurity security, final FunctionInputs inputs,
      final CurveExposureConfiguration config, final SecuritySource securitySource) {
    final FinancialSecurityVisitor<Set<ValueRequirement>> curveRequirementVisitor = new FinancialSecurityVisitorSameValueAdapter<Set<ValueRequirement>>(null) {

      @Override
      public Set<ValueRequirement> visitCashSecurity(final CashSecurity cash) {
        final Currency currency = cash.getCurrency();
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, config.getDiscountingCurveName())
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config.getDiscountingCurveCalculationConfig())
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties));
      }

      @Override
      public Set<ValueRequirement> visitFRASecurity(final FRASecurity fra) {
        final Currency currency = fra.getCurrency();
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, config.getDiscountingCurveName())
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config.getDiscountingCurveCalculationConfig())
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties));
      }

      @Override
      public Set<ValueRequirement> visitInterestRateFutureSecurity(final InterestRateFutureSecurity interestRateFuture) {
        final Currency currency = interestRateFuture.getCurrency();
        final ValueProperties properties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, config.getDiscountingCurveName())
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config.getDiscountingCurveCalculationConfig())
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties));
      }

      @Override
      public Set<ValueRequirement> visitSwapSecurity(final SwapSecurity swap) {
        final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(swap, securitySource);
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final Currency currency : currencies) {
          final ValueProperties properties = ValueProperties.builder()
              .with(ValuePropertyNames.CURVE, config.getDiscountingCurveName())
              .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config.getDiscountingCurveCalculationConfig())
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties));
        }
        return requirements;
      }

      @Override
      public Set<ValueRequirement> visitFXForwardSecurity(final FXForwardSecurity fxForward) {
        final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(fxForward, securitySource);
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final Currency currency : currencies) {
          final ValueProperties properties = ValueProperties.builder()
              .with(ValuePropertyNames.CURVE, config.getDiscountingCurveName())
              .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config.getDiscountingCurveCalculationConfig())
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties));
        }
        return requirements;
      }

      @Override
      public Set<ValueRequirement> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity fxForward) {
        final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(fxForward, securitySource);
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final Currency currency : currencies) {
          final ValueProperties properties = ValueProperties.builder()
              .with(ValuePropertyNames.CURVE, config.getDiscountingCurveName())
              .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, config.getDiscountingCurveCalculationConfig())
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties));
        }
        return requirements;
      }
    };
    final Set<ValueRequirement> curveRequirements = security.accept(curveRequirementVisitor);
    if (curveRequirements == null) {
      return null;
    }
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new HashMap<>();
    for (final ValueRequirement requirement : curveRequirements) {
      final Object curveObject = inputs.getValue(requirement);
      if (curveObject == null) {
        s_logger.error("Could not get value for requirement {}", requirement);
        return null;
      }
      final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
      final Currency currency = Currency.of(requirement.getTargetReference().getSpecification().getUniqueId().getValue());
      discountingCurves.put(currency, curve);
    }
    return discountingCurves;
  }

  public static Map<IborIndex, YieldAndDiscountCurve> getForwardIborCurves(final FinancialSecurity security) {
    return null;
  }

  public static Map<IndexON, YieldAndDiscountCurve> getForwardONCurves(final FinancialSecurity security) {
    return null;
  }

  public static FXMatrix getFXMatrix(final FinancialSecurity security) {
    return null;
  }

  protected MulticurveProviderDiscount getCurveProvider(final FinancialSecurity security) {
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new HashMap<>();
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new HashMap<>();
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new HashMap<>();
    final FXMatrix fxMatrix = new FXMatrix();
    return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
  }

}
