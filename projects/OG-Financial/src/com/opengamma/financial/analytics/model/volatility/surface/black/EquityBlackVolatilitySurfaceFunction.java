/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleLinkedOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.SurfacePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceQuoteType;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;

/**
 * New BlackVolSurfaceFunction BETA - Assumes SABR as interpolation scheme. 
 * Note: This is a different route of Vol Interpolation than: Raw > Standard > Interpolated as used in IRFutureOptionBlackFunction
 */
public class EquityBlackVolatilitySurfaceFunction extends BlackVolatilitySurfaceFunction {

  @Override
  protected boolean isCorrectIdType(ComputationTarget target) {
    String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
            targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()));
  }

  @Override
  /** 
   * Builds a StandardSmileSurfaceDataBundle 
   * <p>
   * Note that the Volatility requirement is of type STANDARD_VOLATILITY_SURFACE_DATA. This means that it is tiled, and must be brought down to its unique expiry/strike values
   * This is done because the existing of existing Function chain. 
   * RawEquityOptionVolatilitySurfaceFunction produces a VOLATILITY_SURFACE_DATA, but the expiries are simply ordinals to n'th expiry, not times, and there are empty columns..
   * The next function in the chain, EquityFutureOptionVolatilitySurfaceDataFunction, sorts this out, but produces the tiled STANDARD type, hence we use that. Easy to refactor if desired.    
   */
  protected SmileSurfaceDataBundle getData(FunctionInputs inputs, ValueRequirement volatilityDataRequirement, ValueRequirement forwardCurveRequirement) {
    final Object volatilitySurfaceObject = inputs.getValue(volatilityDataRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + volatilityDataRequirement);
    }
    
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    
    
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Double, Double> rawVolatilitySurface = (VolatilitySurfaceData<Double, Double>) volatilitySurfaceObject;
      
    // Get Unique Expiries
    double[] expiries = ArrayUtils.toPrimitive(rawVolatilitySurface.getXs());
    DoubleLinkedOpenHashSet expirySet = new DoubleLinkedOpenHashSet(expiries); 
    double[] uniqueExpiries = expirySet.toDoubleArray();
    Arrays.sort(uniqueExpiries);
    final int nExpiries = uniqueExpiries.length;
    // Get Unique Strikes
    final double[] strikes = ArrayUtils.toPrimitive(rawVolatilitySurface.getYs());
    DoubleLinkedOpenHashSet strikeSet = new DoubleLinkedOpenHashSet(strikes);
    double[] uniqueStrikes = strikeSet.toDoubleArray();
    Arrays.sort(uniqueStrikes);
    final int nStrikes = uniqueStrikes.length;
    
    // Convert vols and strikes to double[][], 
    // noting that different expiries may have different populated strikes
    double[][] fullStrikes = new double[nExpiries][];
    double[][] fullVols = new double[nExpiries][];
    for (int i = 0; i < nExpiries; i++) {
      DoubleList availableStrikes = new DoubleArrayList();
      DoubleList availableVols = new DoubleArrayList();
      for (int j = 0; j < nStrikes; j++) {
        Double vol = rawVolatilitySurface.getVolatility(uniqueExpiries[i], uniqueStrikes[j]);
        if (vol != null) {
          availableStrikes.add(uniqueStrikes[j]);
          availableVols.add(vol);
        }
      }
      if (availableVols.size() == 0) {
        throw new OpenGammaRuntimeException("Unexpected error. No Vols found for an expiry."); // Use ArrayLists for fullStrikes (and Vols). But first, check input surface data
      }
      fullStrikes[i] = availableStrikes.toDoubleArray();
      fullVols[i] = availableVols.toDoubleArray(); 
    }
   
    return new StandardSmileSurfaceDataBundle(forwardCurve, uniqueExpiries, fullStrikes, fullVols, false); // TODO Where does it matter whether calls are used? No prices are given, just vols..
  }
  
  @Override
  protected ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveCalculationMethodName = desiredValue.getConstraint(CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    final String forwardCurveCcyName = desiredValue.getConstraint(CURVE_CURRENCY);
    final String fundingCurveName = desiredValue.getConstraint(YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE_CALCULATION_METHOD, curveCalculationMethodName)
        .with(CURVE, forwardCurveName)
        .with(CURVE_CURRENCY, forwardCurveCcyName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }
  
  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.EQUITY_OPTION;
  }

  @Override
  protected String getSurfaceQuoteUnits() {
    return SurfacePropertyNames.VOLATILITY_QUOTE;
  }

  @Override
  /** TODO Consider whether we might make this variable by reading the volatility specification. */
  protected String getSurfaceQuoteType() {
    return SurfaceQuoteType.CALL_STRIKE;
  }

  @Override
  protected ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName, final String instrumentType,
      final String surfaceQuoteType, final String surfaceQuoteUnits) {
    final ValueRequirement volDataRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
        .get());
    return volDataRequirement;
  }
  
  @Override
  /** TODO THIS IS SPLINE. MAKE OTHER METHODS AVAILABLE */
  protected ValueProperties getResultProperties() {
    ValueProperties properties = createValueProperties().get();
    properties = BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(properties, getInstrumentType()).get();  
    properties = BlackVolatilitySurfaceUtils.addSplineVolatilityInterpolatorProperties(properties).get();
    properties = properties.copy()
      .withAny(ValuePropertyNames.CURVE_CURRENCY)
      .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
      .get();
    return properties;
  }
  
  @Override
  protected ValueProperties getResultProperties(ValueRequirement desiredValue) {
    ValueProperties properties = createValueProperties().get();
    properties = BlackVolatilitySurfaceUtils.addSplineVolatilityInterpolatorProperties(properties, desiredValue).get();
    properties = BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(properties, getInstrumentType(), desiredValue).get();
    return properties; 
  }
  
  // TODO THIS IS SABR. HAD ISSUES WITH FITTING..
  protected ValueProperties getResultPropertiesSABR() {
    ValueProperties properties = createValueProperties().get();
    properties = BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(properties, getInstrumentType()).get();  
    properties = BlackVolatilitySurfaceUtils.addSABRVolatilityInterpolatorProperties(properties).get();
    properties = properties.copy()
      .withAny(ValuePropertyNames.CURVE_CURRENCY)
      .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
      .get();
    return properties;
  }

  protected ValueProperties getResultPropertiesSABR(ValueRequirement desiredValue) {
    ValueProperties properties = createValueProperties().get();
    properties = BlackVolatilitySurfaceUtils.addSABRVolatilityInterpolatorProperties(properties, desiredValue).get();
    properties = BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(properties, getInstrumentType(), desiredValue).get();
    return properties; 
  }

  /** Code to look up the specification, if desired */
  @SuppressWarnings("unused")
  private VolatilitySurfaceSpecification getVolSpecification(String fullSurfaceName, FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceSpecificationSource volSpecSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final VolatilitySurfaceSpecification specification = volSpecSource.getSpecification(fullSurfaceName, getInstrumentType());
    return specification;
  }
}
