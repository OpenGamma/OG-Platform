/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.util.PublicAPI;

/**
 * Standard names used to refer to particular value properties.
 * <p>
 * These name are used as keys to define specific value properties for the engine.
 * They should be used by a {@link FunctionDefinition} to annotate their requirements
 * or provide additional context about their outputs.
 */
@PublicAPI
public final class ValuePropertyNames {

  // Please see http://jira.opengamma.com/browse/PLAT-2330 for information about this constant.
  /** Property name of the contribution to the P&L (e.g. yield curve, FX rate) */
  public static final String PROPERTY_PNL_CONTRIBUTIONS = "PnLContribution";

  /**
   * Restricted constructor.
   */
  private ValuePropertyNames() {
  }

  /**
   * The aggregation approach used to produce the output value from the inputs.
   * <p>
   * This should be used where there are multiple possible behaviors from functions to produce an aggregate
   * value. For example, input values that cannot be combined directly might either be discarded or converted;
   * this property will indicate which. Note that in such circumstances the priority of each function in the
   * repository is important to force "default" behaviors if no explicit aggregation constraint is made
   * when requesting the produced value
   */
  public static final String AGGREGATION = "Aggregation";

  /**
   * The country of a value, specified as an ISO 2 digit code.
   * <p>
   * This should only be used where it is meaningful to describe a value with a single country
   * instead of a currency (e.g. a bond issued by a euro-zone country).
   */
  public static final String COUNTRY = "Country";

  /**
   * The symbolic name of a volatility cube used to produce a value, valid within the naming context of
   * the function repository containing the function definition used.
   * <p>
   * This should only be used where it is meaningful to describe a value based on a single
   * cube. If produced by functions requiring multiple cube inputs, a suitable prefix should
   * be used to differentiate the input cubes.
   */
  public static final String CUBE = "Cube";

  /**
   * The currency of a value, specified as an ISO code.
   * <p>
   * This should only be used where it is meaningful to describe a value with a single currency.
   * For example, an exchange rate should not make use of this property.
   */
  public static final String CURRENCY = "Currency";

  /**
   * The symbolic name of a curve used to produce a value, valid within the naming context of
   * the function repository containing the function definition used.
   * <p>
   * This should only be used where it is meaningful to describe a value based on a single
   * curve. If produced by functions requiring multiple curve inputs, a suitable prefix should
   * be used to differentiate the input curves.
   */
  public static final String CURVE = "Curve";

  /**
   * The currency of the curve used to produce a value. This does not necessarily imply anything about the currency
   * of the output value.
   */
  public static final String CURVE_CURRENCY = "CurveCurrency";

  /**
   * The data provider used to source external values into the view cycle. This may be used by {@link MarketDataAvailabilityProvider} instances that work with multiple underlying providers to indicate
   * which data provider should be queried for the value. In the case of a "route" of nested data providers existing, the {@code /} character can be used as a separator between elements.
   */
  public static final String DATA_PROVIDER = "DataProvider";

  /**
   * The function identifier that produced a value.
   * <p>
   * If there are multiple functions in a repository that can compute a given value, this can
   * be used as a constraint to force a particular one to be used.
   * <p>
   * The result {@link ValueSpecification} objects created by functions must always include an
   * appropriate function identifier.
   */
  public static final String FUNCTION = "Function";

  /**
   * The symbolic name of a curve used to produce an analytic value from an instrument that requires
   * more than one curve (e.g. FX forwards, where the pay curve is the curve used to discount
   * the pay amount, or FX options, where the pay curve is the curve used to discount the
   * put amount).
   */
  public static final String PAY_CURVE = "PayCurve";

  /**
   * The symbolic name of a curve used to produce an analytic value from an instrument that requires
   * more than one curve (e.g. FX forwards, where the receive curve is the curve used to discount
   * the receive amount, or FX options, where the receive curve is the curve used to discount the
   * call amount).
   */
  public static final String RECEIVE_CURVE = "ReceiveCurve";

  /**
   * The name of the curve calculation configuration used for the pay leg of an instrument (e.g.
   * an FX forward or a cross-currency swap).
   */
  public static final String PAY_CURVE_CALCULATION_CONFIG = "PayCurveCalculationConfig";

  /**
   * The name of the curve calculation configuration used for the receive leg of an instrument
   * (e.g. an FX forward or a cross-currency swap).
   */
  public static final String RECEIVE_CURVE_CALCULATION_CONFIG = "ReceiveCurveCalculationConfig";

  /**
   * The symbolic name of a surface used to produce a value, valid within the naming context of
   * the function repository containing the function definition used.
   * <p>
   * This should only be used where it is meaningful to describe a value based on a single
   * surface. If produced by functions requiring multiple surface inputs, a suitable prefix should
   * be used to differentiate the input surfaces.
   */
  public static final String SURFACE = "Surface";

  /**
   * The symbolic name of the calculation method used to produce a curve
   */
  public static final String CURVE_CALCULATION_METHOD = "CurveCalculationMethod";

  /**
   * A general name for a property describing how a value was calculated
   */
  public static final String CALCULATION_METHOD = "CalculationMethod";

  /**
   * A general name for a property describing how a surface was calculated
   */
  public static final String SURFACE_CALCULATION_METHOD = "SurfaceCalculationMethod";

  /**
   * A general name for a property describing how a surface was calculated
   */
  public static final String CUBE_CALCULATION_METHOD = "CubeCalculationMethod";

  /**
   * A general name for a property describing how volatility smiles were modelled (e.g. Heston, SABR)
   */
  public static final String SMILE_FITTING_METHOD = "SmileFittingMethod";

