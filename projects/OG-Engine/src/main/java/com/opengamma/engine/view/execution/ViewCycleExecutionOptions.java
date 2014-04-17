/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.NoOpMarketDataSelector;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Encapsulates specific settings affecting the execution of an individual view cycle.
 */
@PublicAPI
public class ViewCycleExecutionOptions {

  /**
   * Helper for constructing {@link ViewCycleExecutionOptions} instances.
   */
  public static class Builder {
    
    private String _name;
    
    private Instant _valuationTime;

    private List<MarketDataSpecification> _marketDataSpecifications;

    private MarketDataSelector _marketDataSelector;

    private VersionCorrection _resolverVersionCorrection;

    private Map<DistinctMarketDataSelector, FunctionParameters> _functionParameters;

    public Builder() {
      _marketDataSpecifications = ImmutableList.of();
      _marketDataSelector = NoOpMarketDataSelector.getInstance();
      _functionParameters = ImmutableMap.of();
    }

    public Builder(final ViewCycleExecutionOptions copyFrom) {
      _name = copyFrom.getName();
      _valuationTime = copyFrom.getValuationTime();
      _marketDataSpecifications = copyFrom.getMarketDataSpecifications();
      _marketDataSelector = copyFrom.getMarketDataSelector();
      _resolverVersionCorrection = copyFrom.getResolverVersionCorrection();
      _functionParameters = copyFrom.getFunctionParameters();
    }

    /**
     * Sets the name of the view cycle
     * 
     * @param name name of the view cycle
     * @return this instance
     */
    public Builder setName(final String name) {
      _name = name;
      return this;
    }
    
    /**
     * Sets the valuation time for the view cycle. If set to null then a time implied by the data source will be used - if no time is implied the view process' clock will be used.
     * 
     * @param valuationTime the valuation time to set
     * @return this instance
     */
    public Builder setValuationTime(final Instant valuationTime) {
      _valuationTime = valuationTime;
      return this;
    }

    /**
     * Returns the valuation time for the view cycle. If set to null then a time implied by the data source will be used - if no time is implied the view process' clock will be used.
     * 
     * @return the valuation time
     */
    public Instant getValuationTime() {
      return _valuationTime;
    }

    /**
     * Sets the market data specification for the view cycle. This is equivalent to calling {@link #setMarketDataSpecitications} with a list containing a single element.
     * 
     * @param marketDataSpecification the market data specification, not null
     * @return this instance
     */
    public Builder setMarketDataSpecification(final MarketDataSpecification marketDataSpecification) {
      ArgumentChecker.notNull(marketDataSpecification, "marketDataSpecification");
      _marketDataSpecifications = ImmutableList.of(marketDataSpecification);
      return this;
    }

    /**
     * Sets the market data specifications for the view cycle.
     * 
     * @param marketDataSpecifications the market data specifications, not null and not containing null
     * @return this instance
     */
    public Builder setMarketDataSpecifications(List<MarketDataSpecification> marketDataSpecifications) {
      ArgumentChecker.notNull(marketDataSpecifications, "marketDataSpecifications");
      marketDataSpecifications = ImmutableList.copyOf(marketDataSpecifications);
      ArgumentChecker.isFalse(marketDataSpecifications.contains(null), "marketDataSpecifications");
      _marketDataSpecifications = marketDataSpecifications;
      return this;
    }

    /**
     * Sets the market data selectors for the view cycle.
     * These can be used to flag which market data is to be manipulated as it is passed into functions.
     *
     * @param marketDataSelector the market data selector, not null
     * @return this instance
     */
    public Builder setMarketDataSelector(MarketDataSelector marketDataSelector) {
      ArgumentChecker.notNull(marketDataSelector, "marketDataSelector");
      _marketDataSelector = marketDataSelector;
      return this;
    }

    /**
     * Returns the name of the view cycle
     * @return name of the view cycle
     */
    public String getName() {
      return _name;
    }
    
    /**
     * Returns the market data specifications for the view cycle.
     * 
     * @return the market data specifications, not null
     */
    public List<MarketDataSpecification> getMarketDataSpecifications() {
      return _marketDataSpecifications;
    }

    /**
     * Returns the market data shift specification for the view cycle.
     *
     * @return the market data shift specification, not null
     */
    public MarketDataSelector getMarketDataSelector() {
      return _marketDataSelector;
    }

    /**
     * Sets the version/correction to use when resolving references (for example the portfolio, positions, securities, time-series and so on). If set to null, the version correction from the default
     * cycle options will be used. If these are the default cycle options then a value of null will imply {@link VersionCorrection#LATEST}.
     * 
     * @param versionCorrection the version
     * @return this instance
     */
    public Builder setResolverVersionCorrection(final VersionCorrection versionCorrection) {
      _resolverVersionCorrection = versionCorrection;
      return this;
    }

    /**
     * Returns the version/correction to use when resolving references (for example the portfolio, positions, securities, time-series and so on). If not set the default cycle options will be used. If
     * these are the default cycle options then a value of null will imply {@link VersionCorrection#LATEST}.
     * 
     * @return the version/correction to use for reference resolution
     */
    public VersionCorrection getResolverVersionCorrection() {
      return _resolverVersionCorrection;
    }

