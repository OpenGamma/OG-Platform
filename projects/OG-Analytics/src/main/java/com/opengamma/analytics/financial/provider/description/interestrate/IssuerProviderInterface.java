/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for issuer-specific curves.
 */
public interface IssuerProviderInterface extends ParameterIssuerProviderInterface {
  // TODO: Can probably be merged with MulticurveProvider if the Currency is replaced by a UniqueIdentifiable.

  @Override
  IssuerProviderInterface copy();

  /**
   * Gets the discount factor for one issuer in one currency.
   * @param issuer The issuer.
   * @param time The time.
   * @return The discount factor.
   */
  double getDiscountFactor(LegalEntity issuer, Double time);

  /**
  * Return the name associated to the discounting for an issuer.
  * @param issuer The issuer.
  * @return The name.
  */
  String getName(Pair<Object, LegalEntityFilter<LegalEntity>> issuer);

  /**
  * Return the name associated to the discounting for an issuer.
  * @param issuer The issuer.
  * @return The name.
  */
  String getName(LegalEntity issuer);

  /**
   * Gets the names of all curves (discounting, forward, and issuers).
   * @return The names.
   */
  Set<String> getAllNames();

  /**
   * Returns the MulticurveProvider from which the IssuerProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  MulticurveProviderInterface getMulticurveProvider();

  /**
   * Gets the number of parameters for the named curve
   * @param name The name
   * @return The number of parameters
   */
  Integer getNumberOfParameters(String name);

  /**
   * Gets the underlying curve names for a curve.
   * @param name The name
   * @return The underlying curve names
   */
  List<String> getUnderlyingCurvesNames(String name);

  /**
   * Gets all issuers represented in this bundle.
   * @return The issuers
   */
  Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers();
}
