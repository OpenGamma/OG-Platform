/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday;

import javax.time.calendar.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Link;
import com.opengamma.core.LinkResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.money.Currency;

/**
 * Resolver capable of providing holidays.
 * <p>
 * This resolver provides lookup of a {@link Holiday holiday} to the engine functions.
 * The lookup may require selecting a single "best match" from a set of potential options.
 * The best match behavior is the key part that distinguishes one implementation from another.
 * Best match selection may use a version-correction, configuration or code as appropriate.
 * Implementations of this interface must specify the rules they use to best match.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicAPI
public interface HolidayResolver extends LinkResolver<Holiday> {

  /**
   * Resolves the link to the provide the target holiday.
   * <p>
   * A link contains both an object and an external identifier bundle, although
   * typically only one of these is populated. Since neither input exactly specifies
   * a single version of a single holiday a best match is required.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param link  the link to be resolver, not null
   * @return the resolved target, not null
   * @throws DataNotFoundException if the target could not be resolved
   * @throws RuntimeException if an error occurs
   */
  Holiday resolve(Link<Holiday> link);

  /**
   * Gets a holiday by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single holiday at a single version-correction.
   * As such, there should be no complex matching issues in this lookup.
   * However, if the underlying data store does not handle versioning correctly,
   * then a best match selection may be required.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched holiday, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the holiday data could not be found
   * @throws RuntimeException if an error occurs
   */
  Holiday getHoliday(UniqueId uniqueId);

  /**
   * Gets a holiday by object identifier.
   * <p>
   * An object identifier exactly specifies a single holiday, but it provide no information
   * about the version-correction required.
   * As such, it is likely that multiple versions/corrections will match the object identifier.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param objectId  the object identifier to find, not null
   * @return the matched holiday, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the holiday data could not be found
   * @throws RuntimeException if an error occurs
   */
  Holiday getHoliday(ObjectId objectId);

  //-------------------------------------------------------------------------
  /**
   * Checks if a date is a holiday for a CURRENCY type.
   * 
   * @param dateToCheck  the date to check, not null
   * @param currency  the currency to check, not null
   * @return true if it is a holiday
   * @throws IllegalArgumentException if the input is invalid
   * @throws DataNotFoundException if the holiday data could not be found
   * @throws RuntimeException if an error occurs
   */
  boolean isHoliday(LocalDate dateToCheck, Currency currency);

  /**
   * Checks if a date is a holiday for a BANK, SETTLEMENT or TRADING type.
   * 
   * @param dateToCheck  the date to check, not null
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeId  the region or exchange to check, not null
   * @return true if it is a holiday
   * @throws IllegalArgumentException if the input is invalid
   * @throws DataNotFoundException if the holiday data could not be found
   * @throws RuntimeException if an error occurs
   */
  boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId);

  /**
   * Checks if a date is a holiday for a BANK, SETTLEMENT or TRADING type.
   * 
   * @param dateToCheck  the date to check, not null
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeIds  the regions or exchanges to check, not null
   * @return true if it is a holiday
   * @throws IllegalArgumentException if the input is invalid
   * @throws DataNotFoundException if the holiday data could not be found
   * @throws RuntimeException if an error occurs
   */
  boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds);

}
