/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.VersionCorrection;

/**
 * A set of properties to use to build a dependency graph trace.
 */
public class DependencyGraphTraceBuilderProperties {
  private final String _calculationConfigurationName;
  private final Instant _valuationTime;
  private final VersionCorrection _resolutionTime;
  private final ValueProperties _defaultProperties;
  private final Collection<ValueRequirement> _requirements;
  private final List<MarketDataSpecification> _marketData;

  /**
   * Constructs a builder instance with given context and default values.
   */
  public DependencyGraphTraceBuilderProperties() {
    _calculationConfigurationName = "Default";
    _valuationTime = null;
    _resolutionTime = VersionCorrection.LATEST;
    _defaultProperties = ValueProperties.none();
    _requirements = Collections.emptyList();
    _marketData = Collections.emptyList();
  }

  /**
   * Copy constructor
   * @param other instance to copy
   */
  private DependencyGraphTraceBuilderProperties(DependencyGraphTraceBuilderProperties other) {
    _calculationConfigurationName = other.getCalculationConfigurationName();
    _valuationTime = other.getValuationTime();
    _resolutionTime = other.getResolutionTime();
    _defaultProperties = other.getDefaultProperties();
    _requirements = other.getRequirements();
    _marketData = other.getMarketData();
  }

  /**
   * @return configured calculation configuration name
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * @return configured valuation time
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * @return configured resolution time
   */
  public VersionCorrection getResolutionTime() {
    return _resolutionTime;
  }

  /**
   * @return configured default properties
   */
  public ValueProperties getDefaultProperties() {
    return _defaultProperties;
  }

  /**
   * @return configured requirements
   */
  public Collection<ValueRequirement> getRequirements() {
    return _requirements;
  }

  /**
   * @return configured market data
   */
  public List<MarketDataSpecification> getMarketData() {
    return _marketData;
  }

  /**
   * @param calculationConfigurationName calculation configuration name to set
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties calculationConfigurationName(final String calculationConfigurationName) {
    return new DependencyGraphTraceBuilderProperties(this) {
      public String getCalculationConfigurationName() {
        return calculationConfigurationName;
      }
    };
  }

  /**
   * @param resolutionTime resolution time to set
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties resolutionTime(final VersionCorrection resolutionTime) {
    return new DependencyGraphTraceBuilderProperties(this) {
      @Override
      public VersionCorrection getResolutionTime() {
        return resolutionTime;
      }
    };
  }

  /**
   * @param valuationTime valuation time to set
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties valuationTime(final Instant valuationTime) {
    return new DependencyGraphTraceBuilderProperties(this) {
      @Override
      public Instant getValuationTime() {
        return valuationTime;
      }
    };
  }

  /**
   * @param defaultProperties default properties to set
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties defaultProperties(final ValueProperties defaultProperties) {
    return new DependencyGraphTraceBuilderProperties(this) {
      public ValueProperties getDefaultProperties() {
        return defaultProperties;
      };
    };
  }

  /**
   * @param requirement requirement to add
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties addRequirement(ValueRequirement requirement) {
    final Collection<ValueRequirement> currentRequirements = new ArrayList<>(getRequirements());
    currentRequirements.add(requirement);
    return requirements(currentRequirements);
  }

  /**
   * @param requirements requirements to set
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties requirements(final Collection<ValueRequirement> requirements) {
    return new DependencyGraphTraceBuilderProperties(this) {
      @Override
      public Collection<ValueRequirement> getRequirements() {
        return requirements;
      }
    };
  }

  /**
   * @param marketData market data to set
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties marketData(final List<MarketDataSpecification> marketData) {
    return new DependencyGraphTraceBuilderProperties(this) {
      @Override
      public List<MarketDataSpecification> getMarketData() {
        return marketData;
      }
    };
  }
  
  /**
   * Add a market data spec
   * @param marketData a market data spec
   * @return a newly configured instance
   */
  public DependencyGraphTraceBuilderProperties addMarketData(MarketDataSpecification marketData) {
    List<MarketDataSpecification> newMarketData = new ArrayList<>(getMarketData());
    newMarketData.add(marketData);
    return marketData(newMarketData);
  }

  @Override
  public String toString() {
    return "DependencyGraphTraceBuilderProperties [getCalculationConfigurationName()=" + getCalculationConfigurationName() + ", getValuationTime()=" + getValuationTime() + ", getResolutionTime()=" +
        getResolutionTime() + ", getDefaultProperties()=" + getDefaultProperties() + ", getRequirements()=" + getRequirements() + ", getMarketData()=" + getMarketData() + "]";
  }

}
