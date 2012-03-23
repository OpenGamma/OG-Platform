/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.FunctionDefinition;

/**
 * Standard names used to refer to particular computed values.
 * <p>
 * These name are used as keys to define specific required values in the engine.
 * They should be used by a {@link FunctionDefinition} to state their required inputs
 * and their potential outputs.
 * These are a typical common set of names, which may be extended.
 * <p>
 * For names used to refer to market data, see {@link MarketDataRequirementNames}.
 */
public final class ValueRequirementNames {

  // TODO: Some names have spaces, some do not - make consistent

  // IMPORTANT: The contents of this class are used to produce public documentation. Please keep Javadoc comments
  // accurate and add new values to appropriate sections (or create new sections). Ideally a section should describe
  // a concept or logical grouping rather than a specific asset class, especially if the value name has meaning
  // for multiple asset classes. Lines starting with "/////" are treated as section breaks when producing
  // documentation, all other non-javadoc comments are ignored. The ordering here is preserved into the documentation
  // by default, keep things alphabetical unless another ordering makes sense.

  /**
   * Restricted constructor.
   */
  private ValueRequirementNames() {
  }

  ///// Market Data

  /**
   * Cost of carry for an equity or index option (i.e. continuously-compounded dividend yield).
   */
  public static final String COST_OF_CARRY = "Cost Of Carry";
  /**
   * TODO: single sentence description of DAILY_APPLIED_BETA
   */
  public static final String DAILY_APPLIED_BETA = "Last Raw Beta";
  /**
   * TODO: single sentence description of DAILY_MARKET_CAP
   */
  public static final String DAILY_MARKET_CAP = "Last Market Cap";
  /**
   * TODO: single sentence description of DAILY_PRICE
   */
  public static final String DAILY_PRICE = "Last Price";
  /**
   * TODO: single sentence description of DAILY_VOLUME
   */
  public static final String DAILY_VOLUME = "Last Volume";
  //  public static final String DAILY_VOLUME_AVG_5D = "Last Volume Avg 5D";
  //  public static final String DAILY_VOLUME_AVG_10D = "Last Volume Avg 10D";
  //  public static final String DAILY_VOLUME_AVG_20D = "Last Volume Avg 20D";
  //  public static final String DAILY_CALL_IMP_VOL_30D = "Last Call Implied Vol 30D";
  /**
   * TODO: single sentence description of MARK
   */
  public static final String MARK = "Mark";

  ///// Curves

  /**
   * Curve containing (date, discount factor) pairs. 
   */
  public static final String DISCOUNT_CURVE = "DiscountCurve";
  /**
   * TODO: single sentence description of FORWARD_CURVE
   */
  public static final String FORWARD_CURVE = "ForwardCurve";
  /**
   * TODO: single sentence description of FUTURE_PRICE_CURVE_DATA
   */
  public static final String FUTURE_PRICE_CURVE_DATA = "FuturePriceCurveData";
  /**
   * Curve containing (date, rate) pairs. 
   */
  public static final String YIELD_CURVE = "YieldCurve";
  /**
   * TODO: single sentence description of YIELD_CURVE_INTERPOLATED
   */
  public static final String YIELD_CURVE_INTERPOLATED = "YieldCurveInterpolated";
  /**
   * The Jacobian of a yield curve i.e. a matrix where each row is the sensitivity of an instrument used in yield curve construction to the nodal points of the curve.
   */
  public static final String YIELD_CURVE_JACOBIAN = "YieldCurveJacobian";
  /**
   * TODO: single sentence description of YIELD_CURVE_MARKET_DATA
   */
  public static final String YIELD_CURVE_MARKET_DATA = "YieldCurveMarketData";
  /**
   * The sensitivities of a cash-flow based fixed-income instrument to each of the nodal points in a yield curve.
   */
  public static final String YIELD_CURVE_NODE_SENSITIVITIES = "Yield Curve Node Sensitivities";
  /**
   * Curve property metadata. 
   */
  public static final String YIELD_CURVE_SPEC = "YieldCurveSpec";

  ///// Surfaces

