/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Dummy function to inject default properties from a position or trade's attributes into the dependency graph.
 * <p>
 * Any attributes of the form <code><em>ValueName</em>.DEFAULT_<em>PropertyName</em></code> will be
 * processed to introduce a default value for any omitted <em>PropertyName</em> on <em>ValueName</em> for the target.
 */
/* package */abstract class PositionOrTradeDefaultPropertyFunction extends DefaultPropertyFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionOrTradeDefaultPropertyFunction.class);

  // TODO: a wildcard for the value name could be nice but requires [PLAT-1759]

  private static final String SEP = ".DEFAULT_";

  public PositionOrTradeDefaultPropertyFunction(final ComputationTargetType type) {
    super(type, false);
  }

  protected abstract Map<String, String> getAttributes(ComputationTarget target);

  @Override
  protected void getDefaults(final FunctionCompilationContext context, final ComputationTarget target, final PropertyDefaults defaults) {
    for (Map.Entry<String, String> attribute : getAttributes(target).entrySet()) {
      final int i = attribute.getKey().indexOf(SEP);
      if (i > 0) {
        final String valueName = attribute.getKey().substring(0, i);
        final String propertyName = attribute.getKey().substring(i + SEP.length());
        defaults.addValuePropertyName(valueName, propertyName);
        s_logger.debug("Found default {}[{}]", valueName, propertyName);
      }
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Map<String, String> attributes = getAttributes(target);
    if ((attributes == null) || attributes.isEmpty()) {
      s_logger.debug("No attributes for target {}", target);
      return false;
    }
    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      final int i = attribute.getKey().indexOf(SEP);
      if (i > 0) {
        s_logger.debug("Found attribute {} for target {}", attribute.getKey(), target);
        return true;
      }
    }
    s_logger.debug("No matching attributes for target {}", target);
    return false;
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    return Collections.singleton(getAttributes(target).get(desiredValue.getValueName() + SEP + propertyName));
  }

  /**
   * Position and trade default functions are declared a lower priority so that the normal functions that work
   * from the calculation configuration can override their behavior.
   * 
   * @return {@link PriorityClass#BELOW_NORMAL}
   */
  @Override
  public PriorityClass getPriority() {
    return PriorityClass.BELOW_NORMAL;
  }

}
