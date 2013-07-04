/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;

import com.opengamma.analytics.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.UniqueId;

/**
 *
 */
@Deprecated
public abstract class SkewKurtosisDataOptionModelFunction extends StandardOptionDataAnalyticOptionModelFunction {

  @Override
  protected SkewKurtosisOptionDataBundle getDataBundle(final Clock relevantTime, final EquityOptionSecurity option, final FunctionInputs inputs) {
    final StandardOptionDataBundle standardData = super.getDataBundle(relevantTime, option, inputs);
    final UniqueId uid = option.getUniqueId();
    final Object skewObject = inputs.getValue(ValueRequirementNames.SKEW);
    if (skewObject == null) {
      throw new NullPointerException("Could not get skew");
    }
    final Object kurtosisObject = inputs.getValue(ValueRequirementNames.PEARSON_KURTOSIS);
    if (kurtosisObject == null) {
      throw new NullPointerException("Could not get Pearson kurtosis");
    }
    final double skew = (Double) skewObject;
    final double kurtosis = (Double) kurtosisObject;
    return new SkewKurtosisOptionDataBundle(standardData, skew, kurtosis);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final UniqueId uid = target.getSecurity().getUniqueId();
      final Set<ValueRequirement> standardRequirements = super.getRequirements(context, target, desiredValue);
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
      result.addAll(standardRequirements);
      result.add(new ValueRequirement(ValueRequirementNames.SKEW, ComputationTargetType.SECURITY, uid));
      result.add(new ValueRequirement(ValueRequirementNames.PEARSON_KURTOSIS, ComputationTargetType.SECURITY, uid));
      return result;
    }
    return null;
  }
}
