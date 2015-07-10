/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import java.util.Set;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.ZoneId;

import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * A region of the world.
 * <p>
 * Many aspects of business, algorithms and contracts are specific to a region. The region may be of any size, from a municipality to a super-national group.
 * <p>
 * This interface is read-only. Implementations may be mutable.
 */
@PublicAPI
public interface Region extends UniqueIdentifiable, ExternalBundleIdentifiable {

  /**
   * Gets the unique identifier of the region.
   * <p>
   * This specifies a single version-correction of the region.
   * 
   * @return the unique identifier for this region, not null within the engine
   */
  @Override
  UniqueId getUniqueId();

  /**
   * Gets the external identifier bundle that defines the region.
   * <p>
   * Each external system has one or more identifiers by which they refer to the region.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * <p>
   * This will include the country, currency and time-zone.
   * 
   * @return the bundle defining the region, not null
   */
  @Override  // override for Javadoc
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the classification of the region.
   * 
   * @return the classification of region, such as SUPER_NATIONAL or INDEPENDENT_STATE, not null
   */
  RegionClassification getClassification();

  /**
   * Gets the unique identifiers of the regions that this region is a member of. For example, a country might be a member of the World, UN, European Union and NATO.
   * 
   * @return the parent unique identifiers, null if this is the root entry
   */
  Set<UniqueId> getParentRegionIds();

  /**
   * Gets the country.
   * 
   * @return the country, null if not applicable
   */
  Country getCountry();

  /**
   * Gets the currency.
   * 
   * @return the currency, null if not applicable
   */
  Currency getCurrency();

  /**
   * Gets the time-zone. For larger regions, there can be multiple time-zones, so this is only reliable for municipalities.
   * 
   * @return the time-zone, null if not applicable
   */
  ZoneId getTimeZone();

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
   * Gets the extensible data store for additional information. Applications may store additional region based information here.
   * 
   * @return the additional data, not null
   */
  FlexiBean getData();

}
