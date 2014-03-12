/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

/**
 * Class containing string constants used to define volatility surfaces
 * and cubes.
 */
public class SurfaceAndCubePropertyNames {
  /** The name of the surface quote type property */
  public static final String PROPERTY_SURFACE_QUOTE_TYPE = "SurfaceQuoteType";
  /** The name of the surface units property */
  public static final String PROPERTY_SURFACE_UNITS = "SurfaceUnits";
  /** The name of the surface definition property */
  public static final String PROPERTY_SURFACE_DEFINITION = "SurfaceDefinitionName";
  /** The name of the surface specification property */
  public static final String PROPERTY_SURFACE_SPECIFICATION = "SurfaceSpecificationName";
  /** The name of the underlying futures definition property */
  public static final String PROPERTY_FUTURE_DEFINITION = "FutureDefinitionName";
  /** The name of the underlying futures specification property */
  public static final String PROPERTY_FUTURE_SPECIFICATION = "FutureSpecificationName";
  /** Represents volatility surface quotes in units of volatility */
  public static final String VOLATILITY_QUOTE = "VolatilityQuote";
  /** Represents volatility surface quotes in units of price */
  public static final String PRICE_QUOTE = "PriceQuote";
  /** The name of the cube definition property */
  public static final String PROPERTY_CUBE_DEFINITION = "CubeDefinitionName";
  /** The name of the cube specification property */
  public static final String PROPERTY_CUBE_SPECIFICATION = "CubeSpecificationName";
  /** The name of the cube quote type property */
  public static final String PROPERTY_CUBE_QUOTE_TYPE = "CubeQuoteType";
  /** The name of the cube quote units property */
  public static final String PROPERTY_CUBE_UNITS = "CubeVolatilityUnits";
  /** The name of the cube quote units property */
  public static final String PROPERTY_EXERCISE_TYPE = "ExerciseType";
  /** The type of the instrument */
  public static final String INSTRUMENT_TYPE = "InstrumentType";
}
