/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.HashMap;
import java.util.Map;

import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityDefinition;
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.legalentity.GICSCode;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts {@link EquityTotalReturnSwapSecurity} classes to {@link EquityTotalReturnSwapDefinition},
 * which are required for use in the analytics library.
 */
public class EquityTotalReturnSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The security source */
  private final SecuritySource _securitySource;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param securitySource The security source, not null
   */
  public EquityTotalReturnSwapSecurityConverter(final ConventionSource conventionSource, final HolidaySource holidaySource,
      final SecuritySource securitySource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _securitySource = securitySource;
  }

  @Override
  public EquityTotalReturnSwapDefinition visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final FinancialSecurity underlying = (FinancialSecurity) _securitySource.getSingle(security.getAssetId().toBundle()); //TODO ignoring version
    if (underlying instanceof BondSecurity) {
      throw new OpenGammaRuntimeException("Underlying for equity TRS was not an equity");
    }
    final FloatingInterestRateSwapLeg fundingLeg = security.getFundingLeg();
    final boolean isPayer = fundingLeg.getPayReceiveType() == PayReceiveType.PAY ? true : false;
    final LocalDate startDate = security.getEffectiveDate();
    final LocalDate endDate = security.getMaturityDate();
    final NotionalExchange notionalExchange = NotionalExchange.NO_EXCHANGE;
    final AnnuityDefinition<? extends PaymentDefinition> annuityDefinition = AnnuityUtils.buildFloatingAnnuityDefinition(_conventionSource, _holidaySource, _securitySource, isPayer,
        startDate, endDate, notionalExchange, fundingLeg);
    final EquitySecurity equity = (EquitySecurity) underlying;
    final LegalEntity legalEntity = getLegalEntityForEquity(equity);
    final EquityDefinition equityDefinition = new EquityDefinition(legalEntity, equity.getCurrency(), security.getNumberOfShares());
    final ZonedDateTime startDateTime = startDate.atTime(LocalTime.MIN).atZone(ZoneOffset.UTC);
    final ZonedDateTime endDateTime = endDate.atTime(LocalTime.MIN).atZone(ZoneOffset.UTC);
    return new EquityTotalReturnSwapDefinition(startDateTime, endDateTime, annuityDefinition, equityDefinition, security.getNotionalAmount(), 
        security.getNotionalCurrency(), security.getDividendPercentage() / 100.);
  }

  /**
   * Gets the legal entity of an equity from information in the security. Sets the ticker, short name and the
   * sector (GICS code only) if the GICS code is available.
   * @param equity The equity
   * @return The legal entity
   */
  private static LegalEntity getLegalEntityForEquity(final EquitySecurity equity) {
    if (equity.getGicsCode() != null) {
      final GICSCode gics = GICSCode.of(equity.getGicsCode().getCode());
      final Map<String, Object> map = new HashMap<>();
      final FlexiBean classifications = new FlexiBean();
      map.put(GICSCode.NAME, gics);
      classifications.putAll(map);
      final Sector sector = Sector.of(equity.getGicsCode().getSectorDescription(), classifications);
      return new LegalEntity(equity.getShortName(), equity.getCompanyName(), null, sector, null);
    }
    return new LegalEntity(equity.getShortName(), equity.getCompanyName(), null, null, null);
  }

}
