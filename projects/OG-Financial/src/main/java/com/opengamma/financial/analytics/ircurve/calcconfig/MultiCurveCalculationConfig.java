/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
@Config(description = "Multi curve calculation config", group = ConfigGroups.CURVES_LEGACY)
public class MultiCurveCalculationConfig {
  //TODO check inputs for instrument exposures - need some or all of yield curve names in the array of names
  private final String _calculationConfigName;
  private final String[] _yieldCurveNames;
  private final ComputationTargetSpecification _target;
  private final String _calculationMethod;
  private final LinkedHashMap<String, String[]> _exogenousConfigAndCurveNames;
  private final LinkedHashMap<String, CurveInstrumentConfig> _curveExposuresForInstruments;

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final ComputationTargetSpecification target, final String calculationMethod,
      final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments) {
    this(calculationConfigName, yieldCurveNames, target, calculationMethod, curveExposuresForInstruments, null);
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final ComputationTargetSpecification target, final String calculationMethod,
      final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments, final LinkedHashMap<String, String[]> exogenousConfigAndCurveNames) {
    ArgumentChecker.notNull(calculationConfigName, "calculation configuration name");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(calculationMethod, "calculation methods");
    ArgumentChecker.notEmpty(yieldCurveNames, "yield curve names");
    ArgumentChecker.noNulls(yieldCurveNames, "yield curve names");
    if (curveExposuresForInstruments != null) {
      ArgumentChecker.notEmpty(curveExposuresForInstruments, "curve exposures for instruments");
    }
    if (exogenousConfigAndCurveNames != null) {
      ArgumentChecker.notEmpty(exogenousConfigAndCurveNames, "exogenous config names");
      ArgumentChecker.noNulls(exogenousConfigAndCurveNames.entrySet(), "exogenous config names");
    }
    _calculationConfigName = calculationConfigName;
    _yieldCurveNames = yieldCurveNames;
    _target = target;
    _calculationMethod = calculationMethod;
    _curveExposuresForInstruments = curveExposuresForInstruments;
    _exogenousConfigAndCurveNames = exogenousConfigAndCurveNames;
  }

  public String getCalculationConfigName() {
    return _calculationConfigName;
  }

  public String[] getYieldCurveNames() {
    return _yieldCurveNames;
  }

  public String[] getCurveNames() {
    return _yieldCurveNames;
  }

  public String getCalculationMethod() {
    return _calculationMethod;
  }

  public ComputationTargetSpecification getTarget() {
    return _target;
  }

  public LinkedHashMap<String, CurveInstrumentConfig> getCurveExposuresForInstruments() {
    return _curveExposuresForInstruments;
  }

  public String[] getCurveExposureForInstrument(final String yieldCurveName, final StripInstrumentType instrumentType) {
    if (_curveExposuresForInstruments.containsKey(yieldCurveName)) {
      return _curveExposuresForInstruments.get(yieldCurveName).getExposuresForInstrument(instrumentType);
    }
    throw new IllegalArgumentException("Could not get instrument exposures for curve named " + yieldCurveName + " in config " + _calculationConfigName);
  }

  public LinkedHashMap<String, String[]> getExogenousConfigData() {
    return _exogenousConfigAndCurveNames;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calculationConfigName.hashCode();
    result = prime * result + _calculationMethod.hashCode();
    result = prime * result + (_exogenousConfigAndCurveNames == null ? 0 : _exogenousConfigAndCurveNames.hashCode());
    result = prime * result + _target.hashCode();
    result = prime * result + Arrays.hashCode(_yieldCurveNames);
    result = prime * result + (_curveExposuresForInstruments == null ? 0 : _curveExposuresForInstruments.hashCode());
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
    if (!ObjectUtils.equals(_curveExposuresForInstruments, other._curveExposuresForInstruments)) {
      return false;
    }
    if (!ObjectUtils.equals(_target, other._target)) {
      return false;
    }
    if (!ObjectUtils.equals(_calculationMethod, other._calculationMethod)) {
      return false;
    }
    if (_exogenousConfigAndCurveNames != null) {
      if (_exogenousConfigAndCurveNames.size() != other._exogenousConfigAndCurveNames.size()) {
        return false;
      }
      for (final Map.Entry<String, String[]> entry : _exogenousConfigAndCurveNames.entrySet()) {
        if (!other._exogenousConfigAndCurveNames.containsKey(entry.getKey())) {
          return false;
        }
        if (!Arrays.deepEquals(entry.getValue(), other._exogenousConfigAndCurveNames.get(entry.getKey()))) {
          return false;
        }
      }
    }
    return true;
  }

}
