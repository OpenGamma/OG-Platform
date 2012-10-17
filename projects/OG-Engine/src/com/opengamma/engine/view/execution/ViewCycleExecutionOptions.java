/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.Collections;
import java.util.List;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.springframework.util.ObjectUtils;

import com.google.common.collect.ImmutableList;
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

    private Instant _valuationTime;

    private List<MarketDataSpecification> _marketDataSpecifications;

    private VersionCorrection _resolverVersionCorrection;

    public Builder() {
      _marketDataSpecifications = Collections.<MarketDataSpecification>emptyList();
    }

    public Builder(final ViewCycleExecutionOptions copyFrom) {
      _valuationTime = copyFrom.getValuationTime();
      _marketDataSpecifications = copyFrom.getMarketDataSpecifications();
      _resolverVersionCorrection = copyFrom.getResolverVersionCorrection();
    }

    /**
     * Sets the valuation time for the view cycle. If set to null then a time implied by the data source will be used - if no time is implied the view process' clock will be used.
     * 
     * @param valuationTime the valuation time to set
     * @return this instance
     */
    public Builder setValuationTime(final InstantProvider valuationTime) {
      _valuationTime = (valuationTime != null) ? Instant.of(valuationTime) : null;
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
      _marketDataSpecifications = Collections.singletonList(marketDataSpecification);
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
     * Returns the market data specifications for the view cycle.
     * 
     * @return the market data specifications, not null
     */
    public List<MarketDataSpecification> getMarketDataSpecifications() {
      return _marketDataSpecifications;
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
     * Creates a {@link ViewCycleExecutionOptions} instance containing the values from this builder.
     * 
     * @return the new instance
     */
    public ViewCycleExecutionOptions create() {
      return new ViewCycleExecutionOptions(this);
    }

  }

  private final Instant _valuationTime;

  private final List<MarketDataSpecification> _marketDataSpecifications;
  
  private final VersionCorrection _resolverVersionCorrection;

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
    _valuationTime = builder.getValuationTime();
    _marketDataSpecifications = builder.getMarketDataSpecifications();
    _resolverVersionCorrection = builder.getResolverVersionCorrection();
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
   * Returns the version/correction to use when resolving references (for example the portfolio, positions, securities, time-series and so on). If not set the default cycle options will be used. If
   * these are the default cycle options then a value of null will imply {@link VersionCorrection#LATEST}.
   * 
   * @return the version/correction to use for reference resolution
   */
  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ViewCycleExecutionOptions[");
    if (getValuationTime() != null) {
      sb.append("valuationTime=").append(getValuationTime()).append(", ");
    }
    if (getResolverVersionCorrection() != null) {
      sb.append("portfolioVersionCorrection=").append(getResolverVersionCorrection()).append(", ");
    }
    sb.append("marketDataSpecifications=").append(getMarketDataSpecifications());
    return sb.append("]").toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result += (result << 4) + getMarketDataSpecifications().hashCode();
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
    return getMarketDataSpecifications().equals(other.getMarketDataSpecifications())
        && ObjectUtils.nullSafeEquals(getValuationTime(), other.getValuationTime())
        && ObjectUtils.nullSafeEquals(getResolverVersionCorrection(), other.getResolverVersionCorrection());
  }
  
}
