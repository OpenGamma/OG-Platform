/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.SpreadCurveDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source for {@link AbstractCurveDefinition}s.
 */
public class ConfigDBCurveDefinitionSource implements CurveDefinitionSource {

  private final ConfigSourceQuery<InterpolatedCurveDefinition> _queryInterpolatedCurveDefinition;
  private final ConfigSourceQuery<CurveDefinition> _queryCurveDefinition;
  private final ConfigSourceQuery<ConstantCurveDefinition> _queryConstantCurveDefinition;
  private final ConfigSourceQuery<SpreadCurveDefinition> _querySpreadCurveDefinition;

  /**
   * @param configSource The config source, not null
   * @deprecated Use {@link #ConfigDBCurveDefinitionSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBCurveDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBCurveDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(configSource, "configSource");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    _queryInterpolatedCurveDefinition = new ConfigSourceQuery<>(configSource, InterpolatedCurveDefinition.class, versionCorrection);
    _queryCurveDefinition = new ConfigSourceQuery<>(configSource, CurveDefinition.class, versionCorrection);
    _queryConstantCurveDefinition = new ConfigSourceQuery<>(configSource, ConstantCurveDefinition.class, versionCorrection);
    _querySpreadCurveDefinition = new ConfigSourceQuery<>(configSource, SpreadCurveDefinition.class, versionCorrection);
  }

  public static ConfigDBCurveDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    final ConfigDBCurveDefinitionSource source = new ConfigDBCurveDefinitionSource(OpenGammaCompilationContext.getConfigSource(context), context.getFunctionInitializationVersionCorrection());
    source._queryInterpolatedCurveDefinition.reinitOnChange(context, function);
    source._queryCurveDefinition.reinitOnChange(context, function);
    source._queryConstantCurveDefinition.reinitOnChange(context, function);
    source._querySpreadCurveDefinition.reinitOnChange(context, function);
    return source;
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name) {
    return getCurveDefinition(name, _queryCurveDefinition.getVersionCorrection());
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    CurveDefinition result = _queryInterpolatedCurveDefinition.get(name, versionCorrection);
    if (result == null) {
      result = _queryCurveDefinition.get(name, versionCorrection);
    }
    return result;
  }

  @Override
  public AbstractCurveDefinition getDefinition(final String name) {
    return getDefinition(name, _queryCurveDefinition.getVersionCorrection());
  }

  @Override
  public AbstractCurveDefinition getDefinition(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    AbstractCurveDefinition result = _queryInterpolatedCurveDefinition.get(name, versionCorrection);
    if (result != null) {
      return result;
    }
    result = _queryConstantCurveDefinition.get(name, versionCorrection);
    if (result != null) {
      return result;
    }
    result = _querySpreadCurveDefinition.get(name, versionCorrection);
    if (result != null) {
      return result;
    }
    return _queryCurveDefinition.get(name, versionCorrection);
  }

}
