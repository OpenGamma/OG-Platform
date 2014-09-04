/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.discrete.ParameterizedSABRModelDiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.ParameterizedSmileModelDiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.VectorFunction;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Caplet stripper using Hagan's SABR formula. The SABR parameters (alpha, beta, rho & nu) are themselves represented as
 * parameterised term structures, and this collection of parameters are the <i>model parameters</i>. For a particular
 * caplet, we can find the smile model parameters at its expiry, then (using the SABR formula) find the (Black)
 * volatility at its strike; hence the model parameters describe a caplet volatility surface.
 * <p>
 * For a set of market cap values, we can find, in a least-square sense, the optimal set of model parameters to reproduce the market values. Since the smiles are smooth functions of a few (4)
 * parameters, it is generally not possible to recover exactly market values using this method.
 */
public class CapletStripperSABRModel extends CapletStripperSmileModel<SABRFormulaData> {

  private static final int NUM_MODEL_PARMS = 4;

  /**
   * Set up the stripper.
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param volFuncProvider This will 'provide' a {@link DiscreteVolatilityFunction} that maps from a set of model
   * parameters (describing the term structures of SABR parameters) to the volatilities of the requested caplets.
   */
  public CapletStripperSABRModel(MultiCapFloorPricer pricer, ParameterizedSmileModelDiscreteVolatilityFunctionProvider<SABRFormulaData> volFuncProvider) {
    super(pricer, volFuncProvider);
  }

  /**
   * Set up the stripper.
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param smileModelParameterProviders each of these providers represents a different smile parameter - <b>there
   * must be one for each smile model parameter</b>. Given a (common) set of expiries, each one provides a {@link VectorFunction} that gives the corresponding smile model parameter at each expiry for
   * a set of model
   * parameters. This gives a lot of flexibility as to how the (smile model) parameter term structures are
   * represented.
   */
  public CapletStripperSABRModel(MultiCapFloorPricer pricer, DoublesVectorFunctionProvider[] smileModelParameterProviders) {
    super(pricer, getDiscreteVolatilityFunctionProvider(pricer, smileModelParameterProviders));
  }

  private static ParameterizedSABRModelDiscreteVolatilityFunctionProvider getDiscreteVolatilityFunctionProvider(MultiCapFloorPricer pricer, DoublesVectorFunctionProvider[] smileModelParameterProviders) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.noNulls(smileModelParameterProviders, "smileModelParameterProviders");
    ArgumentChecker.isTrue(NUM_MODEL_PARMS == smileModelParameterProviders.length, "Require {} smileModelParameterProviders", NUM_MODEL_PARMS);

    // this interpolated forward curve that will only be hit at the knots, so don't need anything more than linear
    ForwardCurve fwdCurve = new ForwardCurve(InterpolatedDoublesCurve.from(pricer.getCapletExpiries(), pricer.getCapletForwardRates(),
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)));
    return new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(fwdCurve, smileModelParameterProviders);
  }
}