  /**
   * TODO: single sentence description of HESTON_SURFACES
   */
  public static final String HESTON_SURFACES = "Heston Surfaces";
  /**
   * TODO: single sentence description of INTERPOLATED_VOLATILITY_SURFACE
   */
  public static final String INTERPOLATED_VOLATILITY_SURFACE = "InterpolatedVolatilitySurfaceData";
  /**
   * TODO: single sentence description of PIECEWISE_SABR_VOL_SURFACE
   */
  public static final String PIECEWISE_SABR_VOL_SURFACE = "Piecewise SABR fitted surface";
  /**
   * TODO: single sentence description of SABR_SURFACES
   */
  public static final String SABR_SURFACES = "SABR Surfaces";
  /**
   * TODO: single sentence description of STANDARD_VOLATILITY_SURFACE_DATA
   */
  public static final String STANDARD_VOLATILITY_SURFACE_DATA = "StandardVolatilitySurfaceData";
  /**
   * Surface containing (x, y, volatility) triples (where (x, y) can be (expiry, strike) (equity options) or (expiry, tenor) (swaptions). 
   */
  public static final String VOLATILITY_SURFACE = "VolatilitySurface";
  /**
   * Volatility surface metadata.
   */
  public static final String VOLATILITY_SURFACE_DATA = "VolatilitySurfaceData";
  /**
   * TODO: single sentence description of VOLATILITY_SURFACE_FITTED_POINTS
   */
  public static final String VOLATILITY_SURFACE_FITTED_POINTS = "Volatility Surface Fitted Points";
  /**
   * TODO: single sentence description of VOLATILITY_SURFACE_SPECIFICATION
   */
  public static final String VOLATILITY_SURFACE_SPEC = "VolatilitySurfaceSpecification";

  ///// Cubes

  /**
   * TODO: single sentence description of STANDARD_VOLATILITY_CUBE_DATA
   */
  public static final String STANDARD_VOLATILITY_CUBE_DATA = "StandardVolatilityCubeData";
  /**
   * TODO: single sentence description of VOLATILITY_CUBE
   */
  public static final String VOLATILITY_CUBE = "VolatilityCube";
  /**
   * TODO: single sentence description of VOLATILITY_CUBE_DEFN
   */
  public static final String VOLATILITY_CUBE_DEFN = "VolatilityCubeDefinition";
  /**
   * TODO: single sentence description of VOLATILITY_CUBE_FITTED_POINTS
   */
  public static final String VOLATILITY_CUBE_FITTED_POINTS = "Volatility Cube Fitted Points";
  /**
   * TODO: single sentence description of VOLATILITY_CUBE_MARKET_DATA
   */
  public static final String VOLATILITY_CUBE_MARKET_DATA = "VolatilityCubeMarketData";
  /**
   * TODO: single sentence description of VOLATILITY_CUBE_SPEC
   */
  public static final String VOLATILITY_CUBE_SPEC = "VolatilityCubeSpec";

  ///// Pricing

  /**
   * TODO: single sentence description of CREDIT_SENSITIVITIES
   */
  public static final String CREDIT_SENSITIVITIES = "Credit Sensitivities";
  /**
   * TODO: single sentence description of CS01
   */
  public static final String CS01 = "CS01";
  /**
   * TODO: single sentence description of DIVIDEND_YIELD
   */
  public static final String DIVIDEND_YIELD = "Dividend Yield";
  /**
   * TODO: single sentence description of DV01
   */
  public static final String DV01 = "DV01";
  /**
   * TODO: single sentence description of EXTERNAL_SENSITIVITIES
   */
  public static final String EXTERNAL_SENSITIVITIES = "External Sensitivities";
  /**
   * Fair value for a security (used for non-fixed income securities).
   */
  public static final String FAIR_VALUE = "FairValue";
  /**
   * The present value of a cash-flow based fixed-income instrument. 
   */
  public static final String PRESENT_VALUE = "Present Value";
  /**
   * The rate that prices a cash-flow based fixed-income instrument to zero. 
   */
  public static final String PAR_RATE = "Par Rate";
  /**
   * Sensitivity of par rate to a 1bp shift in the yield curve.
   */
  public static final String PAR_RATE_PARALLEL_CURVE_SHIFT = "Par Rate Parallel Shift Sensitivity";
  /**
   * Fair value for a position (used for non-fixed income securities - the number of trades multiplied by FAIR_VALUE).
   */
  public static final String POSITION_FAIR_VALUE = "PositionFairValue";
  /**
   * The PV01 of a cash-flow based fixed-income instrument.
   */
  public static final String PV01 = "PV01";
  /**
   * TODO: single sentence description of SECURITY_MARKET_PRICE
   */
  public static final String SECURITY_MARKET_PRICE = "Security Market Price";
  /**
   * TODO: single sentence description of SECURITY_IMPLIED_VOLATILITY
   */
  public static final String SECURITY_IMPLIED_VOLATLITY = "Security Implied Volatility";
  /**
   * Generic valuation of a security, for example it might be FAIR_VALUE or PRESENT_VALUE depending on the asset class.
   */
  public static final String VALUE = "Value";
  /**
   * Fair value for an option position (used for options - equal to the FAIR_VALUE multiplied by the number of trades and the point value). 
   */
  public static final String VALUE_FAIR_VALUE = "ValueFairValue";

  ///// Greeks

