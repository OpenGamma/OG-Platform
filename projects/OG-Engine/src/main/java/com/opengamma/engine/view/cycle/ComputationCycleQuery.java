/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * Represents a query for outputs from a computation cycle.
 */
@PublicSPI
public class ComputationCycleQuery {

  /**
   * The calculation configuration name.
   */
  private String _calculationConfigurationName;

  /**
   * The value specifications defining the desired outputs.
   */
  private Collection<ValueSpecification> _valueSpecifications;

  //-----------------------------------------------------------------------
  /**
   * Gets the calculation configuration for which the computation caches will be queried.
   * 
   * @return the value of the property
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * Sets the calculation configuration for which the computation caches will be queried.
   * 
   * @param calculationConfigurationName the new value of the property
   */
  public void setCalculationConfigurationName(String calculationConfigurationName) {
    this._calculationConfigurationName = calculationConfigurationName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the value specifications defining the desired cache values.
   * 
   * @return the value of the property
   */
  public Collection<ValueSpecification> getValueSpecifications() {
    return _valueSpecifications;
  }

  /**
   * Sets the value specifications defining the desired cache values.
   * 
   * @param valueSpecifications the new value of the property
   */
  public void setValueSpecifications(Collection<ValueSpecification> valueSpecifications) {
    this._valueSpecifications = valueSpecifications;
  }

}
