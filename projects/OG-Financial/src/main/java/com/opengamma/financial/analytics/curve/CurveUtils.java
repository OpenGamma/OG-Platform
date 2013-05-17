/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.id.VersionCorrection;

/**
 * 
 */
public class CurveUtils {

  public static CurveSpecification getCurveSpecification(final Instant valuationTime, final ConfigSource configSource, final LocalDate curveDate, final String curveName) {
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    final CurveDefinitionSource curveDefinitionSource = new ConfigDBCurveDefinitionSource(configSource);
    final CurveDefinition curveDefinition = curveDefinitionSource.getCurveDefinition(curveName, VersionCorrection.of(versionTime, versionTime));
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
    }
    final CurveSpecificationBuilder curveSpecificationBuilder = new ConfigDBCurveSpecificationBuilder(configSource);
    return curveSpecificationBuilder.buildCurve(valuationTime, curveDate, curveDefinition);
  }

  public static MultiCurveCalculationConfig getCurveCalculationConfig(final Instant valuationTime, final ConfigSource configSource, final LocalDate configDate,
      final String configName) {
    final Instant versionTime = valuationTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    final ConfigDBCurveCalculationConfigSource curveConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveConfig = curveConfigSource.getConfig(configName, VersionCorrection.of(versionTime, versionTime));
    if (curveConfig == null) {
      throw new OpenGammaRuntimeException("Could not get curve calculation config called " + configName);
    }
    return curveConfig;
  }
}