  /**
   * The carry rho of an option (first order derivative of price with respect to the cost of carry).
   */
  public static final String CARRY_RHO = "CarryRho";
  /**
   * The delta of an option (first order derivative of price with respect to the spot). 
   */
  public static final String DELTA = "Delta";
  /**
   * The delta bleed of an option (derivative of the delta with respect to the spot and time). 
   */
  public static final String DELTA_BLEED = "DeltaBleed";
  /**
   * TODO: single sentence description of DRIFTLESS_THETA
   */
  public static final String DRIFTLESS_THETA = "DriftlessTheta";
  /**
   * Second order derivative of delta with respect to the volatility. 
   */
  public static final String DVANNA_DVOL = "dVanna_dVol";
  /**
   * TODO: single sentence description of DZETA_DVOL
   */
  public static final String DZETA_DVOL = "dZeta_dVol";
  /**
   * TODO: single sentence description of ELASTICITY
   */
  public static final String ELASTICITY = "Elasticity";
  /**
   * The gamma of an option (second order derivative of price with respect to the spot). 
   */
  public static final String GAMMA = "Gamma";
  /**
   * The gamma bleed of an option (derivative of the gamma with respect to time). 
   */
  public static final String GAMMA_BLEED = "GammaBleed";
  /**
   * The percentage gamma of an option.
   */
  public static final String GAMMA_P = "GammaP";
  /**
   * The percentage gamma bleed. 
   */
  public static final String GAMMA_P_BLEED = "GammaPBleed";
  /**
   * TODO: single sentence description of PHI
   */
  public static final String PHI = "Phi";
  /**
   * The aggregate carry rho of an option (first order derivative of price with respect to the cost of carry).
   */
  public static final String POSITION_CARRY_RHO = "PositionCarryRho";
  /**
   * The aggregate delta of an option (first order derivative of price with respect to the spot). 
   */
  public static final String POSITION_DELTA = "PositionDelta";
  /**
   * The aggregate delta bleed of an option (derivative of the delta with respect to the spot and time). 
   */
  public static final String POSITION_DELTA_BLEED = "PositionDeltaBleed";
  /**
   * TODO: single sentence description of POSITION_DRIFTLESS_THETA
   */
  public static final String POSITION_DRIFTLESS_DELTA = "PositionDriftlessTheta";
  /**
   * Aggregate second order derivative of delta with respect to the volatility. 
   */
  public static final String POSITION_DVANNA_DVOL = "PositiondVanna_dVol";
  /**
   * TODO: single sentence description of POSITION_DZETA_DVOL
   */
  public static final String POSITION_DZETA_DVOL = "PositiondZeta_dVol";
  /**
   * TODO: single sentence description of POSITION_ELASTICITY
   */
  public static final String POSITION_ELASTICITY = "PositionElasticity";
  /**
   * The aggregate gamma of an option (second order derivative of price with respect to the spot).
   */
  public static final String POSITION_GAMMA = "PositionGamma";
  /**
   * The aggregate gamma bleed of an option (derivative of the gamma with respect to time). 
   */
  public static final String POSITION_GAMMA_BLEED = "PositionGammaBleed";
  /**
   * The aggregate percentage gamma of an option.
   */
  public static final String POSITION_GAMMA_P = "PositionGammaP";
  /**
   * The aggregate percentage gamma bleed. 
   */
  public static final String POSITION_GAMMA_P_BLEED = "PositionGammaPBleed";
  /**
   * TODO: single sentence description of POSITION_PHI
   */
  public static final String POSITION_PHI = "PositionPhi";
  /**
   * The aggregate rho of an option (first order derivative of price with respect to the interest rate). 
   */
  public static final String POSITION_RHO = "PositionRho";
  /**
   * The aggregate speed of an option (third order derivative of price with respect to the spot).
   */
  public static final String POSITION_SPEED = "PositionSpeed";
  /**
   * The aggregate strike delta of an option (first order derivative of price with respect to the strike). 
   */
  public static final String POSITION_STRIKE_DELTA = "PositionStrikeDelta";
  /**
   * The aggregate strike gamma of an option (second order derivative of price with respect to the strike).
   */
  public static final String POSITION_STRIKE_GAMMA = "PositionStrikeGamma";
  /**
   * The aggregate percentage speed.
   */
  public static final String POSITION_SPEED_P = "PositionSpeedP";
  /**
   * The aggregate theta of an option (first order derivative of price with respect to time).
   */
  public static final String POSITION_THETA = "PositionTheta";
  /**
   * The aggregate ultima of an option (third order derivative of price with respect to the volatility).
   */
  public static final String POSITION_ULTIMA = "PositionUltima";
  /**
   * The aggregate vanna of an option (first order derivative of delta with respect to the volatility).
   */
  public static final String POSITION_VANNA = "PositionVanna";
  /**
   * The aggregate ultima of an option (third order derivative of price with respect to the variance).
   */
  public static final String POSITION_VARIANCE_ULTIMA = "PositionVarianceUltima";
  /**
   * The aggregate variance vanna of an option (first order derivative of delta with respect to the variance).
   */
  public static final String POSITION_VARIANCE_VANNA = "PositionVarianceVanna";
  /**
   * The aggregate variance vega of an option (first order derivative of price with respect to the variance).
   */
  public static final String POSITION_VARIANCE_VEGA = "PositionVarianceVega";
  /**
   * The aggregate variance vomma of an option (second order derivative of price with respect to the variance). 
   */
  public static final String POSITION_VARIANCE_VOMMA = "PositionVarianceVomma";
  /**
   * The aggregate vega bleed of an option (derivative of the vega with respect to time).
   */
  public static final String POSITION_VEGA_BLEED = "PositionVegaBleed";
  /**
   * The aggregate vega of an option (first order derivative of price with respect to the volatility).
   */
  public static final String POSITION_VEGA = "PositionVega";
  /**
   * The aggregate percentage vega of an option. 
   */
  public static final String POSITION_VEGA_P = "PositionVegaP";
  /**
   * The aggregate vomma of an option (second order derivative of price with respect to the volatility).
   */
  public static final String POSITION_VOMMA = "PositionVomma";
  /**
   * The aggregate percentage vomma of an option. 
   */
  public static final String POSITION_VOMMA_P = "PositionVommaP";
  /**
   * TODO: single sentence description of POSITION_ZETA
   */
  public static final String POSITION_ZETA = "PositionZeta";
  /**
   * TODO: single sentence description of POSITION_ZETA_BLEED
   */
  public static final String POSITION_ZETA_BLEED = "PositionZetaBleed";
  /**
   * TODO: single sentence description of POSITION_ZOMMA
   */
  public static final String POSITION_ZOMMA = "PositionZomma";
  /**
   * TODO: single sentence description of POSITION_ZOMMA_P
   */
  public static final String POSITION_ZOMMA_P = "PositionZommaP";
  /**
   * The rho of an option (first order derivative of price with respect to the interest rate). 
   */
  public static final String RHO = "Rho";
  /**
   * The speed of an option (third order derivative of price with respect to the spot).
   */
  public static final String SPEED = "Speed";
  /**
   * The percentage speed.
   */
  public static final String SPEED_P = "SpeedP";
  /**
   * The strike delta of an option (first order derivative of price with respect to the strike). 
   */
  public static final String STRIKE_DELTA = "StrikeDelta";
  /**
   * The strike gamma of an option (second order derivative of price with respect to the strike).
   */
  public static final String STRIKE_GAMMA = "StrikeGamma";
  /**
   * The theta of an option (first order derivative of price with respect to time).
   */
  public static final String THETA = "Theta";
  /**
   * The ultima of an option (third order derivative of price with respect to the volatility).
   */
  public static final String ULTIMA = "Ultima";
  /**
   * The currency specific carry rho of an option (first order derivative of price with respect to the cost of carry).
   */
  public static final String VALUE_CARRY_RHO = "ValueCarryRho";
  /**
   * The currency specific delta of an option (first order derivative of price with respect to the spot). 
   */
  public static final String VALUE_DELTA = "ValueDelta";
  /**
   * The currency specific delta bleed of an option (derivative of the delta with respect to the spot and time). 
   */
  public static final String VALUE_DELTA_BLEED = "ValueDeltaBleed";
  /**
   * TODO: single sentence description of VALUE_DRIFTLESS_THETA
   */
  public static final String VALUE_DRIFTLESS_DELTA = "ValueDriftlessTheta";
  /**
   * Currency specific second order derivative of delta with respect to the volatility. 
   */
  public static final String VALUE_DVANNA_DVOL = "ValuedVanna_dVol";
  /**
   * TODO: single sentence description of VALUE_DZETA_DVOL
   */
  public static final String VALUE_DZETA_DVOL = "ValuedZeta_dVol";
  /**
   * TODO: single sentence description of VALUE_ELASTICITY
   */
  public static final String VALUE_ELASTICITY = "ValueElasticity";
  /**
   * The currency specific gamma of an option (second order derivative of price with respect to the spot). 
   */
  public static final String VALUE_GAMMA = "ValueGamma";
  /**
   * The currency specific gamma bleed of an option (derivative of the gamma with respect to time). 
   */
  public static final String VALUE_GAMMA_BLEED = "ValueGammaBleed";
  /**
   * The currency specific percentage gamma of an option.
   */
  public static final String VALUE_GAMMA_P = "ValueGammaP";
  /**
   * The currency specific percentage gamma bleed. 
   */
  public static final String VALUE_GAMMA_P_BLEED = "ValueGammaPBleed";
  /**
   * TODO: single sentence description of VALUE_PHI
   */
  public static final String VALUE_PHI = "ValuePhi";
  /**
   * The currency specific rho of an option (first order derivative of price with respect to the interest rate). 
   */
  public static final String VALUE_RHO = "ValueRho";
  /**
   * The currency specific speed of an option (third order derivative of price with respect to the spot).
   */
  public static final String VALUE_SPEED = "ValueSpeed";
  /**
   * The currency specific percentage speed.
   */
  public static final String VALUE_SPEED_P = "ValueSpeedP";
  /**
   * The currency specific strike delta of an option (first order derivative of price with respect to the strike). 
   */
  public static final String VALUE_STRIKE_DELTA = "ValueStrikeDelta";
  /**
   * The currency specific strike gamma of an option (second order derivative of price with respect to the strike).
   */
  public static final String VALUE_STRIKE_GAMMA = "ValueStrikeGamma";
  /**
   * The currency specific theta of an option (first order derivative of price with respect to time).
   */
  public static final String VALUE_THETA = "ValueTheta";
  /**
   * The currency specific ultima of an option (third order derivative of price with respect to the volatility).
   */
  public static final String VALUE_ULTIMA = "ValueUltima";
  /**
   * The currency specific vanna of an option (first order derivative of delta with respect to the volatility).
   */
  public static final String VALUE_VANNA = "ValueVanna";
  /**
   * The currency specific ultima of an option (third order derivative of price with respect to the variance).
   */
  public static final String VALUE_VARIANCE_ULTIMA = "ValueVarianceUltima";
  /**
   * The currency specific variance vanna of an option (first order derivative of delta with respect to the variance).
   */
  public static final String VALUE_VARIANCE_VANNA = "ValueVarianceVanna";
  /**
   * The currency specific variance vega of an option (first order derivative of price with respect to the variance).
   */
  public static final String VALUE_VARIANCE_VEGA = "ValueVarianceVega";
  /**
   * The currency specific variance vomma of an option (second order derivative of price with respect to the variance). 
   */
  public static final String VALUE_VARIANCE_VOMMA = "ValueVarianceVomma";
  /**
   * The currency specific vega of an option (first order derivative of price with respect to the volatility).
   */
  public static final String VALUE_VEGA = "ValueVega";
  /**
   * The currency specific vega bleed of an option (derivative of the vega with respect to time).
   */
  public static final String VALUE_VEGA_BLEED = "ValueVegaBleed";
  /**
   * The currency specific percentage vega of an option. 
   */
  public static final String VALUE_VEGA_P = "ValueVegaP";
  /**
   * The currency specific vomma of an option (second order derivative of price with respect to the volatility).
   */
  public static final String VALUE_VOMMA = "ValueVomma";
  /**
   * The currency specific percentage vomma of an option. 
   */
  public static final String VALUE_VOMMA_P = "ValueVommaP";
  /**
   * TODO: single sentence description of VALUE_ZETA
   */
  public static final String VALUE_ZETA = "ValueZeta";
  /**
   * TODO: single sentence description of VALUE_ZETA_BLEED
   */
  public static final String VALUE_ZETA_BLEED = "ValueZetaBleed";
  /**
   * TODO: single sentence description of VALUE_ZOMMA
   */
  public static final String VALUE_ZOMMA = "ValueZomma";
  /**
   * TODO: single sentence description of VALUE_ZOMMA_P
   */
  public static final String VALUE_ZOMMA_P = "ValueZommaP";
  /**
   * The vanna of an option (first order derivative of delta with respect to the volatility).
   */
  public static final String VANNA = "Vanna";
  /**
   * The ultima of an option (third order derivative of price with respect to the variance).
   */
  public static final String VARIANCE_ULTIMA = "VarianceUltima";
  /**
   * The variance vanna of an option (first order derivative of delta with respect to the variance).
   */
  public static final String VARIANCE_VANNA = "VarianceVanna";
  /**
   * The variance vega of an option (first order derivative of price with respect to the variance).
   */
  public static final String VARIANCE_VEGA = "VarianceVega";
  /**
   * The variance vomma of an option (second order derivative of price with respect to the variance). 
   */
  public static final String VARIANCE_VOMMA = "VarianceVomma";
  /**
   * The vega of an option (first order derivative of price with respect to the volatility).
   */
  public static final String VEGA = "Vega";
  /**
   * The vega bleed of an option (derivative of the vega with respect to time).
   */
  public static final String VEGA_BLEED = "VegaBleed";
  /**
   * TODO: single sentence descripton of VEGA_MATRIX
   */
  public static final String VEGA_MATRIX = "Vega Matrix";
  /**
   * The percentage vega an option.
   */
  public static final String VEGA_P = "VegaP";
  /**
   * TODO: single sentence descripton of VEGA_QUOTE_CUBE
   */
  public static final String VEGA_QUOTE_CUBE = "Vega Quote Cube";
  /**
   * TODO: single sentence descripton of VEGA_QUOTE_MATRIX
   */
  public static final String VEGA_QUOTE_MATRIX = "Vega Quote Matrix";
  /**
   * The vomma of an option (second order derivative of price with respect to the volatility).
   */
  public static final String VOMMA = "Vomma";
  /**
   * The percentage vomma of an option. 
   */
  public static final String VOMMA_P = "VommaP";
  /**
   * TODO: single sentence description of ZETA
   */
  public static final String ZETA = "Zeta";
  /**
   * TODO: single sentence description of ZETA_BLEED
   */
  public static final String ZETA_BLEED = "ZetaBleed";
  /**
   * TODO: single sentence description of ZOMMA
   */
  public static final String ZOMMA = "Zomma";
  /**
   * TODO: single sentence description of ZOMMA_P
   */
  public static final String ZOMMA_P = "ZommaP";

