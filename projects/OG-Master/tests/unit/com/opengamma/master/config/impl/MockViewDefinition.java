/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Mock class.
 */
public class MockViewDefinition {
  private String _name;
  private UniqueIdentifier _portfolioId;
  private Long _minDeltaCalculationPeriod;
  private Long _maxDeltaCalculationPeriod;

  public MockViewDefinition() {
  }

  public MockViewDefinition(String name, UniqueIdentifier portfolioId) {
    ArgumentChecker.notNull(name, "View name");

    _name = name;
    _portfolioId = portfolioId;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public UniqueIdentifier getPortfolioId() {
    return _portfolioId;
  }

  public void setPortfolioId(UniqueIdentifier portfolioId) {
    _portfolioId = portfolioId;
  }

  public Long getMinDeltaCalculationPeriod() {
    return _minDeltaCalculationPeriod;
  }

  public void setMinDeltaCalculationPeriod(Long minDeltaCalculationPeriod) {
    _minDeltaCalculationPeriod = minDeltaCalculationPeriod;
  }

  public Long getMaxDeltaCalculationPeriod() {
    return _maxDeltaCalculationPeriod;
  }

  public void setMaxDeltaCalculationPeriod(Long maxDeltaCalculationPeriod) {
    _maxDeltaCalculationPeriod = maxDeltaCalculationPeriod;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getName());
    result = prime * result + ObjectUtils.hashCode(getPortfolioId());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof MockViewDefinition)) {
      return false;
    }

    MockViewDefinition other = (MockViewDefinition) obj;
    boolean basicPropertiesEqual = ObjectUtils.equals(getName(), other.getName()) && ObjectUtils.equals(getPortfolioId(), other.getPortfolioId());
    if (!basicPropertiesEqual) {
      return false;
    }
    return true;
  }

}
