/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Calendar;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Trade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Generic extractor to extract the securities from a trade.
 *
 * @param <T> the type of trade handled by the extractor
 */
public abstract class TradeSecurityExtractor<T extends Trade> {

  /**
   * The trade to perform the extraction on.
   */
  private final T _trade;

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public TradeSecurityExtractor(T trade) {
    _trade = trade;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the trade.
   * 
   * @return the trade, not null
   */
  public T getTrade() {
    return _trade;
  }

  //-------------------------------------------------------------------------
  /**
   * Extract the securities from the embedded trade.
   *
   * @return the array of securities from the trade
   */
  public abstract ManageableSecurity[] extractSecurities();

  /**
   * Utility method to convert a local date to a ZonedDateTime.
   *
   * @param date date to convert
   * @return the converted ZonedDateTime
   */
  protected ZonedDateTime convertLocalDate(LocalDate date) {
    return date.atStartOfDay(ZoneOffset.UTC);
  }

  /**
   * Utility method to extract the set of region identifiers from the supplied calendars.
   *
   * @param calendars the calendars to extract the regions from
   * @return the extracted regions
   */
  protected Set<String> extractCalendarRegions(Set<Calendar> calendars) {

    Set<String> regions = Sets.newHashSet();
    for (Calendar calendar : calendars) {
      regions.add(calendar.getId().getId());
    }

    return regions;
  }

  /**
   * Utility method to extract a single external region identifier from the supplied set of calendars.
   *
   * @param calendars the calendars to extract the region identifier from.
   * @return the external region id
   */
  protected ExternalId extractRegion(Set<Calendar> calendars) {
    Set<String> calendarRegions = extractCalendarRegions(calendars);
    return ExternalSchemes.financialRegionId(StringUtils.join(calendarRegions, "+"));
  }

  /**
   * Utility method to wrap the passed set of securities into an array.
   *
   * @param securities the securities to be wrapped
   * @return an array of the securities
   */
  protected ManageableSecurity[] securityArray(ManageableSecurity... securities) {
    return securities;
  }

  /**
   * Add an external identififer to the supplied security, returning the modified security.
   *
   * @param security the security to add an identifier to
   * @return the modified security
   */
  protected ManageableSecurity addIdentifier(ManageableSecurity security) {

    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(security)
            .toHashCode()
    )));
    return security;
  }

}