  /**
   * A reserved output prefix. No function should ever produce output values with properties that start
   * with this. It is therefore safe for functions to use it to prefix an optional constraint on inputs
   * that can never influence resolution, but serve to distinguish one from the other.
   */
  public static final String OUTPUT_RESERVED_PREFIX = ".";

  /**
   * A general name for a property describing the period of time over which a time-series applies (e.g. P2Y).
   */
  // NOTE jonathan 2013-03-12 -- PERIOD would be a better name
  public static final String SAMPLING_PERIOD = "SamplingPeriod";

  /**
   * A general name for a property describing how to calculate the returns of a given timeseries
   */
  public static final String RETURN_CALCULATOR = "ReturnMethod";

  /**
   * A general name for a property describing how to generate a schedule (e.g. daily, weekly)
   */
  public static final String SCHEDULE_CALCULATOR = "ScheduleMethod";

  /**
   * A general name for a property describing how to sample a series (e.g. pad missing data with that of the previous entry in the series)
   */
  public static final String SAMPLING_FUNCTION = "SamplingMethod";

  /**
   * A general name for a property describing how to calculate the mean of a series
   */
  public static final String MEAN_CALCULATOR = "MeanMethod";

  /**
   * A general name for a property describing how to calculate the standard deviation of a series
   */
  public static final String STD_DEV_CALCULATOR = "StandardDeviationMethod";

  /**
   * A general name for a property describing the confidence level to use for VaR calculations
   */
  public static final String CONFIDENCE_LEVEL = "Percentile";

  /**
   * A general name for a property describing the horizon in sampling periods to use for VaR calculations
   */
  public static final String HORIZON = "Horizon";

  /**
   * A general name for a property describing how to calculate the covariance of two time series
   */
  public static final String COVARIANCE_CALCULATOR = "CovarianceMethod";

  /**
   * A general name for a property describing how to calculate the variance of a series
   */
  public static final String VARIANCE_CALCULATOR = "VarianceMethod";

  /**
   * A general name for a property describing the maximum order of sensitivities to use when calculating VaR
   */
  public static final String ORDER = "Order";

  /**
   * A general name for a property describing how the calculate the excess return of a series
   */
  public static final String EXCESS_RETURN_CALCULATOR = "ExcessReturnMethod";

  /**
   * The name for a property describing the curve calculation configuration
   */
  public static final String CURVE_CALCULATION_CONFIG = "CurveCalculationConfig";

  /**
   * The name for a property describing the curve calculation configuration name.
   */
  public static final String CURVE_CONSTRUCTION_CONFIG = "CurveConstructionConfiguration";

  /**
   * The name for a property describing the shift of the strike
   * when approximating a binary option as a call or put spread
   */
  public static final String BINARY_OVERHEDGE = "BinaryOverhedge";

  /**
   * The name for a property describing the full width of the spread between the two calls or puts
   * used when approximating a binary option as a call or put spread
   */
  public static final String BINARY_SMOOTHING_FULLWIDTH = "BinarySmoothingFullWidth";

  /**
   * The amount to shift DV01 by in basis points
   */
  public static final String SHIFT = "Shift";

  // REVIEW 2012-08-29 andrew -- The Javadoc for SHIFT above is bad; it's a common name that will be used for things other than DV01.
  // REVIEW 2012-10-13 casey -- One doesn't even shift DV01. The shift in DV01 is in its name - Delta Value of One Basis Point..
  // REVIEW 2013-01-02 emcleod -- In the case of DV01, "Shift" refers to the scale factor applied (1 or -1). It was added to allow comparisons of results
  // from other libraries. The general point about the javadoc is valid.
  /**
   *
   */
  public static final String VALUE_AGGREGATION = "ValueAggregation";

  /**
   * The underlying ticker is used in Equity Options to tie results to the vol surface used,
   * such that each column may represent, for example, the vega of all positions sensitive to changes in that specific surface
   */
  public static final String UNDERLYING_TICKER = "UnderlyingTicker";

  /**
   * Controls the number formatting of the barrier distance - e.g. abs, percentage
   */
  public static final String BARRIER_DISTANCE_OUTPUT_FORMAT = "BarrierDistanceOutputFormat";
  
  /**
   * The name of a property that provides the name of a forward curve.
   */
  public static final String FORWARD_CURVE_NAME = "ForwardCurveName";
  
  /**
   * A general name for a property describing a transformation method to use.
   */
  public static final String TRANSFORMATION_METHOD = "TransformationMethod";
  
  /** 
   * The name of a property that provides the type of dividends, be they Continuous, Discrete or None 
   */
  public static final String DIVIDEND_TYPE = "DividendType";
  
  /** 
   * Instance of DIVIDEND_TYPE_PROPERTY representing a continuous dividend yield 
   */
  public static final String DIVIDEND_TYPE_CONTINUOUS = "Continuous";
  
  /** 
   * Instance of DIVIDEND_TYPE_PROPERTY representing discrete dividend payments. See { @link DiscreteDividendFunction} 
   */
  public static final String DIVIDEND_TYPE_DISCRETE = "Discrete";
  
  /** 
   * Instance of DIVIDEND_TYPE_PROPERTY representing no dividends 
  */
  public static final String DIVIDEND_TYPE_NONE = "None";
  
  /** 
   * The name of a general property that scales results. 
   */
  public static final String SCALE = "Scale";
  
  /** The name of a property representing the exposure configuration used for pricing */
  public static final String CURVE_EXPOSURES = "CurveExposures";
  
  /**
   * The name of the conversion method.
   */
  public static final String CONVERSION_METHOD = "ConversionMethod";
  
}