  ///// Series Analysis

  /**
   * TODO: single sentence description of DAILY_PNL
   */
  public static final String DAILY_PNL = "Daily PnL";
  /**
   * The Fisher kurtosis of a distribution (usually the return series of a security or its underlying).
   */
  public static final String FISHER_KURTOSIS = "Fisher Kurtosis";
  /**
   * The median of a set of values.
   */
  public static final String MEDIAN = "Median";
  /**
   * The Pearson kurtosis of a distribution (usually the return series of a security or its underlying).
   */
  public static final String PEARSON_KURTOSIS = "Pearson Kurtosis";
  /**
   * The P&L of a position, from reference date.
   */
  public static final String PNL = "PnL";
  /**
   * The P&L series of a position.
   */
  public static final String PNL_SERIES = "P&L Series";
  /**
   * The price series of a security.
   */
  public static final String PRICE_SERIES = "Price Series";
  /**
   * The return series of a security.
   */
  public static final String RETURN_SERIES = "Return Series";
  /**
   * The skew of a distribution (usually the return series of a security or its underlying).
   */
  public static final String SKEW = "Skew";
  /**
   * The sum of a set of values.
   */
  public static final String SUM = "Sum";
  /**
   * The return series of the underlying of a security (usually an option).
   */
  public static final String UNDERLYING_RETURN_SERIES = "Underlying Return Series";

