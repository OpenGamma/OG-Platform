package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Calendar;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Trade;
import com.opengamma.master.security.ManageableSecurity;

public abstract class TradeSecurityExtractor<T extends Trade> {

  public abstract ManageableSecurity extractSecurity(T trade);

  protected ZonedDateTime convertLocalDate(LocalDate date) {
    return date.atStartOfDay(ZoneOffset.UTC);
  }

  protected Set<String> extractCalendarRegions(Set<Calendar> calendars) {

    Set<String> regions = Sets.newHashSet();
    for (Calendar calendar : calendars) {

      regions.add(calendar.getId().getId());
    }

    return regions;
  }
}
