/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import java.util.Set;

import javax.time.calendar.TimeZone;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A region of the world.
 * <p>
 * Many aspects of business, algorithms and contracts are specific to a region.
 * The region may be of any size, from a municipality to a super-national group.
 */
@PublicSPI
public interface Region extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the region.
   * 
   * @return the unique identifier for this region entry, not null
   */
  UniqueIdentifier getUniqueId();

  /**
   * Gets the classification of the region.
   * 
   * @return the classification of region, such as SUPER_NATIONAL or INDEPENDENT_STATE, not null
   */
  RegionClassification getClassification();

  /**
   * Gets the unique identifiers of the regions that this region is a member of.
   * For example, a country might be a member of the World, UN, European Union and NATO.
   * 
   * @return the parent unique identifiers, null if this is the root entry
   */
  Set<UniqueIdentifier> getParentRegionIds();

  /**
   * Gets the short descriptive name of the region.
   * 
   * @return the name of the region, not null
   */
  String getName();

  /**
   * Gets the full descriptive name of the region.
   * 
   * @return the full name of the region, not null
   */
  String getFullName();

  /**
   * Gets the identifiers defining the region.
   * <p>
   * This will include the country, currency and time-zone.
   * 
   * @return the identifiers, null if not applicable
   */
  IdentifierBundle getIdentifiers();

  /**
   * Gets the country ISO code.
   * 
   * @return the 2 letter country code, null if not applicable
   */
  String getCountryISO();

  /**
   * Gets the currency.
   * 
   * @return the currency, null if not applicable
   */
  Currency getCurrency();

  /**
   * Gets the time-zone.
   * For larger regions, there can be multiple time-zones, so this is only reliable
   * for municipalities.
   * 
   * @return the time-zone, null if not applicable
   */
  TimeZone getTimeZone();

  /**
   * Gets the extensible data store for additional information.
   * Applications may store additional region based information here.
   * 
   * @return the additional data, not null
   */
  FlexiBean getData();

}