  ///// Value At Risk

  /**
   * The VaR of a position or portfolio calculated using the historical P&L series (where the models make assumptions about the distribution e.g. assuming a Gaussian distribution). 
   */
  public static final String HISTORICAL_VAR = "HistoricalVaR";
  /**
   * The VaR of a position or portfolio calculated using the variance-covariance method (where the model can be first- or second-order). 
   */
  public static final String PARAMETRIC_VAR = "ParametricVaR";

  ///// Capital Asset Pricing Model

  /**
   * The beta of an equity position or portfolio calculated using the CAPM model. 
   */
  public static final String CAPM_BETA = "CAPM Beta";
  /**
   * The adjusted R-squared value of the regression. 
   */
  public static final String CAPM_REGRESSION_ADJUSTED_R_SQUARED = "CAPM Regression Adjusted R-Squared";
  /**
   * The alpha of an equity position or portfolio calculated using linear regression on the CAPM model. 
   */
  public static final String CAPM_REGRESSION_ALPHA = "CAPM Regression Alpha";
  /**
   * The p-value of alpha. 
   */
  public static final String CAPM_REGRESSION_ALPHA_PVALUES = "CAPM Regression Alpha p-Values";
  /**
   * The residual of the regression for alpha. 
   */
  public static final String CAPM_REGRESSION_ALPHA_RESIDUALS = "CAPM Regression Alpha Residual";
  /**
   * The t-statistic of alpha.
   */
  public static final String CAPM_REGRESSION_ALPHA_TSTATS = "CAPM Regression Alpha t-Stats";
  /**
   * The beta of an equity position or portfolio calculated using linear regression on the CAPM model. 
   */
  public static final String CAPM_REGRESSION_BETA = "CAPM Regression Beta";
  /**
   * The p-value of beta. 
   */
  public static final String CAPM_REGRESSION_BETA_PVALUES = "CAPM Regression Beta p-Values";
  /**
   * The residual of the regression for beta. 
   */
  public static final String CAPM_REGRESSION_BETA_RESIDUALS = "CAPM Regression Beta Residual";
  /**
   * The t-statistic of beta.
   */
  public static final String CAPM_REGRESSION_BETA_TSTATS = "CAPM Regression Beta t-Stats";
  /**
   * The mean squared error of the regression. 
   */
  public static final String CAPM_REGRESSION_MEAN_SQUARE_ERROR = "CAPM Regression Mean Square Error";
  /**
   * The R-squared value of the regression. 
   */
  public static final String CAPM_REGRESSION_R_SQUARED = "CAPM Regression R-Squared";
  /**
   * The standard error of alpha. 
   */
  public static final String CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA = "CAPM Regression Alpha Standard Error";
  /**
   * The standard error of beta. 
   */
  public static final String CAPM_REGRESSION_STANDARD_ERROR_OF_BETA = "CAPM Regression Beta Standard Error";

