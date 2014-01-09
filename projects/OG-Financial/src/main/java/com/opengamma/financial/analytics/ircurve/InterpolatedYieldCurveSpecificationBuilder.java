/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import org.threeten.bp.LocalDate;

import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public interface InterpolatedYieldCurveSpecificationBuilder {

  /**
   * Synthetic object identifier used to propagate changes from concrete implementations to function initializations.
   */
  ObjectId SYNTHETIC_CHANGE_ID = ObjectId.of("OpenGamma", "InterpolatedYieldCurveSpecification");

  /**
   * Instance locked at a single version/correction timestamp.
   * <p>
   * Construction of this object, using the {@link #init} method, will also register the calling function for re-initialization.
   */
  final class AtVersionCorrection {

    private final InterpolatedYieldCurveSpecificationBuilder _instance;
    private final VersionCorrection _version;

    private AtVersionCorrection(final InterpolatedYieldCurveSpecificationBuilder instance, final VersionCorrection version) {
      _instance = ArgumentChecker.notNull(instance, "instance");
      _version = ArgumentChecker.notNull(version, "version");
    }

    public InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition) {
      return _instance.buildCurve(curveDate, curveDefinition, _version);
    }

    public static AtVersionCorrection init(final FunctionCompilationContext context, final FunctionDefinition function) {
      if (context.getFunctionReinitializer() != null) {
        context.getFunctionReinitializer().reinitializeFunction(function, SYNTHETIC_CHANGE_ID);
      }
      return new AtVersionCorrection(OpenGammaCompilationContext.getInterpolatedYieldCurveSpecificationBuilder(context), context.getFunctionInitializationVersionCorrection());
    }

  }

  InterpolatedYieldCurveSpecification buildCurve(LocalDate curveDate, YieldCurveDefinition curveDefinition, VersionCorrection version);

}
