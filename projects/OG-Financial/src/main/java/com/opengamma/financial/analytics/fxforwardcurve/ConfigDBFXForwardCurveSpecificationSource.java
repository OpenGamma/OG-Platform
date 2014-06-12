/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBFXForwardCurveSpecificationSource implements FXForwardCurveSpecificationSource {

  private final ConfigSourceQuery<FXForwardCurveSpecification> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBFXForwardCurveSpecificationSource(ConfigSource,VersionCorrection)}, {@link #ConfigDBFXForwardCurveSpecificationSource(ConfigSourceQuery)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBFXForwardCurveSpecificationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBFXForwardCurveSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<FXForwardCurveSpecification>(configSource, FXForwardCurveSpecification.class, versionCorrection));
  }

  public ConfigDBFXForwardCurveSpecificationSource(final ConfigSourceQuery<FXForwardCurveSpecification> query) {
    _query = ArgumentChecker.notNull(query, "query");
  }

  public static ConfigDBFXForwardCurveSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBFXForwardCurveSpecificationSource(ConfigSourceQuery.init(context, function, FXForwardCurveSpecification.class));
  }

  @Override
  public FXForwardCurveSpecification getSpecification(final String name, final String currencyPair) {
    return getSpecification(name, currencyPair, _query.getVersionCorrection());
  }

  @Override
  public FXForwardCurveSpecification getSpecification(final String name, final String currencyPair, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final FXForwardCurveSpecification specification = _query.get(name + "_" + currencyPair + "_FX_FORWARD", versionCorrection);
    if (specification == null) {
      if (currencyPair.length() == 6) {
        final String firstCcy = currencyPair.substring(0, 3);
        final String secondCcy = currencyPair.substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _query.get(name + "_" + reversedCcys + "_FX_FORWARD", versionCorrection);
      }
    }
    return specification;
  }
}