  ///// Traditional Risk-Reward

  /**
   * Jensen's alpha of an equity position or sub-portfolio in the portfolio. 
   */
  public static final String JENSENS_ALPHA = "Jensen's Alpha";
  /**
   * The Sharpe ratio of an equity position or sub-portfolio in the portfolio. 
   */
  public static final String SHARPE_RATIO = "Sharpe Ratio";
  /**
   * The total risk alpha of an equity position or sub-portfolio in the portfolio. 
   */
  public static final String TOTAL_RISK_ALPHA = "Total Risk Alpha";
  /**
   * The Treynor ratio of an equity position or sub-portfolio in the portfolio. 
   */
  public static final String TREYNOR_RATIO = "Treynor Ratio";
  /**
   * The weight of an equity position or sub-portfolio in the portfolio.
   */
  public static final String WEIGHT = "Weight";

  ///// Bonds

  /**
   * The return earned on a repo transaction expressed as an interest rate on the case side of the transaction.
   */
  public static final String ACTUAL_REPO = "Actual Repo";
  /**
   * The payment dates (actual settlement dates, not nominal) of the coupons and notional of a bond. 
   */
  public static final String BOND_COUPON_PAYMENT_TIMES = "Bond Coupon Payment Times";
  /**
   * The original tenor of a bond. 
   */
  public static final String BOND_TENOR = "Bond Tenor";
  /**
   * The clean price of a bond. 
   */
  public static final String CLEAN_PRICE = "Clean Price";
  /**
   * The conversion factor of a bond in the deliverable basket of a bond future (Note spelling mistake. To be fixed.).
   */
  public static final String CONVERTION_FACTOR = "Convertion Factor";
  /**
   * The convexity of a bond. 
   */
  public static final String CONVEXITY = "Convexity";
  /**
   * TODO: single sentence description of CURRENT_YIELD
   */
  public static final String CURRENT_YIELD = "Current Yield";
  /**
   * The dirty price of a bond. 
   */
  public static final String DIRTY_PRICE = "Dirty Price";
  /**
   * The gross basis of a bond in the deliverable basket of a bond future. 
   */
  public static final String GROSS_BASIS = "Gross Basis";
  /**
   * The implied repo rate of a bond in the deliverable basket of a bond future. 
   */
  public static final String IMPLIED_REPO = "Implied Repo";
  /**
   * The Macaulay duration of a bond. 
   */
  public static final String MACAULAY_DURATION = "Macaulay Duration";
  /**
   * The quoted market value of the clean price of a bond (ie excluding accrued interest).
   */
  public static final String MARKET_CLEAN_PRICE = "Market Clean Price";
  /**
   * The quoted market value of the dirty price of a bond (ie excluding accrued interest).
   */
  public static final String MARKET_DIRTY_PRICE = "Market Dirty Price";
  /**
   * The quoted market value of the yield to maturity of a bond.
   */
  public static final String MARKET_YTM = "Market Yield To Maturity";
  /**
   * TODO: single sentence description of MODIFIED_DURATION
   */
  public static final String MODIFIED_DURATION = "Modified Duration";
  /**
   * The net basis of a bond in the deliverable basket of a bond future. 
   */
  public static final String NET_BASIS = "Net Basis";
  /**
   * A bond curve calculated using the Nelson-Siegel method. 
   */
  public static final String NS_BOND_CURVE = "Nelson-Siegel Bond Curve";
  /**
   * A bond curve calculated using the Nelson-Siegel-Svennson. 
   */
  public static final String NSS_BOND_CURVE = "Nelson-Siegel-Svennson Bond Curve";
  /**
   * The sensitivity of a bond's PV to a unit change in the Z-spread.
   */
  public static final String PRESENT_VALUE_Z_SPREAD_SENSITIVITY = "PV Z Spread Sensitivity";
  /**
   * The yield to maturity of a bond. 
   */
  public static final String YTM = "Yield To Maturity";
  /**
   * The z-spread of a bond. 
   */
  public static final String Z_SPREAD = "Z Spread";

