/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ForwardPDEDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ForwardPDEDefaults.class);
  private static final String[] VALUE_REQUIREMENT_NAMES = new String[] {
    ValueRequirementNames.FORWARD_DELTA,
    ValueRequirementNames.DUAL_DELTA,
    ValueRequirementNames.DUAL_GAMMA,
    ValueRequirementNames.FORWARD_GAMMA,
    ValueRequirementNames.FOREX_DOMESTIC_PRICE,
    ValueRequirementNames.FOREX_PV_QUOTES,
    ValueRequirementNames.FORWARD_VEGA,
    ValueRequirementNames.FORWARD_VOMMA,
    ValueRequirementNames.FORWARD_VANNA,
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.GRID_DUAL_DELTA,
    ValueRequirementNames.GRID_DUAL_GAMMA,
    ValueRequirementNames.GRID_FORWARD_DELTA,
    ValueRequirementNames.GRID_FORWARD_GAMMA,
    ValueRequirementNames.GRID_FORWARD_VEGA,
    ValueRequirementNames.GRID_FORWARD_VANNA,
    ValueRequirementNames.GRID_FORWARD_VOMMA,
    ValueRequirementNames.GRID_IMPLIED_VOLATILITY,
    ValueRequirementNames.GRID_PRESENT_VALUE
  };
  private final String _theta;
  private final String _nTimeSteps;
  private final String _nSpaceSteps;
  private final String _timeStepBunching;
  private final String _spaceStepBunching;
  private final String _maxProxyDelta;
  private final String _centreMoneyness;
  private final String _spaceDirectionInterpolator;

  public ForwardPDEDefaults(final String theta, final String nTimeSteps, final String nSpaceSteps, final String timeStepBunching, final String spaceStepBunching,
      final String maxProxyDelta, final String centreMoneyness, final String spaceDirectionInterpolator) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(theta, "theta");
    ArgumentChecker.notNull(nTimeSteps, "number of time steps");
    ArgumentChecker.notNull(nSpaceSteps, "number of space steps");
    ArgumentChecker.notNull(timeStepBunching, "time step bunching");
    ArgumentChecker.notNull(spaceStepBunching, "space step bunching");
    ArgumentChecker.notNull(maxProxyDelta, "max proxy delta");
    ArgumentChecker.notNull(centreMoneyness, "centre moneyness");
    ArgumentChecker.notNull(spaceDirectionInterpolator, "space direction interpolator");
    _theta = theta;
    _nTimeSteps = nTimeSteps;
    _nSpaceSteps = nSpaceSteps;
    _timeStepBunching = timeStepBunching;
    _spaceStepBunching = spaceStepBunching;
    _maxProxyDelta = maxProxyDelta;
    _centreMoneyness = centreMoneyness;
    _spaceDirectionInterpolator = spaceDirectionInterpolator;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENT_NAMES) {
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_CENTRE_MONEYNESS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_MAX_PROXY_DELTA);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_THETA);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (PDEPropertyNamesAndValues.PROPERTY_MAX_PROXY_DELTA.equals(propertyName)) {
      return Collections.singleton(_maxProxyDelta);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_CENTRE_MONEYNESS.equals(propertyName)) {
      return Collections.singleton(_centreMoneyness);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS.equals(propertyName)) {
      return Collections.singleton(_nSpaceSteps);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS.equals(propertyName)) {
      return Collections.singleton(_nTimeSteps);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_spaceDirectionInterpolator);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING.equals(propertyName)) {
      return Collections.singleton(_spaceStepBunching);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_THETA.equals(propertyName)) {
      return Collections.singleton(_theta);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING.equals(propertyName)) {
      return Collections.singleton(_timeStepBunching);
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

  protected String[] getValueRequirementNames() {
    return VALUE_REQUIREMENT_NAMES;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PDE_DEFAULTS;
  }

}
