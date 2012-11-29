/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class InstrumentSpecificBlackVolatilitySurfaceDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(InstrumentSpecificBlackVolatilitySurfaceDefaults.class);
  private final Map<String, String> _uidToCurveName;
  private final Map<String, String> _uidToCurveCalculationMethodName;
  private final Map<String, String> _uidToSurfaceName;

  public InstrumentSpecificBlackVolatilitySurfaceDefaults(final String... defaultsPerUid) {
    super(ComputationTargetType.PRIMITIVE, true);
    ArgumentChecker.notNull(defaultsPerUid, "defaults per currency");
    final int n = defaultsPerUid.length;
    ArgumentChecker.notNull(n % 4 == 0, "Need one forward curve name, forward curve calculation method and surface name per uid value");
    _uidToCurveName = Maps.newLinkedHashMap();
    _uidToCurveCalculationMethodName = Maps.newLinkedHashMap();
    _uidToSurfaceName = Maps.newLinkedHashMap();
    for (int i = 0; i < n; i += 4) {
      final String uid = defaultsPerUid[i];
      _uidToCurveName.put(uid, defaultsPerUid[i + 1]);
      _uidToCurveCalculationMethodName.put(uid, defaultsPerUid[i + 2]);
      _uidToSurfaceName.put(uid, defaultsPerUid[i + 3]);
    }
  }

  @Override
  public abstract boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ValuePropertyNames.CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, ValuePropertyNames.SURFACE);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String uid = target.getUniqueId().getValue();
    final String curveName = _uidToCurveName.get(uid);
    if (curveName == null) {
      s_logger.error("Could not get curve name for {}; should never happen", target.getValue());
      return null;
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(curveName);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_uidToCurveCalculationMethodName.get(uid));
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_uidToSurfaceName.get(uid));
    }
    s_logger.error("Could not find default value for {} in this function", propertyName);
    return null;
  }

  protected Set<String> getUids() {
    return _uidToCurveName.keySet();
  }
}