  ///// Fixed Income

  /**
   * TODO: single sentence description of FORWARD
   */
  public static final String FORWARD = "Forward";
  /**
   * TODO: single sentence description of PAR_RATE_CURVE_SENSITIVITY
   */
  public static final String PAR_RATE_CURVE_SENSITIVITY = "Par Rate Curve Sensitivity";
  /**
   * TODO: single sentence description of PRESENT_VALUE_COUPON_SENSITIVITY
   */
  public static final String PRESENT_VALUE_COUPON_SENSITIVITY = "Present Value Coupon Sensitivity";
  /**
   * TODO: single sentence description of PRESENT_VALUE_CURVE_SENSITIVITY
   */
  public static final String PRESENT_VALUE_CURVE_SENSITIVITY = "Present Value Curve Sensitivity";
  /**
   * TODO: single sentence description of PRESENT_VALUE_SABR_ALPHA_SENSITIVITY
   */
  public static final String PRESENT_VALUE_SABR_ALPHA_SENSITIVITY = "Present Value SABR Alpha Sensitivity";
  /**
   * TODO: single sentence description of PRESENT_VALUE_SABR_BETA_SENSITIVITY
   */
  public static final String PRESENT_VALUE_SABR_BETA_SENSITIVITY = "Present Value SABR Beta Sensitivity";
  /**
   * TODO: single sentence description of PRESENT_VALUE_SABR_RHO_SENSITIVITY
   */
  public static final String PRESENT_VALUE_SABR_RHO_SENSITIVITY = "Present Value SABR Rho Sensitivity";
  /**
   * TODO: single sentence description of PRESENT_VALUE_SABR_NU_SENSITIVITY
   */
  public static final String PRESENT_VALUE_SABR_NU_SENSITIVITY = "Present Value SABR Nu Sensitivity";

