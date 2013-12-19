/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity;

import java.util.Collection;

import com.opengamma.core.Attributable;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A legal entity that provides common or shared information about a financial product.
 * <p/>
 * A legal entity is used to capture information that is common in a market.
 * For example, they are used in curve and security construction.
 * <p/>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface LegalEntity extends UniqueIdentifiable, ExternalBundleIdentifiable, Attributable {

  /**
   * Gets the external identifier bundle that defines the legal entity.
   * <p/>
   * Each external system has one or more identifiers by which they refer to the legal entity.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   *
   * @return the bundle defining the legal entity, not null
   */
  @Override
  // override for Javadoc
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the name of the legal entity.
   *
   * @return the name, not null
   */
  String getName();

  /**
   * Gets the ratings of the legal entity.
   *
   * @return the ratings, not null
   */
  Collection<Rating> getRatings();

  /**
   * Gets the capabilities of the legal entity.
   *
   * @return the capabilities, not null
   */
  Collection<Capability> getCapabilities();

  Collection<Object> getIssuedSecurities(); //TODO refactor <Object> to <SecurityLink> when SecurityLink is ready.

  Collection<Obligation> getObligations();

  Collection<Account> getAccounts();

  Collection<Object> getPortfolios(); //TODO refactor <Object> to <PortfolioLink> when PortfolioLink is ready.

}
