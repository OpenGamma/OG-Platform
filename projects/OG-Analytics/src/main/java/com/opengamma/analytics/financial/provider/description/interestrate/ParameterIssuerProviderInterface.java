/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

/**
 * Interface for parameter provider with multi-curves provider.
 */
public interface ParameterIssuerProviderInterface extends ParameterProviderInterface {

  /**
   * Returns the issuer and multi-curves provider.
   * @return The provider
   */
  IssuerProviderInterface getIssuerProvider();

}