  ///// FX
  /**
   * TODO: single sentence description of FX_CURRENCY_EXPOSURE
   */
  public static final String FX_CURRENCY_EXPOSURE = "FX Currency Exposure";
  /**
   * TODO: single sentence description of FX_CURVE_SENSITIVITIES
   */
  public static final String FX_CURVE_SENSITIVITIES = "FX Curve Sensitivities";
  /**
   * TODO: single sentence description of FX_PRESENT_VALUE
   */
  public static final String FX_PRESENT_VALUE = "FX Present Value";
  /**
   * TODO: single sentence description of FX_VOLATILITY_SENSITIVITIES
   */
  public static final String FX_VOLATILITY_SENSITIVITIES = "FX Volatility Sensitivities";

  ///// Local Volatility
  //TODO this set of names might be too specific
  /**
   * TODO: single sentence description of BLACK_VOLATILITY_GRID_PRICE
   */
  public static final String BLACK_VOLATILITY_GRID_PRICE = "Black Price";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_DELTA
   */
  public static final String LOCAL_VOLATILITY_DELTA = "Forward Delta (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_DOMESTIC_PRICE
   */
  public static final String LOCAL_VOLATILITY_DOMESTIC_PRICE = "Domestic Price (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_DUAL_DELTA
   */
  public static final String LOCAL_VOLATILITY_DUAL_DELTA = "Dual Delta (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_DUAL_GAMMA
   */
  public static final String LOCAL_VOLATILITY_DUAL_GAMMA = "Dual Gamma (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_FOREX_PV_QUOTES
   */
  public static final String LOCAL_VOLATILITY_FOREX_PV_QUOTES = "Forex PV Quotes";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_FULL_PDE_GRID
   */
  public static final String LOCAL_VOLATILITY_FULL_PDE_GRID = "Full PDE Grid (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_GAMMA
   */
  public static final String LOCAL_VOLATILITY_GAMMA = "Forward Gamma (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_GRID_IMPLIED_VOL
   */
  public static final String LOCAL_VOLATILITY_GRID_IMPLIED_VOL = "Implied Vol (LV Black Equivalent)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_GRID_PRICE
   */
  public static final String LOCAL_VOLATILITY_GRID_PRICE = "Price (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_PDE_BUCKETED_VEGA
   */
  public static final String LOCAL_VOLATILITY_PDE_BUCKETED_VEGA = "PDE Bucketed Vega (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_PDE_GREEKS
   */
  public static final String LOCAL_VOLATILITY_PDE_GREEKS = "PDE Greeks (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_SURFACE
   */
  public static final String LOCAL_VOLATILITY_SURFACE = "Local Volatility Surface";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_VANNA
   */
  public static final String LOCAL_VOLATILITY_VANNA = "Forward Vanna (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_VEGA
   */
  public static final String LOCAL_VOLATILITY_VEGA = "Forward Vega (LV)";
  /**
   * TODO: single sentence description of LOCAL_VOLATILITY_VOMMA
   */
  public static final String LOCAL_VOLATILITY_VOMMA = "Forward Vomma (LV)";

}
