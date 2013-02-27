/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexDividendFutureDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Visits a Trade containing a FutureSecurity (OG-Financial)
 * Converts it to an InstrumentDefinitionWithData (OG-Analytics)
 */
public class FutureTradeConverter {

  /**
   * The security converter.
   */
  private final FutureSecurityConverter _futureSecurityConverter;

  public FutureTradeConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final ConventionBundleSource conventionSource,
      final RegionSource regionSource) {
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _futureSecurityConverter = new FutureSecurityConverter(irFutureConverter, bondFutureConverter);
  }

  /**
   * Converts a futures Trade to a Definition
   * @param trade The trade
   * @return EquityFutureDefinition
   */
  public InstrumentDefinitionWithData<?, Double> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    if (security instanceof FutureSecurity) {
      InstrumentDefinitionWithData<?, Double> securityDefinition = ((FutureSecurity) security).accept(_futureSecurityConverter);
      InstrumentDefinitionWithData<?, Double> tradeDefinition = securityToTrade(securityDefinition, trade.getPremium(),
          trade.getTradeDate().atTime(trade.getTradeTime().getTime()).atZone(ZoneOffset.UTC)); //TODO get the real time zone
      // TODO: The trade price is stored in the trade premium. This has to be corrected.
      return tradeDefinition;
    }
    throw new IllegalArgumentException("Can only handle FutureSecurity");
  }

  private InstrumentDefinitionWithData<?, Double> securityToTrade(InstrumentDefinitionWithData<?, Double> securityDefinition, final Double tradePrice, final ZonedDateTime tradeDate) {

    final InstrumentDefinitionVisitorAdapter<InstrumentDefinitionWithData<?, Double>, InstrumentDefinitionWithData<?, Double>> visitor =
        new InstrumentDefinitionVisitorAdapter<InstrumentDefinitionWithData<?, Double>, InstrumentDefinitionWithData<?, Double>>() {

          @Override
          public InstrumentDefinitionWithData<?, Double> visitAgricultureFutureDefinition(final AgricultureFutureDefinition futures) {
            return new AgricultureFutureDefinition(futures.getExpiryDate(), futures.getUnderlying(), futures.getUnitAmount(), null, null,
                1.0, futures.getUnitName(), futures.getSettlementType(), tradePrice, futures.getCurrency(), futures.getSettlementDate());
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitEnergyFutureDefinition(final EnergyFutureDefinition futures) {
            return new EnergyFutureDefinition(futures.getExpiryDate(), futures.getUnderlying(), futures.getUnitAmount(), null, null,
                1.0, futures.getUnitName(), futures.getSettlementType(), tradePrice, futures.getCurrency(), futures.getSettlementDate());
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitMetalFutureDefinition(final MetalFutureDefinition futures) {
            return new MetalFutureDefinition(futures.getExpiryDate(), futures.getUnderlying(), futures.getUnitAmount(), null, null,
                1.0, futures.getUnitName(), futures.getSettlementType(), tradePrice, futures.getCurrency(), futures.getSettlementDate());
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition futures) {
            return new EquityFutureDefinition(futures.getExpiryDate(), futures.getSettlementDate(), tradePrice, futures.getCurrency(), futures.getUnitAmount());
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitEquityFutureDefinition(final EquityFutureDefinition futures) {
            return new EquityFutureDefinition(futures.getExpiryDate(), futures.getSettlementDate(), tradePrice, futures.getCurrency(), futures.getUnitAmount());
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitBondFutureSecurityDefinition(final BondFutureDefinition futures) {
            return futures;
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition futures) {
            return new InterestRateFutureTransactionDefinition(futures, tradeDate, tradePrice, 1);
          }

        };

    return securityDefinition.accept(visitor);
  }

}
