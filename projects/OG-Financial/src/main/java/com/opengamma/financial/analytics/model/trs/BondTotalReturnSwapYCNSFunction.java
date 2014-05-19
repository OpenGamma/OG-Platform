/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_SENSITIVITY_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class BondTotalReturnSwapYCNSFunction extends BondTotalReturnSwapFunction {

  
  private static final Logger s_logger = LoggerFactory.getLogger(BondTotalReturnSwapYCNSFunction.class);
  
  /**
   * 
   */
  public BondTotalReturnSwapYCNSFunction() {
    super(YIELD_CURVE_NODE_SENSITIVITIES);
  }
  
  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
    return new BondTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), 
        getDefinitionToDerivativeConverter(context), true) {
      
      @Override
      protected Set<ComputedValue> getValues(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues,
          InstrumentDerivative derivative, FXMatrix fxMatrix) {
        final MultipleCurrencyParameterSensitivity sensitivities = (MultipleCurrencyParameterSensitivity) inputs.getValue(BLOCK_CURVE_SENSITIVITIES);
        Set<ComputedValue> results = Sets.newHashSet();
        for (ValueRequirement desiredValue : desiredValues) {
          final ValueProperties properties = desiredValue.getConstraints();
          final String desiredCurveName = desiredValue.getConstraint(CURVE);
          final Map<Pair<String, Currency>, DoubleMatrix1D> entries = sensitivities.getSensitivities();
          for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : entries.entrySet()) {
            final String curveName = entry.getKey().getFirst();
            if (desiredCurveName.equals(curveName)) {
              final ValueProperties curveSpecificProperties = properties.copy()
                  .withoutAny(CURVE)
                  .with(CURVE, curveName)
                  .get();
              final CurveDefinition curveDefinition = (CurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL,
                  ValueProperties.builder().with(CURVE, curveName).get()));
              final DoubleLabelledMatrix1D ycns = MultiCurveUtils.getLabelledMatrix(entry.getValue(), curveDefinition);
              final ValueSpecification spec = new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), curveSpecificProperties);
              results.add(new ComputedValue(spec, ycns));
            }
          }
        }
        return results;

      }
      
      @Override
      protected String getCurrencyOfResult(final BondTotalReturnSwapSecurity security) {
        throw new IllegalStateException("BondTotalReturnSwapYCNSFunction does not set the Currency property in this method");
      }
      
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return super.getResults(context, target);
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
        String curveExposures = desiredValue.getConstraint(CURVE_EXPOSURES);
        String curveType = desiredValue.getConstraint(PROPERTY_CURVE_TYPE);
        Builder builder = ValueProperties.builder();
        builder.with(CURVE_EXPOSURES, curveExposures);
        builder.with(PROPERTY_CURVE_TYPE, curveType);
        ImmutableSet<ValueRequirement> bcsReq = ImmutableSet.of(new ValueRequirement(ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), builder.get()));
        return Sets.union(bcsReq, super.getRequirements(context, target, desiredValue));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
        final Set<String> functionNames = new HashSet<>();
        List<Pair<String, String>> ccyCurvePairs = Lists.newArrayList();
        for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
          final ValueSpecification specification = entry.getKey();
          if (specification.getValueName().equals(CURVE_BUNDLE)) {
            final ValueProperties constraints = specification.getProperties();
            for (String ccy : constraints.getValues(CURVE_SENSITIVITY_CURRENCY)) {
              for (String curve : constraints.getValues(CURVE)) {
                ccyCurvePairs.add(ObjectsPair.of(ccy, curve));
              }
            }
            functionNames.add(constraints.getSingleValue(FUNCTION));
          }
        }
        if (ccyCurvePairs.isEmpty()) {
          s_logger.error("Could not get currencies or curve name properties; have not been set in function(s) called {}", functionNames);
          return null;
        }
        final Set<ValueSpecification> results = new HashSet<>();
        for (Pair<String, String> ccyCurvePair : ccyCurvePairs) {
          final ValueProperties properties = createValueProperties()
              .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
              .withAny(CURVE_EXPOSURES)
              .with(CURRENCY, ccyCurvePair.getFirst())
              .with(CURVE_SENSITIVITY_CURRENCY, ccyCurvePair.getFirst())
              .with(CURVE, ccyCurvePair.getSecond())
              .get();
          results.add(new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties));
        }
        return results;
      }
      
      @SuppressWarnings("synthetic-access")
      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final ValueProperties.Builder properties = createValueProperties()
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .withAny(CURVE_EXPOSURES)
            .withAny(CURVE_SENSITIVITY_CURRENCY)
            .withoutAny(CURRENCY)
            .withAny(CURRENCY)
            .withAny(CURVE);
        return Collections.singleton(properties);
      }

    };
    
    
  }

}
