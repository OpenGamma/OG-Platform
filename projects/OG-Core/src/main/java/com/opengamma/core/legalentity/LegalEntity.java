/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity;

import java.util.List;
import java.util.Map;

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
  List<Rating> getRatings();

  /**
   * Gets the capabilities of the legal entity.
   *
   * @return the capabilities, not null
   */
  List<Capability> getCapabilities();

  /**
   * Gets the securities issued by the legal entity
   *
   * @return the securities issued by the legal entity
   */
  List<ExternalIdBundle> getIssuedSecurities(); //TODO refactor <ExternalIdBundle> to <SecurityLink> when SecurityLink is ready.

  /**
   * Gets the obligations of the legal entity
   *
   * @return the obligations of a legal entity
   */
  List<Obligation> getObligations();

  /**
   * Gets the accounts of the legal entity
   *
   * @return the accounts of a legal entity
   */
  List<Account> getAccounts();

  /**
   * Gets the portfolio of the legal entity
   *
   * @return the portfolio of the legal entity
   */
  RootPortfolio getRootPortfolio();

  /**
   * Gets the entire set of details.
   * <p/>
   * Details are used to tag the object with additional information.
   *
   * @return the complete set of details, not null
   */
  Map<String, String> getDetails();

  /**
   * Sets the entire set of details.
   * <p/>
   * Details are used to tag the object with additional information.
   *
   * @param details the new set of details, not null
   */
  void setDetails(Map<String, String> details);

  /**
   * Adds a key-value pair to the set of details
   * <p/>
   * Details are used to tag the object with additional information.
   *
   * @param key the key to add, not null
   * @param value the value to add, not null
   */
  void addDetail(String key, String value);

}
