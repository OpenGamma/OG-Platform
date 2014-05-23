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
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Visits a Trade containing a {@link FutureSecurity} (OG-Financial)
 * Converts it to an {@link InstrumentDefinitionWithData} (OG-Analytics)
 * @deprecated Use the version that takes a {@link ConventionSource}
 */
@Deprecated
public class FutureTradeConverterDeprecated {

  /**
   * The security converter (to convert the trade underlying).
   */
  private final FutureSecurityConverterDeprecated _futureSecurityConverter;
  
  private final InterestRateFutureTradeConverterDeprecated _irFutureTradeConverter;

  /**
   * Constructor.
   * @param securitySource The security source.
   * @param holidaySource The holiday source.
   * @param conventionSource The convention source.
   * @param regionSource The region source.
   */
  public FutureTradeConverterDeprecated(final SecuritySource securitySource, final HolidaySource holidaySource, final ConventionBundleSource conventionSource,
      final RegionSource regionSource) {
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final BondFutureSecurityConverter bondFutureConverter = new BondFutureSecurityConverter(securitySource, bondConverter);
    _futureSecurityConverter = new FutureSecurityConverterDeprecated(bondFutureConverter);
    
    final InterestRateFutureSecurityConverterDeprecated irFutureSecurityConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    _irFutureTradeConverter = new InterestRateFutureTradeConverterDeprecated(irFutureSecurityConverter);
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
      final InstrumentDefinitionWithData<?, Double> securityDefinition;
      if (security instanceof InterestRateFutureSecurity) {
        securityDefinition = _irFutureTradeConverter.convert(trade);
      } else {
        securityDefinition = ((FutureSecurity) security).accept(_futureSecurityConverter);
      }
      double tradePremium = 0.0;
      if (trade.getPremium() != null) {
        tradePremium = trade.getPremium(); // TODO: The trade price is stored in the trade premium. This has to be corrected.
      }
      ZonedDateTime tradeDate = DateUtils.getUTCDate(1900, 1, 1);
      if ((trade.getTradeDate() != null) && trade.getTradeTime() != null && (trade.getTradeTime().toLocalTime() != null)) {
        tradeDate = trade.getTradeDate().atTime(trade.getTradeTime().toLocalTime()).atZone(ZoneOffset.UTC); //TODO get the real time zone
      }
      final InstrumentDefinitionWithData<?, Double> tradeDefinition = securityToTrade(securityDefinition, tradePremium, tradeDate);
      return tradeDefinition;
    }
    throw new IllegalArgumentException("Can only handle FutureSecurity");
  }

  /**
   * Creates the OG-Analytics tradeDefinition from the OG-Analytics securityDefinition and the trade details (price and date).
   * @param securityDefinition The security definition (OG-Analytics object).
   * @param tradePrice The trade price.
   * @param tradeDate The trade date.
   * @return The tradeDefinition.
   */
  private static InstrumentDefinitionWithData<?, Double> securityToTrade(final InstrumentDefinitionWithData<?, Double> securityDefinition, final Double tradePrice,
      final ZonedDateTime tradeDate) {

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
          public InstrumentDefinitionWithData<?, Double> visitBondFutureDefinition(final BondFutureDefinition futures) {
            return futures;
          }

          @Override
          public InstrumentDefinitionWithData<?, Double> visitIndexFutureDefinition(final IndexFutureDefinition futures) {
            return new IndexFutureDefinition(futures.getExpiryDate(), futures.getSettlementDate(), tradePrice, futures.getCurrency(), futures.getUnitAmount(), futures.getUnderlying());
          }

        };

    return securityDefinition.accept(visitor);
  }

}
