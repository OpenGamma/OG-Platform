/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.TargetSourcingFunction;

/**
 * Standard names used to refer to particular computed values.
 * <p>
 * These name are used as keys to define specific required values in the engine. They should be used by a {@link FunctionDefinition} to state their required inputs and their potential outputs. These
 * are a typical common set of names, which may be extended.
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
   * Cost of carry for an equity or index option (ie continuously-compounded dividend yield).
   */
  public static final String COST_OF_CARRY = "Cost Of Carry";
  /**
   * The beta of a stock as of the previous close
   */
  public static final String DAILY_APPLIED_BETA = "Last Raw Beta";
  /**
   * The market cap as of the previous close
   */
  public static final String DAILY_MARKET_CAP = "Last Market Cap";
  /**
   * The market value as of the previous close
   */
  public static final String DAILY_PRICE = "Last Price";
  /**
   * The daily volume as of the previous close
   */
  public static final String DAILY_VOLUME = "Last Volume";
  //  public static final String DAILY_VOLUME_AVG_5D = "Last Volume Avg 5D";
  //  public static final String DAILY_VOLUME_AVG_10D = "Last Volume Avg 10D";
  //  public static final String DAILY_VOLUME_AVG_20D = "Last Volume Avg 20D";
  //  public static final String DAILY_CALL_IMP_VOL_30D = "Last Call Implied Vol 30D";
  /**
   * The mark as of the previous close (e.g. equity price) <p>
   */
  public static final String MARK = "Mark";
  /**
   * Current value of a security.
   * This is typically the mid of bid/ask prices, but if these are not available, may be formed in another fashion
   */
  public static final String MARK_CURRENT = "Mark - Current";

  /**
   * Previous value of a security.
   * This is typically the mid of previous bid/ask prices, but if these are not available, may be formed in another fashion
   */
  public static final String MARK_PREVIOUS = "Mark - Previous";
  /**
   * Spot - General name for current value of underlying asset / index
   */
  public static final String SPOT = "Spot";
  /**
   * The spot rate for currency pair
   */
  public static final String SPOT_RATE = "SpotRate";
  /**
   * The spot rate for an FX option
   */
  public static final String SPOT_RATE_FOR_SECURITY = "Spot Rate For Security";
  /**
   * The percentage change between the last close price and live
   */
  public static final String SPOT_FX_PERCENTAGE_CHANGE = "Spot FX % Change";
  /**
   * The market price of the security underlying a trade or position.
   */
  public static final String SECURITY_MARKET_PRICE = "Security Market Price";
  /**
   * The market price of the underlying security in a compound security, such as an option
   */
  public static final String UNDERLYING_MARKET_PRICE = "Underlying Market Price";
  /**
   * For margined securities, the reference or margin price. This will either be the security's close price or,
   * on the transaction date itself, the traded price
   */
  public static final String MARGIN_PRICE = "Margin Price";
  /**
   * The historical time series of a quantity.
   */
  public static final String HISTORICAL_TIME_SERIES = "Historical Time Series";
  /**
   * The latest point from a historical time series of a quantity.
   */
  public static final String HISTORICAL_TIME_SERIES_LATEST = "Historical Time Series (latest value)";
  /**
   * A FX series for a security
   */
  public static final String HISTORICAL_FX_TIME_SERIES = "Historical FX Time Series";
  /**
   * The dates and payment amounts to be paid of the cash-flows of a security or portfolio
   */
  public static final String FIXED_PAY_CASH_FLOWS = "Pay Fixed Cash-Flows";
  /**
   * The dates and payment amounts to be received of the cash-flows of a security or portfolio
   */
  public static final String FIXED_RECEIVE_CASH_FLOWS = "Receive Fixed Cash-Flows";
  /**
   * The dates and netted amounts of the fixed cash-flows of a security or portfolio
   */
  public static final String NETTED_FIXED_CASH_FLOWS = "Netted Fixed Cash-Flows";
  /**
   * The payment dates, amounts and indices of the pay cash-flows of a security or portfolio
   */
  public static final String FLOATING_PAY_CASH_FLOWS = "Pay Floating Cash-Flows";
  /**
   * The payment dates, amounts and indices of the receive cash-flows of a security or portfolio
   */
  public static final String FLOATING_RECEIVE_CASH_FLOWS = "Receive Floating Cash-Flows";
  /**
   * The projected cash-flows to be paid of a security or portfolio
   */
  public static final String PROJECTED_FLOATING_PAY_CASH_FLOWS = "Projected Floating Pay Cash-Flows";
  /**
   * The projected cash-flows of a security or portfolio
   */
  public static final String PROJECTED_FLOATING_RECEIVE_CASH_FLOWS = "Projected Floating Receive Cash-Flows";

  /**
   * (For barrier options) The absolute difference between the nearest barrier and the spot.
   */
  public static final String BARRIER_DISTANCE = "BarrierDistance";

  ///// Curves
  /**
   * Curve containing (date, discount factor) pairs.
   */
  public static final String DISCOUNT_CURVE = "DiscountCurve";
  /**
   * Forward curve containing (time, forward rate) pairs.
   */
  public static final String FORWARD_CURVE = "ForwardCurve";
  /**
   * Curve containing (time, future price) pairs.
   */
  public static final String FUTURE_PRICE_CURVE_DATA = "FuturePriceCurveData";
  /**
   * Curve containing (time, rate) pairs.
   */
  public static final String YIELD_CURVE = "YieldCurve";
  /**
   * Curve containing (time, rate) pairs.
   */
  public static final String INSTANTANEOUS_FORWARD_CURVE = "InstantaneousForwardCurve";
  /**
   * Curve containing (time, price index) pairs.
   */
  public static final String PRICE_INDEX_CURVE = "PriceIndexCurve";
  /**
   * Hazard rate curve for credit instruments.
   */
  public static final String HAZARD_RATE_CURVE = "HazardRateCurve";
  /**
   * Credit spread curves.
   */
  public static final String CREDIT_SPREAD_CURVE = "CreditSpreadCurve";
  /**
   * The bundle of historical time series objects for nodes on a credit spread curve.
   */
  public static final String CREDIT_SPREAD_CURVE_HISTORICAL_TIME_SERIES = "Credit Spread Curve Historical Time Series";
  /**
   * The bundle of historical time series objects for instruments on a yield curve.
   */
  public static final String YIELD_CURVE_HISTORICAL_TIME_SERIES = "Yield Curve Historical Time Series";
  /**
   * The bundle of historical time series objects for instruments on an FX forward curve.
   */
  public static final String FX_FORWARD_CURVE_HISTORICAL_TIME_SERIES = "FX Forward Curve Historical Time Series";
  /**
   * The bundle of historical time series objects for instruments on a curve.
   */
  public static final String CURVE_HISTORICAL_TIME_SERIES = "Curve Historical Time Series";
  /**
   * The bundle of time series objects needed to convert instruments on a yield curve to their OG-Analytics derivative form.
   */
  public static final String YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES = "Yield Curve Instrument Conversion Historical Time Series";
  /**
   * The bundle of time series objects needed to convert instruments on a curve to their OG-Analytics derivative form.
   */
  public static final String CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES = "Curve Instrument Conversion Historical Time Series";
  /**
   * The bundle of time series objects needed to convert instruments on a yield curve to their OG-Analytics derivative form.
   */
  public static final String YIELD_CURVE_CONVERSION_HISTORICAL_TIME_SERIES = "Yield Curve Conversion Series";
  /**
   * A series of yield curves calculated using historical data
   */
  public static final String YIELD_CURVE_SERIES = "Yield Curve Series";
  /**
   * The FX matrix associated with a bundle of curves.
   */
  public static final String FX_MATRIX = "FX Matrix";
  /**
   * A set of parameters for the Hull-White one factor model.
   */
  public static final String HULL_WHITE_ONE_FACTOR_PARAMETERS = "Hull-White One Factor Parameters";
  /**
   * A set of parameters for the G2++ model.
   */
  public static final String G2PP_PARAMETERS = "G2pp Parameters";
  /**
   * Curve containing (time, rate) pairs that is constructed by directly interpolating between market data points (i.e. no settlement day corrections,
   * ignoring the type of instrument etc.).
   */
  public static final String YIELD_CURVE_INTERPOLATED = "YieldCurveInterpolated";
  /**
   * The Jacobian of a yield curve, that is a matrix where each row is the sensitivity of an instrument used in yield curve construction to the nodal points of the curve.
   */
  public static final String YIELD_CURVE_JACOBIAN = "YieldCurveJacobian";
  /**
   * The transition matrix between the sensitivity with respect to the new currency parameters and the initial currency market data.
   * Used for FX swaps implied curves calibration.
   */
  public static final String FX_IMPLIED_TRANSITION_MATRIX = "FXImpliedTransitionMatrix";
  /**
   * The raw market data that is used in yield curve construction.
   * @deprecated Use {@link #YIELD_CURVE_DATA} instead, values using this constant can't be manipulated in scenarios.
   */
  @Deprecated
  public static final String YIELD_CURVE_MARKET_DATA = "YieldCurveMarketData";
  /**
   * The yield curve specification and raw market data used in its construction.
   */
  public static final String YIELD_CURVE_DATA = "YieldCurveData";
  /**
   * The raw market data that is used in curve construction.
   */
  public static final String CURVE_MARKET_DATA = "CurveMarketData";
  /**
   * Data for an arbitrary surface.
   */
  public static final String SURFACE_MARKET_DATA = "SurfaceMarketData";
  /**
   * The sensitivities of a cash-flow based fixed-income instrument to each of the nodal points in a yield curve.
   */
  public static final String YIELD_CURVE_NODE_SENSITIVITIES = "Yield Curve Node Sensitivities";
  /**
   * The bucketed PV01.
   */
  public static final String BUCKETED_PV01 = "Bucketed PV01";
  /**
   * International Securities Identification Number (ISIN) uniquely identifies a security
   */
  public static final String ISIN = "ISIN";
  /**
   * Ticker symbol used to identify a security in Bloomberg. See {@link ExternalSchemes}
   */
  public static final String BLOOMBERG_TICKER = "BLOOMBERG_TICKER";
  /**
   * Bloomberg's older security identifier scheme.
   * They now wish users to adopt their new Bloomberg Global ID (BBGID). See {@link ExternalSchemes}
   */
  public static final String BLOOMBERG_BUID = "BLOOMBERG_BUID";
  /**
   * Curve property metadata.
   */
  public static final String YIELD_CURVE_SPEC = "YieldCurveSpec";
  /**
   * Curve specifications.
   */
  public static final String CURVE_SPECIFICATION = "CurveSpecification";
  /**
   * Curve definitions.
   */
  public static final String CURVE_DEFINITION = "CurveDefinition";
  /**
   * A vector of P&L series for the nodal points of a yield curve.
   */
  public static final String YIELD_CURVE_PNL_SERIES = "Yield Curve P&L Series";
  /**
   * A vector of P&L series for the nodal points of a curve.
   */
  public static final String CURVE_PNL_SERIES = "Curve P&L Series";
  /**
   * A vector of return series for the nodal points of a yield curve.
   */
  public static final String YIELD_CURVE_RETURN_SERIES = "Yield Curve Return Series";
  /**
   * A vector of return series for the nodal points of a curve.
   */
  public static final String CURVE_RETURN_SERIES = "Curve Return Series";
  /**
   * A vector of return series for the nodal points of an FX forward curve.
   */
  public static final String FX_FORWARD_CURVE_RETURN_SERIES = "FX Forward Curve Return Series";
  /**
   * Curve calculation property metadata
   */
  public static final String CURVE_CALCULATION_CONFIG = "CurveCalculationConfig";
  /**
   * Curve construction property metadata
   */
  public static final String CURVE_CONSTRUCTION_CONFIG = "CurveConstructionConfiguration";
  /**
   * The raw FX forward and spot market data that is used in curve construction
   */
  public static final String FX_FORWARD_CURVE_MARKET_DATA = "FXForwardCurveMarketData";
  /**
   * The raw FX forward points and spot market data
   */
  public static final String FX_FORWARD_POINTS_CURVE_MARKET_DATA = "FXForwardPointsCurveMarketData";
  /**
   * The sensitivities of an fx instrument to each of the nodal points in a FX forward points curve.
   */
  public static final String FX_FORWARD_POINTS_NODE_SENSITIVITIES = "FX Forward Points Node Sensitivities";
  /**
   * The raw forward swap and spot market data that is used in curve construction
   */
  public static final String FORWARD_SWAP_CURVE_MARKET_DATA = "ForwardSwapCurveMarketData";
  /**
   * FX forward curve definition
   */
  public static final String FX_FORWARD_CURVE_DEFINITION = "FXForwardCurveDefinition";
  /**
   * FX forward curve specification
   */
  public static final String FX_FORWARD_CURVE_SPECIFICATION = "FXForwardCurveSpecification";
  /**
   * A bundle of curves
   */
  public static final String CURVE_BUNDLE = "Curve Bundle";
  /**
   * A bundle of Jacobians
   */
  public static final String JACOBIAN_BUNDLE = "Jacobian Bundle";
  /**
   * The sensitivities to all curves in a bundle
   */
  public static final String BLOCK_CURVE_SENSITIVITIES = "Block Curve Sensitivities";
  /**
   * Currency pairs property metadata
   */
  public static final String CURRENCY_PAIRS = "CurrencyPairs";

  ///// Surfaces
  /**
   * A surface specification
   */
  public static final String SURFACE_SPECIFICATION = "SurfaceSpecification";
  /**
   * A surface definition
   */
  public static final String SURFACE_DEFINITION = "SurfaceDefinition";
  /**
   * Surface containing arrays of x, y, and  values for (x, y) pairs.
   */
  public static final String SURFACE_DATA = "SurfaceData";
  /**
   * Set of data containing surfaces of (x, y, parameter) triples, where the parameters are those in the Heston model.
   */
  public static final String HESTON_SURFACES = "Heston Surfaces";
  /**
   * Surface containing (x, y, volatility) triples that is constructed by directly interpolating market data.
   */
  public static final String INTERPOLATED_VOLATILITY_SURFACE = "InterpolatedVolatilitySurfaceData";
  /**
   * Surface containing (x, y, volatility) triples that is constructed by piecewise fitting the SABR model through the smiles.
   */
  public static final String PIECEWISE_SABR_VOL_SURFACE = "Piecewise SABR fitted surface";
  /**
   * Interpolator for Black volatility surfaces
   */
  public static final String BLACK_VOLATILITY_SURFACE_INTERPOLATOR = "BlackVolatilitySurfaceInterpolator";
  /**
   * Surface containing (x, y, Black volatility) triples
   */
  public static final String BLACK_VOLATILITY_SURFACE = "BlackVolatilitySurface";
  /**
   * Surface containing (x, y, Black volatility) triples, where the prices used to calculate the Black volatility surface have been corrected for dividends
   */
  public static final String PURE_VOLATILITY_SURFACE = "PureVolatilitySurface";
  /**
   * Set of data containing surfaces of (x, y, parameter) triples, where the parameters are those used in the SABR model.
   */
  public static final String SABR_SURFACES = "SABR Surfaces";
  /**
   * Surface containing (x, y, volatility) triples that are the outer join of the values on the x and y axes.
   */
  public static final String STANDARD_VOLATILITY_SURFACE_DATA = "StandardVolatilitySurfaceData";
  /**
   * Surface containing (x, y, volatility) triples for the vanna-volga FX volatility model
   */
  public static final String VANNA_VOLGA_VOLATILITY_SURFACE_DATA = "VannaVolgaVolatilitySurfaceData";
  /**
   * Surface containing (x, y, volatility) triples (where (x, y) can be (expiry, strike) (equity options) or (expiry, tenor) (swaptions).
   */
  public static final String VOLATILITY_SURFACE = "VolatilitySurface";
  /**
   * Surface containing arrays of x, y, and volatility values for (x, y) pairs.
   */
  public static final String VOLATILITY_SURFACE_DATA = "VolatilitySurfaceData";
  /**
   * Result containing information about which points were used in a smile fit.
   */
  public static final String VOLATILITY_SURFACE_FITTED_POINTS = "Volatility Surface Fitted Points";
  /**
   * A volatility surface specification
   */
  public static final String VOLATILITY_SURFACE_SPEC = "VolatilitySurfaceSpecification";
  /**
   * A volatility surface definition
   */
  public static final String VOLATILITY_SURFACE_DEFINITION = "VolatilitySurfaceDefinition";
  /**
   * The bundle of historical time series objects for elements of a volatility surface.
   */
  public static final String VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES = "Volatility Surface Historical Time Series";

  ///// Cubes

  /**
   * Cube containing sets of (x, y, z, volatility) that are the outer join of the values on the x, y and z axes.
   */
  public static final String STANDARD_VOLATILITY_CUBE_DATA = "StandardVolatilityCubeData";
  /**
   * Cube containing sets of (x, y, z, volatility)
   */
  public static final String VOLATILITY_CUBE = "VolatilityCube";
  /**
   * A volatility cube definition
   */
  public static final String VOLATILITY_CUBE_DEFN = "VolatilityCubeDefinition";
  /**
   * The set of market data that is used in constructing a cube.
   */
  public static final String VOLATILITY_CUBE_MARKET_DATA = "VolatilityCubeMarketData";
  /**
   * A volatility cube specification.
   */
  public static final String VOLATILITY_CUBE_SPEC = "VolatilityCubeSpecification";
  /**
   * The points that have been included in a fit
   */
  public static final String VOLATILITY_CUBE_FITTED_POINTS = "VolatilityCubeFittedPoints";

  ///// Volatility adjustments
  /**
   * The shifts to apply to a log-normal volatility surface
   */
  public static final String LOGNORMAL_SURFACE_SHIFTS = "LognormalSurfaceShifts";

  ///// Pricing

  /**
   * The credit sensitivities of an instrument
   */
  public static final String CREDIT_SENSITIVITIES = "Credit Sensitivities";
  /**
   * The change in the value of an instrument if the credit curve is moved by 1 basis point.
   */
  public static final String CS01 = "CS01";

  /**
   * The bucketed CS01 for a credit default swap.
   */
  public static final String BUCKETED_CS01 = "Bucketed CS01";

  /**
   * The parallel CS01 for a credit default swap.
   */
  public static final String PARALLEL_CS01 = "Parallel CS01";

  /**
   * The second-order change in the value of an instrument if the credit curve is moved by 1 basis point.
   */
  public static final String GAMMA_CS01 = "Gamma CS01";

  /**
   * The bucketed second-order changes in the value of an instrument if the credit curve is moved by 1 basis point.
   */
  public static final String BUCKETED_GAMMA_CS01 = "Bucketed Gamma CS01";

  /**
   * The change in the value of an instrument if the recovery rate is moved by one basis point.
   */
  public static final String RR01 = "RR01";

  /**
   * The change in the value of an instrument if the yield curve is shifted in parallel by one basis point.
   */
  public static final String IR01 = "IR01";

  /**
   * The bucketed changes in the value of an instrument if the interest rate curve is moved by 1 basis point.
   */
  public static final String BUCKETED_IR01 = "Bucketed IR01";

  /**
   * The accrued premium for a credit default swap.
   */
  public static final String ACCRUED_PREMIUM = "Accrued Premium ";

  /**
   * The accrued days for a credit default swap.
   */
  public static final String ACCRUED_DAYS = "Accrued Days";

  /**
   * The upfront ammount for a credit default swap.
   */
  public static final String UPFRONT_AMOUNT = "Upfront Amount";

  /**
   * The points upfront for a credit default swap.
   */
  public static final String POINTS_UPFRONT = "Points Upfront";

  /**
   * The spread for a credit default swap.
   */
  public static final String QUOTED_SPREAD = "Quoted Spread";

  /**
   * The principal for a credit default swap.
   */
  public static final String PRINCIPAL = "Principal";

  /**
   * The clean present value for a credit default swap.
   */
  public static final String CLEAN_PRESENT_VALUE = "Clean Present Value";

  /**
   * The dirty present value for a credit default swap.
   */
  public static final String DIRTY_PRESENT_VALUE = "Dirty Present Value";

  /**
   * The jump-to-default.
   */
  public static final String JUMP_TO_DEFAULT = "Jump to Default";

  /**
   * The bucketed (CS01) spreads for a credit default swap.
   */
  public static final String BUCKETED_SPREADS = "Bucketed Spreads";

  /**
   * The pillar spreads used for pricing a credit default swap.
   */
  public static final String PILLAR_SPREADS = "Pillar Spreads";

  /**
   * The hedge notional, a matrix of notionals and tenors required to hedge an instrument.
   */
  public static final String HEDGE_NOTIONAL = "Hedge Notional";

  /**
   * The dividend yield of an equity or equity index.
   */
  public static final String DIVIDEND_YIELD = "Dividend Yield";
  /**
   * The change in the dollar value of an instrument if a yield curve is moved by one basis point.
   */
  public static final String DV01 = "DV01";
  /**
   * Sensitivities that are externally provided, not calculated by OpenGamma functions
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
   * The present value of the pay leg of a swap.
   */
  public static final String PAY_LEG_PRESENT_VALUE = "Swap Pay Leg Present Value";
  /**
   * The present value of the receive leg of a swap.
   */
  public static final String RECEIVE_LEG_PRESENT_VALUE = "Swap Receive Leg Present Value";
  /**
   * The details of a swap pay leg.
   */
  public static final String SWAP_PAY_LEG_DETAILS = "Swap Pay Leg Details";
  /**
   * The details of a swap receive leg.
   */
  public static final String SWAP_RECEIVE_LEG_DETAILS = "Swap Receive Leg Details";
  /**
   * The rate that prices a cash-flow based fixed-income instrument to zero.
   */
  public static final String PAR_RATE = "Par Rate";
  /**
   * The spread that must be added to the market quote of an instrument to produce a present value of zero
   */
  public static final String PAR_SPREAD = "Par Spread";
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
   * All PV01s of a cash-flow based fixed-income instrument.
   */
  public static final String ALL_PV01S = "All PV01s";
  /**
   * The Gamma PV01 of a cash-flow based fixed-income instrument.
   */
  public static final String GAMMA_PV01 = "Gamma PV01";
  /**
   * Details of a bond cash-flow leg.
   */
  public static final String BOND_DETAILS = "Bond Details";
  /**
   * The implied volatility of a security.
   */
  public static final String SECURITY_IMPLIED_VOLATILITY = "Security Implied Volatility";
  /**
   * The model price of the security, as opposed to trade or position.
   */
  public static final String SECURITY_MODEL_PRICE = "Security Model Price";
  /**
   * The model price of the underlying security in a compound security, such as an option
   */
  public static final String UNDERLYING_MODEL_PRICE = "Underlying Model Price";
  /**
   * Generic valuation of a security, for example it might be FAIR_VALUE or PRESENT_VALUE depending on the asset class.
   */
  public static final String VALUE = "Value";
  /**
   * Fair value for an option position (used for options - equal to the FAIR_VALUE multiplied by the number of trades and the point value).
   */
  public static final String VALUE_FAIR_VALUE = "ValueFairValue";
  /**
   * The convexity adjustment - the difference between the price and the par rate of an instrument
   */
  public static final String CONVEXITY_ADJUSTMENT = "Convexity Adjustment";
  /**
   * The net amount of an inflation coupon at the start of a month with respect to the reference index.
   */
  public static final String INFLATION_NET_AMOUNT = "Inflation Net Amount";
  /**
   * The market quoted value of an instrument (e.g. 0.99 for a Eurodollar future)
   */
  public static final String MARKET_QUOTE = "Market Quote";
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
   * The driftless theta of an option (the time decay of an option without considering the drift of the underlying or interest rates).
   */
  public static final String DRIFTLESS_THETA = "DriftlessTheta";
  /**
   * Second order derivative of delta with respect to the volatility.
   */
  public static final String DVANNA_DVOL = "dVanna_dVol";
  /**
   * First order derivative of the in-the-money probability (zeta) with respect to the volatility.
   */
  public static final String DZETA_DVOL = "dZeta_dVol";
  /**
   * The sensitivity in percent to a percent change in the underlying.
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
   * The first order derivative with respect to the yield
   */
  public static final String PHI = "Phi";
  /**
   * The aggregate carry rho of an option (first order derivative of price with respect to the cost of carry).
   */
  public static final String POSITION_CARRY_RHO = "PositionCarryRho";
  /**
   * The aggregate delta of an option position (change in the value of the {@link Position} with respect to the underlying).
   */
  public static final String POSITION_DELTA = "PositionDelta";
  /**
   * The aggregate delta bleed of an option (derivative of the delta with respect to the spot and time).
   */
  public static final String POSITION_DELTA_BLEED = "PositionDeltaBleed";
  /**
   * The aggregate driftless theta of an option (the time decay of an option without considering the drift of the underlying or interest rates).
   */
  public static final String POSITION_DRIFTLESS_THETA = "PositionDriftlessTheta";
  /**
   * Aggregate second order derivative of delta with respect to the volatility.
   */
  public static final String POSITION_DVANNA_DVOL = "PositiondVanna_dVol";
  /**
   * Aggregate first order derivative of the in-the-money probability (zeta) with respect to the volatility.
   */
  public static final String POSITION_DZETA_DVOL = "PositiondZeta_dVol";
  /**
   * The aggregate sensitivity in percent to a percent change in the underlying.
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
   * The aggregate first order derivative with respect to the yield
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
   * The aggregate vega of an option with a weighting factor related to square root of time to expiry
   */
  public static final String POSITION_WEIGHTED_VEGA = "PositionWeightedVega";
  /**
   * The aggregate vomma of an option (second order derivative of price with respect to the volatility).
   */
  public static final String POSITION_VOMMA = "PositionVomma";
  /**
   * The aggregate percentage vomma of an option.
   */
  public static final String POSITION_VOMMA_P = "PositionVommaP";
  /**
   * The aggregate in-the-money probability of an option.
   */
  public static final String POSITION_ZETA = "PositionZeta";
  /**
   * The aggregate of the time derivative of the in-the-money probability of an option.
   */
  public static final String POSITION_ZETA_BLEED = "PositionZetaBleed";
  /**
   * The aggregate of the time derivative of the gamma of an option.
   */
  public static final String POSITION_ZOMMA = "PositionZomma";
  /**
   * The aggregate of the time derivative of the percentage gamma of an option.
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
   * The amount by which the value of a portfolio would change due to carry rho.
   */
  public static final String VALUE_CARRY_RHO = "ValueCarryRho";
  /**
   * ValueDelta represents the cash value of the position or, the value of money one would make if the underlying increased in price by 100%.<p>
   * {@link #DELTA} = dV/dS.  ValueDelta is defined as S(t) * dV/dS. <p>
   * Observe: PNL = dV/dS * (change in S) = S(t) * dV/dS * (S(T) - S(t)) / S(t), thus S(t)* dV/dS (ValueDelta) would be the PNL if 1.0 = (S(T) - S(t)) / S(t) => S(T) = 2*S(t),
   * i.e. if the underlying doubled (increased by 100%). It thus gives a measure of the sensitivity as a relative measure.
   */
  public static final String VALUE_DELTA = "ValueDelta";
  /**
   * The amount by which the value of a portfolio would change due to delta bleed.
   */
  public static final String VALUE_DELTA_BLEED = "ValueDeltaBleed";
  /**
   * The amount by which the value of a portfolio would change due to driftless theta.
   */
  public static final String VALUE_DRIFTLESS_DELTA = "ValueDriftlessTheta";
  /**
   * The amount by which the value of a portfolio would change due to dual delta.
   */
  public static final String VALUE_DUAL_DELTA = "ValueDualDelta";
  /**
   * The amount by which the value of a portfolio would change due to dVannadVol.
   */
  public static final String VALUE_DVANNA_DVOL = "ValuedVanna_dVol";
  /**
   * The amount by which the value of a portfolio would change due to dZetadVol.
   */
  public static final String VALUE_DZETA_DVOL = "ValuedZeta_dVol";
  /**
   * The amount by which the value of a portfolio would change due to elasticity.
   */
  public static final String VALUE_ELASTICITY = "ValueElasticity";
  /**
   * The amount by which the value of a portfolio would change due to gamma.
   */
  public static final String VALUE_GAMMA = "ValueGamma";
  /**
   * The amount by which the value of a portfolio would change due to gamma bleed.
   */
  public static final String VALUE_GAMMA_BLEED = "ValueGammaBleed";
  /**
   * The amount by which the value of a portfolio would change due to percentage gamma.
   */
  public static final String VALUE_GAMMA_P = "ValueGammaP";
  /**
   * The amount by which the value of a portfolio would change due to gamma bleed.
   */
  public static final String VALUE_GAMMA_P_BLEED = "ValueGammaPBleed";
  /**
   * The amount by which the value of a portfolio would change due to phi.
   */
  public static final String VALUE_PHI = "ValuePhi";
  /**
   * The amount by which the value of a portfolio would change due to rho.
   */
  public static final String VALUE_RHO = "ValueRho";
  /**
   * The amount by which the value of a portfolio would change due to speed.
   */
  public static final String VALUE_SPEED = "ValueSpeed";
  /**
   * The amount by which the value of a portfolio would change due to percentage speed.
   */
  public static final String VALUE_SPEED_P = "ValueSpeedP";
  /**
   * The amount by which the value of a portfolio would change due to strike delta.
   */
  public static final String VALUE_STRIKE_DELTA = "ValueStrikeDelta";
  /**
   * The amount by which the value of a portfolio would change due to strike gamma.
   */
  public static final String VALUE_STRIKE_GAMMA = "ValueStrikeGamma";
  /**
   * The amount by which the value of a portfolio would change due to theta.
   */
  public static final String VALUE_THETA = "ValueTheta";
  /**
   * The amount by which the value of a portfolio would change due to ultima.
   */
  public static final String VALUE_ULTIMA = "ValueUltima";
  /**
   * The amount by which the value of a portfolio would change due to vanna.
   */
  public static final String VALUE_VANNA = "ValueVanna";
  /**
   * The amount by which the value of a portfolio would change due to variance ultima.
   */
  public static final String VALUE_VARIANCE_ULTIMA = "ValueVarianceUltima";
  /**
   * The amount by which the value of a portfolio would change due to variance vanna.
   */
  public static final String VALUE_VARIANCE_VANNA = "ValueVarianceVanna";
  /**
   * The amount by which the value of a portfolio would change due to variance vega.
   */
  public static final String VALUE_VARIANCE_VEGA = "ValueVarianceVega";
  /**
   * The amount by which the value of a portfolio would change due to variance vomma.
   */
  public static final String VALUE_VARIANCE_VOMMA = "ValueVarianceVomma";
  /**
   * The amount by which the value of a portfolio would change due to vega.
   */
  public static final String VALUE_VEGA = "ValueVega";
  /**
   * The amount by which the value of a portfolio would change due to vega bleed.
   */
  public static final String VALUE_VEGA_BLEED = "ValueVegaBleed";
  /**
   * The amount by which the value of a portfolio would change due to percentage vega.
   */
  public static final String VALUE_VEGA_P = "ValueVegaP";
  /**
   * The amount by which the value of a portfolio would change due to vomma.
   */
  public static final String VALUE_VOMMA = "ValueVomma";
  /**
   * The amount by which the value of a portfolio would change due to percentage vomma.
   */
  public static final String VALUE_VOMMA_P = "ValueVommaP";
  /**
   * The amount by which the value of a portfolio would change due to zeta.
   */
  public static final String VALUE_ZETA = "ValueZeta";
  /**
   * The amount by which the value of a portfolio would change due to zeta bleed.
   */
  public static final String VALUE_ZETA_BLEED = "ValueZetaBleed";
  /**
   * The amount by which the value of a portfolio would change due to zomma.
   */
  public static final String VALUE_ZOMMA = "ValueZomma";
  /**
   * The amount by which the value of a portfolio would change due to percentage zomma.
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
   * The bucketed vega of a security for a (expiry, delta) volatility surface.
   */
  public static final String VEGA_MATRIX = "Vega Matrix";
  /**
   * The percentage vega an option.
   */
  public static final String VEGA_P = "VegaP";
  /**
   * The vega of an option with a weighting factor related to square root of time to expiry
   */
  public static final String WEIGHTED_VEGA = "Weighted Vega";
  /**
   * The bucketed vega of a security to the market data volatility cube.
   */
  public static final String VEGA_QUOTE_CUBE = "Vega Quote Cube";
  /**
   * The bucketed vega of a security to the market data volatility surface.
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
   * The in-the-money probability of an option
   */
  public static final String ZETA = "Zeta";
  /**
   * The time derivative of the in-the-money probability of an option.
   */
  public static final String ZETA_BLEED = "ZetaBleed";
  /**
   * The time derivative of the gamma of an option.
   */
  public static final String ZOMMA = "Zomma";
  /**
   * The time derivative of the percentage gamma of an option.
   */
  public static final String ZOMMA_P = "ZommaP";
  /**
   * The monetized vega.
   */
  public static final String MONETIZED_VEGA = "Monetized Vega";

  ///// Series Analysis

  /**
   * A correlation matrix. This should be a labeled matrix with {@link ValueSpecification} labels that indicate the original components.
   */
  public static final String CORRELATION_MATRIX = "Correlation Matrix";
  /**
   * A covariance matrix. This should be a labeled matrix with {@link ValueSpecification} labels that indicate the original components.
   */
  public static final String COVARIANCE_MATRIX = "Covariance Matrix";
  /**
   * The daily profit and loss of a security
   */
  public static final String DAILY_PNL = "Daily PnL";
  /**
   * The daily profit and loss of an exchange traded security
   */
  public static final String MTM_PNL = "Mark-to-Market P&L";

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
  /**
   * The notional of a security
   */
  public static final String NOTIONAL = "Notional";
  /**
   * The attributes of a security
   */
  public static final String ATTRIBUTES = "Attributes";
  /**
   * An ExternalId of a security
   */
  public static final String EXTERNAL_ID = "ExternalId";
  /** 
   * The realized variance of a time series.
   */
  public static final String REALIZED_VARIANCE = "Realized Variance";

  ///// Value At Risk

  /**
   * The VaR of a position or portfolio calculated using the historical P&L series.
   */
  public static final String HISTORICAL_VAR = "HistoricalVaR";
  /**
   * The standard deviation for VaR calculated using the historical P&L series. Should be combined with {@link #HISTORICAL_VAR}.
   */
  public static final String HISTORICAL_VAR_STDDEV = "HistoricalVaR Standard Deviation";
  /**
   * The conditional VaR of a position or portfolio calculated using a historical P&L series.
   */
  public static final String CONDITIONAL_HISTORICAL_VAR = "HistoricalCVaR";
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
   * The conversion factor of a bond in the deliverable basket of a bond future (note spelling mistake - to be fixed).
   */
  public static final String CONVERTION_FACTOR = "Convertion Factor";
  /**
   * The convexity of a bond.
   */
  public static final String CONVEXITY = "Convexity";
  /**
   * The accrued interest of a bond.
   */
  public static final String ACCRUED_INTEREST = "Accrued Interest";
  /**
   * The current yield of a bond
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
   * The modified duration of a bond.
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
   * The forward value of a security
   */
  public static final String FORWARD = "Forward";

  /**
   * The forward price of a security
   */
  public static final String FORWARD_PRICE = "Forward Price";

  /**
   * The sensitivity of the par rate of a cash-flow instrument to a shift of 100 percent in the (named) yield curve.
   */
  public static final String PAR_RATE_CURVE_SENSITIVITY = "Par Rate Curve Sensitivity";
  /**
   * The sensitivity of the present value to the value of any fixed coupons of a cash-flow instrument.
   */
  public static final String PRESENT_VALUE_COUPON_SENSITIVITY = "Present Value Coupon Sensitivity";
  /**
   * The sensitivity of the present value to points on the yield curve at every point a cash-flow instrument has sensitivity.
   */
  public static final String PRESENT_VALUE_CURVE_SENSITIVITY = "Present Value Curve Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the alpha parameter of the SABR model.
   */
  public static final String PRESENT_VALUE_SABR_ALPHA_SENSITIVITY = "Present Value SABR Alpha Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the beta parameter of the SABR model.
   */
  public static final String PRESENT_VALUE_SABR_BETA_SENSITIVITY = "Present Value SABR Beta Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the rho parameter of the SABR model.
   */
  public static final String PRESENT_VALUE_SABR_RHO_SENSITIVITY = "Present Value SABR Rho Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the nu parameter of the SABR model.
   */
  public static final String PRESENT_VALUE_SABR_NU_SENSITIVITY = "Present Value SABR Nu Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the alpha parameter of the SABR model. Sensitivity to the grid node points.
   */
  public static final String PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY = "Present Value SABR Alpha Node Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the beta parameter of the SABR model. Sensitivity to the grid node points.
   */
  public static final String PRESENT_VALUE_SABR_BETA_NODE_SENSITIVITY = "Present Value SABR Beta Node Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the rho parameter of the SABR model. Sensitivity to the grid node points.
   */
  public static final String PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY = "Present Value SABR Rho Node Sensitivity";
  /**
   * The sensitivity of the present value of an instrument to the nu parameter of the SABR model. Sensitivity to the grid node points.
   */
  public static final String PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY = "Present Value SABR Nu Node Sensitivity";

  ///// FX
  /**
   * The currency exposure of a FX instrument
   */
  public static final String FX_CURRENCY_EXPOSURE = "FX Currency Exposure";
  /**
   * The sensitivities of the present value of a FX instrument to the curves to which it is sensitive.
   */
  public static final String FX_CURVE_SENSITIVITIES = "FX Curve Sensitivities";
  /**
   * The present value in both currencies of a FX instrument.
   */
  public static final String FX_PRESENT_VALUE = "FX Present Value";

  /**
   * The value vega of a digital option that is priced using call spread replication
   */
  //TODO too specific? Only in here because we can't add the outputs for digital options (which has two in this case) and normal value vega
  public static final String CALL_SPREAD_VALUE_VEGA = "Call Spread Value Vega";

  ///// Local Volatility
  //TODO this set of names might be too specific
  /**
   * Result containing the Black price of an option at each of the points at the option maturity on a PDE grid.
   */
  public static final String BLACK_VOLATILITY_GRID_PRICE = "Black Price";
  /**
   * The forward delta of an instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_DELTA = "Forward Delta (LV)";
  /**
   * The domestic price of a FX instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_DOMESTIC_PRICE = "Domestic Price (LV)";
  /**
   * The dual delta of an instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_DUAL_DELTA = "Dual Delta (LV)";
  /**
   * The dual gamma of an instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_DUAL_GAMMA = "Dual Gamma (LV)";
  /**
   * The pips present value of a FX instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_FOREX_PV_QUOTES = "Forex PV Quotes";
  /**
   * The full PDE grid generated when calibrating a local volatility surface.
   */
  public static final String LOCAL_VOLATILITY_FULL_PDE_GRID = "Full PDE Grid (LV)";
  /**
   * The forward gamma of an instrument calculated using local volatility PDE methods.
   */
  public static final String LOCAL_VOLATILITY_GAMMA = "Forward Gamma (LV)";
  /**
   * Result containing the equivalent Black volatilities of an option at each of the points at the option maturity on a PDE grid.
   */
  public static final String LOCAL_VOLATILITY_GRID_IMPLIED_VOL = "Implied Vol (LV Black Equivalent)";
  /**
   * Result containing the price calculated using local volatility of an option at each of the points at the option maturity on a PDE grid.
   */
  public static final String LOCAL_VOLATILITY_GRID_PRICE = "Price (LV)";
  /**
   * Result containing the bucketed vega of an option calculated using a PDE and local volatility
   */
  public static final String LOCAL_VOLATILITY_PDE_BUCKETED_VEGA = "PDE Bucketed Vega (LV)";
  /**
   * Result containing the greeks of an option calculated using a PDE and local volatility
   */
  public static final String LOCAL_VOLATILITY_PDE_GREEKS = "PDE Greeks (LV)";
  /**
   * Surface containing (x, y, volatility) triples calculated using the Dupire local volatility method.
   */
  public static final String LOCAL_VOLATILITY_SURFACE = "LocalVolatilitySurface";
  /**
   * The vanna of an instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_VANNA = "Forward Vanna (LV)";
  /**
   * The vega of an instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_VEGA = "Forward Vega (LV)";
  /**
   * The vomma of an instrument calculated using local volatility and PDE methods.
   */
  public static final String LOCAL_VOLATILITY_VOMMA = "Forward Vomma (LV)";
  /**
   * The forward delta of an instrument
   */
  public static final String FORWARD_DELTA = "Forward Delta";
  /**
   * The forward driftless theta of an instrument
   */
  public static final String FORWARD_DRIFTLESS_THETA = "Forward Driftless Theta";
  /**
   * The forward gamma of an instrument
   */
  public static final String FORWARD_GAMMA = "Forward Gamma";
  /**
   * The forward vega of an instrument
   */
  public static final String FORWARD_VEGA = "Forward Vega";
  /**
   * The forward vanna of an instrument
   */
  public static final String FORWARD_VANNA = "Forward Vanna";
  /**
   * The forward vomma of an instrument
   */
  public static final String FORWARD_VOMMA = "Forward Vomma";
  /**
   * The dual delta of an instrument
   */
  public static final String DUAL_DELTA = "Dual Delta";
  /**
   * The dual gamma of an instrument
   */
  public static final String DUAL_GAMMA = "Dual Gamma";
  /**
   * The domestic price for FX options
   */
  public static final String FOREX_DOMESTIC_PRICE = "Forex Domestic Price";
  /**
   * The PV quote form of FX option prices
   */
  public static final String FOREX_PV_QUOTES = "Forex PV Quotes";
  /**
   * The implied volatility
   */
  public static final String IMPLIED_VOLATILITY = "Implied Volatility";
  /**
   * The PDE space grid values of the forward delta of an instrument
   */
  public static final String GRID_FORWARD_DELTA = "Grid Forward Delta";
  /**
   * The PDE space grid values of the forward gamma of an instrument
   */
  public static final String GRID_FORWARD_GAMMA = "Grid Forward Gamma";
  /**
   * The PDE space grid values of the forward vega of an instrument
   */
  public static final String GRID_FORWARD_VEGA = "Grid Forward Vega";
  /**
   * The PDE space grid values of the forward vanna of an instrument
   */
  public static final String GRID_FORWARD_VANNA = "Grid Forward Vanna";
  /**
   * The PDE space grid values of the forward vomma of an instrument
   */
  public static final String GRID_FORWARD_VOMMA = "Grid Forward Vomma";
  /**
   * The PDE space grid values of the dual delta of an instrument
   */
  public static final String GRID_DUAL_DELTA = "Grid Dual Delta";
  /**
   * The PDE space grid values of the dual gamma of an instrument
   */
  public static final String GRID_DUAL_GAMMA = "Grid Dual Gamma";
  /**
   * The PDE space grid values of the implied volatility
   */
  public static final String GRID_IMPLIED_VOLATILITY = "Grid Implied Volatility";
  /**
   * The PDE space grid values of the price
   */
  public static final String GRID_PRESENT_VALUE = "Grid Present Value";

  /**
   * The affine dividends of an index or equity
   */
  public static final String AFFINE_DIVIDENDS = "Affine Dividends";

  ///// Miscellaneous

  /**
   * The object referenced by a target specification. Can be used to load an item, referenced by a target specification, directly from a database and receive it as an input to a function. See the
   * documentation for {@link TargetSourcingFunction} for details on requesting target resolved items.
   */
  public static final String TARGET = "Target";

  // TODO what section should this be in
  /**
   * The quantity of a position or trade.
   */
  public static final String QUANTITY = "Quantity";

  /**
   * The quantity of a position or trade.
   */
  public static final String PAY_REC = "Pay/Receive";

  /**
   * Frequency of a position or trade.
   */
  public static final String FREQUENCY = "Frequency";

  /**
   * Float frequency of a position or trade.
   */
  public static final String FLOAT_FREQUENCY = "Float Frequency";

  /**
   * Index of a position or trade.
   */
  public static final String INDEX = "Index";

  /**
   * Maturity date of a position or trade
   */
  public static final String MATURITY = "Maturity Date";

  /**
   * Product of a position or trade.
   */
  public static final String PRODUCT = "Product";
  /**
   * Rate of a position or trade.
   */
  public static final String RATE = "Rate";
  /**
   * Start date of a position or trade.
   */
  public static final String START = "Start Date";
  /**
   * Type of a position or trade.
   */
  public static final String TYPE = "Type";
  /**
   * The named used for a value published as a merged output.
   */
  public static final String MERGED_OUTPUT = "MergedOutput";

  ///// Externally-sourced values
  // Existing value requirement names with a suffix
  // NOTE jonathan 2012-07-13 -- simply to allow clearer column headers. Should be removed once we have a better solution.
  private static final String EXTERNAL_SUFFIX = " (ext)";
  /**
   * External value of {@link #CONDITIONAL_HISTORICAL_VAR}
   */
  public static final String EXTERNAL_CONDITIONAL_HISTORICAL_VAR = CONDITIONAL_HISTORICAL_VAR + EXTERNAL_SUFFIX;
  /**
   * External value of {@link #DV01}
   */
  public static final String EXTERNAL_DV01 = DV01 + EXTERNAL_SUFFIX;
  /**
   * External value of {@link #HISTORICAL_VAR_STDDEV}
   */
  public static final String EXTERNAL_HISTORICAL_VAR_STDDEV = HISTORICAL_VAR_STDDEV + EXTERNAL_SUFFIX;
  /**
   * External value of {@link #PRESENT_VALUE}.
   */
  public static final String EXTERNAL_PRESENT_VALUE = PRESENT_VALUE + EXTERNAL_SUFFIX;
  /**
   * External position quantity.
   */
  public static final String EXTERNAL_QUANTITY = "Quantity" + EXTERNAL_SUFFIX;

  /**
   * Delta Equivalent value of a position. Essentially equivalent to {@link ValueRequirementNames#VALUE_DELTA}
   */
  public static final String NET_MARKET_VALUE = "Net Market Value";
  /**
   * {@link ValueRequirementNames#NET_MARKET_VALUE} scaled by some Capital amount
   */
  public static final String NET_CAPITAL = "Net Capital";
  /**
   * {@link ValueRequirementNames#NET_MARKET_VALUE} filtered for Equity Security Types
   */
  public static final String EQUITY_NET_MARKET_VALUE = "Equity Net Market Value";
  /**
   * {@link ValueRequirementNames#NET_CAPITAL} filtered for Equity Security Types
   */
  public static final String EQUITY_NET_CAPITAL = "Equity Net Capital";

}
