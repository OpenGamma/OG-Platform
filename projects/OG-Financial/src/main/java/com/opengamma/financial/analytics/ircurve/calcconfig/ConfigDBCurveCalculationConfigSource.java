/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.view.ConfigDocumentWatchSetProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBCurveCalculationConfigSource implements CurveCalculationConfigSource {

  private final ConfigSource _configSource;
  private final VersionCorrection _lockedVersionCorrection;

  public ConfigDBCurveCalculationConfigSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(configSource, "configuration source");
    _configSource = configSource;
    _lockedVersionCorrection = versionCorrection;
  }

  public static void reinitOnChanges(final FunctionCompilationContext context, final FunctionDefinition function) {
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, function, MultiCurveCalculationConfig.class);
  }

  @Override
  public MultiCurveCalculationConfig getConfig(final String name) {
    return getConfig(name, _lockedVersionCorrection);
  }

  @Override
  public MultiCurveCalculationConfig getConfig(final String name, final VersionCorrection versionCorrection) {
    return _configSource.getSingle(MultiCurveCalculationConfig.class, name, versionCorrection);
  }

}
