/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.Z_SPREAD;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InflationBondZspreadFromCurvesFunction extends InflationBondFromCleanPriceAndCurvesFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InflationBondZspreadFromCurvesFunction.class);
  /** The z-spread calculator */
  private static final BondCapitalIndexedSecurityDiscountingMethod CALCULATOR = BondCapitalIndexedSecurityDiscountingMethod.getInstance();
  /** The curve construction configuration source */
  private ConfigDBCurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  /** The instrument exposures provider */
  private InstrumentExposuresProvider _instrumentExposuresProvider;

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#Z_SPREAD}
   */
  public InflationBondZspreadFromCurvesFunction() {
    super(Z_SPREAD);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _instrumentExposuresProvider = ConfigDBInstrumentExposuresProvider.init(context, this);
  }

  @Override
  protected Set<ComputedValue> getResult(final FunctionInputs inputs, final BondCapitalIndexedTransaction<?> bond, final InflationIssuerProviderInterface issuerCurves,
      final double cleanPrice, final ValueSpecification spec) {
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) inputs.getValue(YIELD_CURVE);
    final LegalEntity legalEntity = bond.getBondTransaction().getIssuerEntity();
    final Set<Pair<Object, LegalEntityFilter<LegalEntity>>> keys = issuerCurves.getIssuers();
    Pair<Object, LegalEntityFilter<LegalEntity>> keyOfCurveToReplace = null;
    for (final Pair<Object, LegalEntityFilter<LegalEntity>> key : keys) {
      if (key.getFirst().equals(key.getSecond().getFilteredData(legalEntity))) {
        keyOfCurveToReplace = key;
        break;
      }
    }
    if (keyOfCurveToReplace == null) {
      throw new OpenGammaRuntimeException("Could not find key for " + legalEntity);
    }
    final InflationIssuerProviderDiscount curvesWithReplacement = ((InflationIssuerProviderDiscount) issuerCurves).withIssuerCurve(keyOfCurveToReplace, curve);
    final double zSpread = 10000 * CALCULATOR.zSpreadFromCurvesAndCleanPriceDirect(bond.getBondStandard(), curvesWithReplacement, cleanPrice);
    return Collections.singleton(new ComputedValue(spec, zSpread));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curves = constraints.getValues(CURVE);
    if (curves == null || curves.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final String curveExposureConfig = desiredValue.getConstraint(CURVE_EXPOSURES);
    final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
    final String curve = Iterables.getOnlyElement(curves);
    final Set<String> curveConstructionConfigurationNames = _instrumentExposuresProvider.getCurveConstructionConfigurationsForConfig(curveExposureConfig, security);
    boolean curveNameFound = false;
    for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
      final CurveConstructionConfiguration curveConstructionConfiguration = _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfigurationName);
      final List<CurveGroupConfiguration> groups = curveConstructionConfiguration.getCurveGroups();
      for (final CurveGroupConfiguration group : groups) {
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
          if (curve.equals(entry.getKey())) {
            curveNameFound = true;
            break;
          }
        }
      }
      List<String> exoConfigs = curveConstructionConfiguration.getExogenousConfigurations();
      for (String curveConstructionConfigurationExogenousName : exoConfigs) {
        final CurveConstructionConfiguration curveConstructionConfigurationExogenous =
            _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfigurationExogenousName);
        final List<CurveGroupConfiguration> groupsExogenous = curveConstructionConfigurationExogenous.getCurveGroups();
        for (final CurveGroupConfiguration group : groupsExogenous) {
          for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
            if (curve.equals(entry.getKey())) {
              curveNameFound = true;
              break;
            }
          }
        }
      }
    }
    if (!curveNameFound) {
      return null;
    }
    final ValueProperties curveProperties = ValueProperties.builder()
        .with(CURVE, curve)
        .with(PROPERTY_CURVE_TYPE, constraints.getValues(PROPERTY_CURVE_TYPE))
        .get();
    requirements.add(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      if (entry.getKey().getValueName().equals(YIELD_CURVE)) {
        curveName = entry.getKey().getProperty(CURVE);
        break;
      }
    }
    if (curveName == null) {
      s_logger.error("Could not get curve name from inputs; missing yield curve");
      return null;
    }
    final ValueProperties properties = getResultProperties(target)
        .withoutAny(CURVE)
        .with(CURVE, curveName)
        .get();
    return Collections.singleton(new ValueSpecification(Z_SPREAD, target.toSpecification(), properties));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return super.getResultProperties(target)
        .withAny(CURVE);
  }
}
