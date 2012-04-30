/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MultiCurveCalculationConfig {
  private final String _calculationConfigName;
  private final String[] _yieldCurveNames;
  private final UniqueIdentifiable[] _uniqueIds;
  private final String[] _calculationMethods;
  private final String[] _exogenousConfigNames;

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String calculationMethod) {
    this(calculationConfigName, yieldCurveNames, uniqueIds, calculationMethod, null);
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String[] calculationMethods) {
    this(calculationConfigName, yieldCurveNames, uniqueIds, calculationMethods, null);
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String calculationMethod,
      final String[] exogenousConfigNames) {
    ArgumentChecker.notNull(calculationConfigName, "calculation configuration name");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(uniqueIds, "unique identifiables");
    ArgumentChecker.notNull(calculationMethod, "calculation methods");
    ArgumentChecker.notEmpty(yieldCurveNames, "yield curve names");
    ArgumentChecker.notEmpty(uniqueIds, "unique ids");
    ArgumentChecker.noNulls(yieldCurveNames, "yield curve names");
    ArgumentChecker.noNulls(uniqueIds, "unique ids");
    ArgumentChecker.isTrue(yieldCurveNames.length == uniqueIds.length, "yield curve names length {} didn't match unique ids length {}", yieldCurveNames.length, uniqueIds.length);
    if (exogenousConfigNames != null) {
      ArgumentChecker.notEmpty(exogenousConfigNames, "exogenous config names");
      ArgumentChecker.noNulls(exogenousConfigNames, "exogenous config names");
    }
    _calculationConfigName = calculationConfigName;
    _yieldCurveNames = yieldCurveNames;
    _uniqueIds = uniqueIds;
    _calculationMethods = new String[_yieldCurveNames.length];
    Arrays.fill(_calculationMethods, calculationMethod);
    _exogenousConfigNames = exogenousConfigNames;
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String[] calculationMethods,
      final String[] exogenousConfigNames) {
    ArgumentChecker.notNull(calculationConfigName, "calculation configuration name");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(uniqueIds, "unique identifiables");
    ArgumentChecker.notNull(calculationMethods, "calculation methods");
    ArgumentChecker.notEmpty(yieldCurveNames, "yield curve names");
    ArgumentChecker.notEmpty(uniqueIds, "unique ids");
    ArgumentChecker.notEmpty(calculationMethods, "calculation methods");
    ArgumentChecker.noNulls(yieldCurveNames, "yield curve names");
    ArgumentChecker.noNulls(uniqueIds, "unique ids");
    ArgumentChecker.noNulls(calculationMethods, "calculation methods");
    ArgumentChecker.isTrue(yieldCurveNames.length == uniqueIds.length, "yield curve names length {} didn't match unique ids length {}", yieldCurveNames.length, uniqueIds.length);
    ArgumentChecker.isTrue(yieldCurveNames.length == calculationMethods.length, "yield curve names length {} didn't match calculationMethods length {}",
        yieldCurveNames.length, calculationMethods.length);
    if (exogenousConfigNames != null) {
      ArgumentChecker.notEmpty(exogenousConfigNames, "exogenous config names");
      ArgumentChecker.noNulls(exogenousConfigNames, "exogenous config names");
    }
    _calculationConfigName = calculationConfigName;
    _yieldCurveNames = yieldCurveNames;
    _uniqueIds = uniqueIds;
    _calculationMethods = calculationMethods;
    _exogenousConfigNames = exogenousConfigNames;
  }

  public String getCalculationConfigName() {
    return _calculationConfigName;
  }

  public String[] getYieldCurveNames() {
    return _yieldCurveNames;
  }

  public String[] getCalculationMethods() {
    return _calculationMethods;
  }

  public UniqueIdentifiable[] getUniqueIds() {
    return _uniqueIds;
  }

  public String[] getExogenousConfigNames() {
    return _exogenousConfigNames;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calculationConfigName.hashCode();
    result = prime * result + Arrays.hashCode(_calculationMethods);
    result = prime * result + Arrays.hashCode(_exogenousConfigNames);
    result = prime * result + Arrays.hashCode(_uniqueIds);
    result = prime * result + Arrays.hashCode(_yieldCurveNames);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultiCurveCalculationConfig other = (MultiCurveCalculationConfig) obj;
    if (!ObjectUtils.equals(_calculationConfigName, other._calculationConfigName)) {
      return false;
    }
    if (!Arrays.equals(_yieldCurveNames, other._yieldCurveNames)) {
      return false;
    }
    if (!Arrays.equals(_uniqueIds, other._uniqueIds)) {
      return false;
    }
    if (!Arrays.equals(_calculationMethods, other._calculationMethods)) {
      return false;
    }
    if (!Arrays.equals(_exogenousConfigNames, other._exogenousConfigNames)) {
      return false;
    }
    return true;
  }

}
