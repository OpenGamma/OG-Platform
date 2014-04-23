/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.CommodityFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureOptionDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.ExerciseTypeAnalyticsVisitorAdapter;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.CommodityFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CommodityFutureOptionConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  /** security source */
  private final SecuritySource _securitySource;
  /** Converter to get underlying future */
  private final FutureSecurityConverterDeprecated _futureSecurityConverter;

  /**
   * @param securitySource The security source, not null
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public CommodityFutureOptionConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final ConventionBundleSource conventionSource,
      final RegionSource regionSource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _securitySource = securitySource;
    final InterestRateFutureSecurityConverterDeprecated irFutureConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _futureSecurityConverter = new FutureSecurityConverterDeprecated(bondFutureConverter);
  }

  @Override
  public CommodityFutureOptionDefinition<?, ?> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityOption) {
    ArgumentChecker.notNull(commodityOption, "security");
    final ExternalIdBundle underlyingBundle = ExternalIdBundle.of(commodityOption.getUnderlyingId());
    final CommodityFutureSecurity underlyingSecurity = (CommodityFutureSecurity) _securitySource.getSingle(underlyingBundle);
    if (underlyingSecurity == null) {
      throw new OpenGammaRuntimeException("No underlying future found with identifier " + commodityOption.getUnderlyingId());
    }

    final ZonedDateTime expiry = underlyingSecurity.getExpiry().getExpiry();
    final boolean isCall = (commodityOption.getOptionType().equals(OptionType.CALL));
    final ExerciseDecisionType exerciseType = commodityOption.getExerciseType().accept(ExerciseTypeAnalyticsVisitorAdapter.getInstance());
    if (underlyingSecurity instanceof AgricultureFutureSecurity) {
      final AgricultureFutureDefinition underlyingDefinition = (AgricultureFutureDefinition) underlyingSecurity.accept(_futureSecurityConverter);
      return new AgricultureFutureOptionDefinition(expiry,
          underlyingDefinition,
          commodityOption.getStrike() * 100.0, // TODO: Remove when security stops scaling price
          exerciseType,
          isCall);
    } else if (underlyingSecurity instanceof EnergyFutureSecurity) {
      final EnergyFutureDefinition underlyingDefinition = (EnergyFutureDefinition) underlyingSecurity.accept(_futureSecurityConverter);
      return new EnergyFutureOptionDefinition(expiry,
          underlyingDefinition,
          commodityOption.getStrike() * 100.0, // TODO: Remove when security stops scaling price
          exerciseType,
          isCall);
    } else if (underlyingSecurity instanceof MetalFutureSecurity) {
      final MetalFutureDefinition underlyingDefinition = (MetalFutureDefinition) underlyingSecurity.accept(_futureSecurityConverter);
      return new MetalFutureOptionDefinition(expiry,
          underlyingDefinition,
          commodityOption.getStrike() * 100.0, // TODO: Remove when security stops scaling price
          exerciseType,
          isCall);
    } else {
      throw new OpenGammaRuntimeException("Unknown commodity option underlying type " + underlyingSecurity.getClass().getName());
    }
  }

}
