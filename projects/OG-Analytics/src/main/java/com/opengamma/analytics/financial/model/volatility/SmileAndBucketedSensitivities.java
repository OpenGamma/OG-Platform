/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing a volatility smile expressed in delta form and the bucketed sensitivities of this smile to the data points used to construct it.
 */
public class SmileAndBucketedSensitivities {
  private final SmileDeltaParameters _smile;
  private final double[][] _sensitivities;

  /**
   * @param smile The volatility smile, not null
   * @param sensitivities The bucketed sensitivities, not null
   */
  public SmileAndBucketedSensitivities(final SmileDeltaParameters smile, final double[][] sensitivities) {
    ArgumentChecker.notNull(smile, "smile");
    ArgumentChecker.notNull(sensitivities, "sensitivities");
    _smile = smile;
    _sensitivities = sensitivities;
  }

  /**
   * Gets the smile
   * @return The smile
   */
  public SmileDeltaParameters getSmile() {
    return _smile;
  }

  /**
   * Gets the bucketed sensitivities
   * @return The bucketed sensitivities
   */
  public double[][] getBucketedSensitivities() {
    return _sensitivities;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_sensitivities);
    result = prime * result + _smile.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SmileAndBucketedSensitivities other = (SmileAndBucketedSensitivities) obj;
    if (!ObjectUtils.equals(_smile, other._smile)) {
      return false;
    }
    if (!Arrays.deepEquals(_sensitivities, other._sensitivities)) {
      return false;
    }
    return true;
  }

}
