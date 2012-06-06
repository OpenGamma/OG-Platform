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

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MultiCurveCalculationConfig {
  //TODO check inputs for instrument exposures - need some or all of yield curve names in the array of names
  private final String _calculationConfigName;
  private final String[] _yieldCurveNames;
  private final UniqueIdentifiable[] _uniqueIds;
  private final String[] _calculationMethods;
  private final LinkedHashMap<String, String[]> _exogenousConfigAndCurveNames;
  private final LinkedHashMap<String, CurveInstrumentConfig> _curveExposuresForInstruments;

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String calculationMethod,
      final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments) {
    this(calculationConfigName, yieldCurveNames, uniqueIds, calculationMethod, curveExposuresForInstruments, null);
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String[] calculationMethods,
      final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments) {
    this(calculationConfigName, yieldCurveNames, uniqueIds, calculationMethods, curveExposuresForInstruments, null);
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String calculationMethod,
      final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments, final LinkedHashMap<String, String[]> exogenousConfigAndCurveNames) {
    ArgumentChecker.notNull(calculationConfigName, "calculation configuration name");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(uniqueIds, "unique identifiables");
    ArgumentChecker.notNull(calculationMethod, "calculation methods");
    ArgumentChecker.notEmpty(yieldCurveNames, "yield curve names");
    ArgumentChecker.notEmpty(uniqueIds, "unique ids");
    ArgumentChecker.noNulls(yieldCurveNames, "yield curve names");
    ArgumentChecker.noNulls(uniqueIds, "unique ids");
    ArgumentChecker.isTrue(yieldCurveNames.length == uniqueIds.length, "yield curve names length {} didn't match unique ids length {}", yieldCurveNames.length, uniqueIds.length);
    if (curveExposuresForInstruments != null) {
      ArgumentChecker.notEmpty(curveExposuresForInstruments, "curve exposures for instruments");
    }
    if (exogenousConfigAndCurveNames != null) {
      ArgumentChecker.notEmpty(exogenousConfigAndCurveNames, "exogenous config names");
      ArgumentChecker.noNulls(exogenousConfigAndCurveNames.entrySet(), "exogenous config names");
    }
    _calculationConfigName = calculationConfigName;
    _yieldCurveNames = yieldCurveNames;
    _uniqueIds = uniqueIds;
    _calculationMethods = new String[_yieldCurveNames.length];
    Arrays.fill(_calculationMethods, calculationMethod);
    _curveExposuresForInstruments = curveExposuresForInstruments;
    _exogenousConfigAndCurveNames = exogenousConfigAndCurveNames;
  }

  public MultiCurveCalculationConfig(final String calculationConfigName, final String[] yieldCurveNames, final UniqueIdentifiable[] uniqueIds, final String[] calculationMethods,
      final LinkedHashMap<String, CurveInstrumentConfig> curveExposuresForInstruments, final LinkedHashMap<String, String[]> exogenousConfigAndCurveNames) {
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
    if (exogenousConfigAndCurveNames != null) {
      ArgumentChecker.notEmpty(exogenousConfigAndCurveNames, "exogenous config names");
      ArgumentChecker.noNulls(exogenousConfigAndCurveNames.entrySet(), "exogenous config names");
    }
    _calculationConfigName = calculationConfigName;
    _yieldCurveNames = yieldCurveNames;
    _uniqueIds = uniqueIds;
    _calculationMethods = calculationMethods;
    _curveExposuresForInstruments = curveExposuresForInstruments;
    _exogenousConfigAndCurveNames = exogenousConfigAndCurveNames;
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

  public LinkedHashMap<String, CurveInstrumentConfig> getCurveExposuresForInstruments() {
    return _curveExposuresForInstruments;
  }

  public String[] getCurveExposureForInstrument(final String yieldCurveName, final StripInstrumentType instrumentType) {
    return _curveExposuresForInstruments.get(yieldCurveName).getExposuresForInstrument(instrumentType);
  }

  public LinkedHashMap<String, String[]> getExogenousConfigData() {
    return _exogenousConfigAndCurveNames;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calculationConfigName.hashCode();
    result = prime * result + Arrays.hashCode(_calculationMethods);
    result = prime * result + (_exogenousConfigAndCurveNames == null ? 0 : _exogenousConfigAndCurveNames.hashCode());
    result = prime * result + Arrays.hashCode(_uniqueIds);
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
    if (!Arrays.equals(_uniqueIds, other._uniqueIds)) {
      return false;
    }
    if (!Arrays.equals(_calculationMethods, other._calculationMethods)) {
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
        if (!Arrays.deepEquals(entry.getValue(), _exogenousConfigAndCurveNames.get(entry.getKey()))) {
          return false;
        }
      }
    }
    return true;
  }

}
