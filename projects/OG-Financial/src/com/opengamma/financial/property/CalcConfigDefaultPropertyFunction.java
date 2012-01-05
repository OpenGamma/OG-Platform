/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Dummy function to inject default properties from the calculation configuration into the dependency graph.
 * <p>
 * Any default properties of the form <code>[PRIMITIVE|SECURITY|TRADE|POSITION|PORTFOLIO_NODE].<em>ValueName</em>.DEFAULT_<em>PropertyName</em>[.<em>UniqueId</em>]</code>
 * will be processed to introduce a default value for any omitted <em>PropertyName</em> on <em>ValueName</em> for that
 * target.
 */
/* package */abstract class CalcConfigDefaultPropertyFunction extends DefaultPropertyFunction {

  private static final String WILDCARD = "*";
  private static final String SEP = ".DEFAULT_";

  private final boolean _identifier;

  protected CalcConfigDefaultPropertyFunction(final ComputationTargetType type, final boolean identifier) {
    super(type, false);
    _identifier = identifier;
  }

  protected boolean isIdentifier() {
    return _identifier;
  }

  protected String getUniqueId(final ComputationTarget target) {
    if (target.getUniqueId() != null) {
      return target.getUniqueId().getObjectId().toString();
    } else {
      return null;
    }
  }

  protected List<String> getIdentifiers(final ComputationTarget target) {
    final String uniqueId = getUniqueId(target);
    if (uniqueId != null) {
      return Collections.singletonList(uniqueId);
    } else {
      return null;
    }
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    final String prefix = getTargetType().name() + ".";
    if (isIdentifier()) {
      final List<String> identifiers = getIdentifiers(defaults.getTarget());
      final List<String> suffixes = new ArrayList<String>(identifiers.size());
      for (String identifier : identifiers) {
        suffixes.add("." + identifier);
      }
      for (String property : defaults.getContext().getViewCalculationConfiguration().getDefaultProperties().getProperties()) {
        for (String suffix : suffixes) {
          if (property.startsWith(prefix) && property.endsWith(suffix)) {
            final int i = property.indexOf(SEP, prefix.length());
            if (i > 0) {
              final String valueName = property.substring(prefix.length(), i);
              final String propertyName = property.substring(i + SEP.length(), property.length() - suffix.length());
              if (WILDCARD.equals(valueName)) {
                defaults.addAllValuesPropertyName(propertyName);
              } else {
                defaults.addValuePropertyName(valueName, propertyName);
              }
            }
          }
        }
      }
    } else {
      for (String property : defaults.getContext().getViewCalculationConfiguration().getDefaultProperties().getProperties()) {
        if (property.startsWith(prefix)) {
          final int i = property.indexOf(SEP, prefix.length());
          if (i > 0) {
            if (property.indexOf('.', i + 1) < 0) {
              final String valueName = property.substring(prefix.length(), i);
              final String propertyName = property.substring(i + SEP.length());
              if (WILDCARD.equals(valueName)) {
                defaults.addAllValuesPropertyName(propertyName);
              } else {
                defaults.addValuePropertyName(valueName, propertyName);
              }
            }
          }
        }
      }
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (context.getViewCalculationConfiguration() == null) {
      return false;
    }
    final ValueProperties defaults = context.getViewCalculationConfiguration().getDefaultProperties();
    if (defaults.getProperties() == null) {
      return false;
    }
    final String prefix = getTargetType().name() + ".";
    if (isIdentifier()) {
      final List<String> identifiers = getIdentifiers(target);
      final List<String> suffixes = new ArrayList<String>(identifiers.size());
      for (String identifier : identifiers) {
        suffixes.add("." + identifier);
      }
      for (String property : defaults.getProperties()) {
        for (String suffix : suffixes) {
          if (property.startsWith(prefix) && property.endsWith(suffix)) {
            final int i = property.indexOf(SEP, prefix.length());
            if (i > 0) {
              return true;
            }
          }
        }
      }
    } else {
      for (String property : defaults.getProperties()) {
        if (property.startsWith(prefix)) {
          final int i = property.indexOf(SEP, prefix.length());
          if ((i > 0) && (property.indexOf('.', i + 1) < 0)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final StringBuilder sbSpecific = new StringBuilder(getTargetType().name()).append('.').append(desiredValue.getValueName()).append(SEP).append(propertyName);
    final StringBuilder sbWildcard = new StringBuilder(getTargetType().name()).append("." + WILDCARD + SEP).append(propertyName);
    if (isIdentifier()) {
      sbSpecific.append('.');
      sbWildcard.append('.');
      final int lSpecific = sbSpecific.length();
      final int lWildcard = sbWildcard.length();
      for (String identifier : getIdentifiers(target)) {
        sbSpecific.delete(lSpecific, sbSpecific.length()).append(identifier);
        sbWildcard.delete(lWildcard, sbWildcard.length()).append(identifier);
        Set<String> values = context.getViewCalculationConfiguration().getDefaultProperties().getValues(sbSpecific.toString());
        if (values != null) {
          return values;
        }
        values = context.getViewCalculationConfiguration().getDefaultProperties().getValues(sbWildcard.toString());
        if (values != null) {
          return values;
        }
      }
      return null;
    } else {
      Set<String> values = context.getViewCalculationConfiguration().getDefaultProperties().getValues(sbSpecific.toString());
      if (values != null) {
        return values;
      }
      return context.getViewCalculationConfiguration().getDefaultProperties().getValues(sbWildcard.toString());
    }
  }

  @Override
  public PriorityClass getPriority() {
    return isIdentifier() ? PriorityClass.ABOVE_NORMAL : PriorityClass.NORMAL;
  }

}
