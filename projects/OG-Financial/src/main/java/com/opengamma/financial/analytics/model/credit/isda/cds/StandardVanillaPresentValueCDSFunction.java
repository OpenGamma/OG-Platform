/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticCDSPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class StandardVanillaPresentValueCDSFunction extends StandardVanillaCDSFunction {
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static final MarketQuoteConverter POINTS_UP_FRONT_CONVERTER = new MarketQuoteConverter();

  public StandardVanillaPresentValueCDSFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition,
                                                final ISDACompliantYieldCurve yieldCurve,
                                                final ZonedDateTime[] times,
                                                final double[] marketSpreads,
                                                final ZonedDateTime valuationDate,
                                                final ComputationTarget target,
                                                final ValueProperties properties,
                                                final FunctionInputs inputs,
                                                final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic, final Tenor[] tenors) {

    final double pv = presentValue(definition, yieldCurve, hazardCurve, analytic);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

  public static double presentValue(final CreditDefaultSwapDefinition definition, final ISDACompliantYieldCurve yieldCurve,
                              final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic) {
    double pv;
    if (definition instanceof LegacyCreditDefaultSwapDefinition) {
      pv = PRICER.pv(analytic, yieldCurve, hazardCurve, ((LegacyCreditDefaultSwapDefinition) definition).getParSpread() * 1e-4) * definition.getNotional();
    } else if (definition instanceof StandardCreditDefaultSwapDefinition) {
      pv = POINTS_UP_FRONT_CONVERTER.quotedSpreadToPUF(analytic,
                                                       ((StandardCreditDefaultSwapDefinition) definition).getPremiumLegCoupon(),
                                                       yieldCurve,
                                                       ((StandardCreditDefaultSwapDefinition) definition).getQuotedSpread() * 1e-4) * definition.getNotional();
    } else {
      throw new OpenGammaRuntimeException("Unexpected cds type: " + definition.getClass().getSimpleName());
    }

    // SELL protection reverses directions of legs
   return (definition.getBuySellProtection() == BuySellProtection.SELL) ? -pv : pv;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> hazardRateCurveCalculationMethodNames = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD);
    if (hazardRateCurveCalculationMethodNames == null || hazardRateCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String spreadCurveName = security.accept(new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context))).getUniqueId().getValue();
    //TODO shouldn't need all of the yield curve properties
    final String hazardRateCurveCalculationMethod = Iterables.getOnlyElement(hazardRateCurveCalculationMethodNames);
    final String yieldCurveName = desiredValue.getConstraint(PROPERTY_YIELD_CURVE);
    final String yieldCurveCalculationConfig = desiredValue.getConstraint(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
    final String yieldCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    final Set<String> creditSpreadCurveShifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    final Set<String> creditSpreadCurveShiftTypes = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
    final ValueProperties.Builder hazardRateCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, hazardRateCurveCalculationMethod)
        .with(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, yieldCurveCalculationConfig)
        .with(PROPERTY_YIELD_CURVE_CALCULATION_METHOD, yieldCurveCalculationMethod)
        .with(PROPERTY_YIELD_CURVE, yieldCurveName);
    if (creditSpreadCurveShifts != null) {
      hazardRateCurveProperties.with(PROPERTY_SPREAD_CURVE_SHIFT, creditSpreadCurveShifts).with(PROPERTY_SPREAD_CURVE_SHIFT_TYPE, creditSpreadCurveShiftTypes);
    }
    final ValueRequirement hazardRateCurveRequirement = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), hazardRateCurveProperties.get());
    requirements.add(hazardRateCurveRequirement);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder propertiesBuilder = getCommonResultProperties();
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification spec = entry.getKey();
      final ValueProperties.Builder inputPropertiesBuilder = spec.getProperties().copy();
      inputPropertiesBuilder.withoutAny(FUNCTION);
      final String valueName = spec.getValueName();
      if (valueName.equals(ValueRequirementNames.YIELD_CURVE)) {
        propertiesBuilder.with(PROPERTY_YIELD_CURVE, inputPropertiesBuilder.get().getValues(CURVE));
        inputPropertiesBuilder.withoutAny(CURVE);
        propertiesBuilder.with(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, inputPropertiesBuilder.get().getValues(CURVE_CALCULATION_CONFIG));
        inputPropertiesBuilder.withoutAny(CURVE_CALCULATION_CONFIG);
        propertiesBuilder.with(PROPERTY_YIELD_CURVE_CALCULATION_METHOD, inputPropertiesBuilder.get().getValues(CURVE_CALCULATION_METHOD));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      } else if (valueName.equals(ValueRequirementNames.CREDIT_SPREAD_CURVE)) {
        propertiesBuilder.with(PROPERTY_SPREAD_CURVE, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE);
      } else if (valueName.equals(ValueRequirementNames.HAZARD_RATE_CURVE)) {
        propertiesBuilder.with(PROPERTY_HAZARD_RATE_CURVE, inputPropertiesBuilder.get().getValues(CURVE));
        inputPropertiesBuilder.withoutAny(CURVE);
        propertiesBuilder.with(PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD, inputPropertiesBuilder.get().getValues(CURVE_CALCULATION_METHOD));
        inputPropertiesBuilder.withoutAny(CURVE_CALCULATION_METHOD);
        inputPropertiesBuilder.withoutAny(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
        inputPropertiesBuilder.withoutAny(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
        inputPropertiesBuilder.withoutAny(PROPERTY_YIELD_CURVE);
      }
      final ValueProperties inputProperties = inputPropertiesBuilder.get();
      if (!inputProperties.isEmpty()) {
        for (final String propertyName : inputProperties.getProperties()) {
          propertiesBuilder.with(propertyName, inputProperties.getValues(propertyName));
        }
      }
    }
    //propertiesBuilder.withoutAny(PROPERTY_CDS_PRICE_TYPE);
    if (labelResultWithCurrency()) {
      propertiesBuilder.with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    }
    final ValueProperties properties = propertiesBuilder.get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, targetSpec, properties));
  }

  @Override
  protected ValueProperties.Builder getCommonResultProperties() {
    return createValueProperties();
  }

  @Override
  protected boolean labelResultWithCurrency() {
    return true;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
}