    /**
     * Sets the function parameters to be used for the current view cycle.
     *
     * @param functionParameters the function parameters, not null
     * @return this instance
     */
    public Builder setFunctionParameters(final Map<DistinctMarketDataSelector, FunctionParameters> functionParameters) {
      _functionParameters = functionParameters;
      return this;
    }

    /**
     * Returns the function parameters to be used for the current view cycle. If not set the default cycle options will be used.
     *
     * @return the function parameters to be used in the current cycle, not null
     */
    public Map<DistinctMarketDataSelector, FunctionParameters> getFunctionParameters() {
      return _functionParameters;
    }

    /**
     * Creates a {@link ViewCycleExecutionOptions} instance containing the values from this builder.
     *
     * @return the new instance
     */
    public ViewCycleExecutionOptions create() {
      return new ViewCycleExecutionOptions(this);
    }
  }

  private final String _name;
  
  private final Instant _valuationTime;

  private final List<MarketDataSpecification> _marketDataSpecifications;

  private final MarketDataSelector _marketDataSelector;
  
  private final VersionCorrection _resolverVersionCorrection;

  private final Map<DistinctMarketDataSelector, FunctionParameters> _functionParameters;

  // TODO [PLAT-1153] view correction time - probably want either valuation time or some fixed correction time

  /**
   * Creates a default instance.
   */
  public ViewCycleExecutionOptions() {
    this(new Builder());
  }

  /**
   * Creates an instance with the values from the supplied builder.
   *
   * @param builder the values to populate the instance from
   */
  protected ViewCycleExecutionOptions(final Builder builder) {
    _name = builder.getName();
    _valuationTime = builder.getValuationTime();
    _marketDataSpecifications = builder.getMarketDataSpecifications();
    _marketDataSelector = builder.getMarketDataSelector();
    _resolverVersionCorrection = builder.getResolverVersionCorrection();
    _functionParameters = builder.getFunctionParameters();
  }

  /**
   * Creates a builder initialized with the values from this instance. The builder can be modified before {@link Builder#create} called to create a new {@link ViewCycleExecutionOptions} instance.
   * 
   * @return a builder instance
   */
  public Builder copy() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the name of the view cycle
   * 
   * @return the name of the view cycle, or null if not specified
   */
  public String getName() {
    return _name;
  }
  
  /**
   * Returns the valuation time for the view cycle. If set to null then a time implied by the data source will be used - if no time is implied the view process' clock will be used.
   * 
   * @return the valuation time, or null if not specified
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Returns the market data specifications.
   * 
   * @return the market data specifications, not null and not containing null but possibly empty
   */
  public List<MarketDataSpecification> getMarketDataSpecifications() {
    return _marketDataSpecifications;
  }

  /**
   * Returns the market data shift specifications.
   *
   * @return the market data shift specifications, not null and not containing null but possibly empty
   */
  public MarketDataSelector getMarketDataSelector() {
    return _marketDataSelector;
  }

  /**
   * Returns the version/correction to use when resolving references (for example the portfolio, positions, securities, time-series and so on). If not set the default cycle options will be used. If
   * these are the default cycle options then a value of null will imply {@link VersionCorrection#LATEST}.
   * 
   * @return the version/correction to use for reference resolution
   */
  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  /**
   * Returns the function parameters to be used for the current view cycle.
   *
   * @return the function parameters to be used in the current cycle
   */
  public Map<DistinctMarketDataSelector, FunctionParameters> getFunctionParameters() {
    return _functionParameters;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ViewCycleExecutionOptions[");
    
    sb.append("name=").append(getName()).append(", ");
    if (getValuationTime() != null) {
      sb.append("valuationTime=").append(getValuationTime()).append(", ");
    }
    if (getResolverVersionCorrection() != null) {
      sb.append("portfolioVersionCorrection=").append(getResolverVersionCorrection()).append(", ");
    }
    
    sb.append("marketDataSpecifications=")
        .append(getMarketDataSpecifications())
        .append(", marketDataShiftSpecification=")
        .append(getMarketDataSelector())
        .append(", functionParameters=")
        .append(getFunctionParameters())
        .append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result += (result << 4) + ObjectUtils.nullSafeHashCode(getName());
    result += (result << 4) + getMarketDataSpecifications().hashCode();
    result += (result << 4) + getMarketDataSelector().hashCode();
    result += (result << 4) + getFunctionParameters().hashCode();
    result += (result << 4) + ObjectUtils.nullSafeHashCode(getValuationTime());
    result += (result << 4) + ObjectUtils.nullSafeHashCode(getResolverVersionCorrection());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ViewCycleExecutionOptions)) {
      return false;
    }
    final ViewCycleExecutionOptions other = (ViewCycleExecutionOptions) obj;
    return ObjectUtils.nullSafeEquals(getName(), other.getName())
        && getMarketDataSpecifications().equals(other.getMarketDataSpecifications())
        && getMarketDataSelector().equals(other.getMarketDataSelector())
        && getFunctionParameters().equals(other.getFunctionParameters())
        && ObjectUtils.nullSafeEquals(getValuationTime(), other.getValuationTime())
        && ObjectUtils.nullSafeEquals(getResolverVersionCorrection(), other.getResolverVersionCorrection());
  }
}
