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
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Visits a Trade containing a {@link FutureSecurity} (OG-Financial)
 * Converts it to an {@link InstrumentDefinitionWithData} (OG-Analytics)
 */
public class FutureTradeConverter implements TradeConverter {
  /**
   * The security converter (to convert the trade underlying).
   */
  private final FutureSecurityConverter _futureSecurityConverter;

  /**
   */
  public FutureTradeConverter() {
    _futureSecurityConverter = new FutureSecurityConverter();
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
      final InstrumentDefinitionWithData<?, Double> securityDefinition = ((FutureSecurity) security).accept(_futureSecurityConverter);
      double tradePremium = 0.0;
      if (trade.getPremium() != null) {
        tradePremium = trade.getPremium(); // TODO: The trade price is stored in the trade premium.
      }
      ZonedDateTime tradeDate = DateUtils.getUTCDate(1900, 1, 1);
      if ((trade.getTradeDate() != null) && trade.getTradeTime() != null && (trade.getTradeTime().toLocalTime() != null)) {
        tradeDate = trade.getTradeDate().atTime(trade.getTradeTime().toLocalTime()).atZone(ZoneOffset.UTC); //TODO get the real time zone
      }
      final int quantity = trade.getQuantity().intValue();
      final InstrumentDefinitionWithData<?, Double> tradeDefinition = securityToTrade(securityDefinition, tradePremium, tradeDate, quantity);
      return tradeDefinition;
    }
    throw new IllegalArgumentException("Can only handle FutureSecurity");
  }

  /**
   * Creates an OG-Analytics trade definition from the OG-Analytics security definition and the trade details (price and date).
   * @param securityDefinition The security definition (OG-Analytics object).
   * @param tradePrice The trade price.
   * @param tradeDate The trade date.
   * @return The tradeDefinition.
   */
  private static InstrumentDefinitionWithData<?, Double> securityToTrade(final InstrumentDefinitionWithData<?, Double> securityDefinition, final Double tradePrice,
      final ZonedDateTime tradeDate, final int quantity) {

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
          public InstrumentDefinitionWithData<?, Double> visitIndexFutureDefinition(final IndexFutureDefinition futures) {
            return new IndexFutureDefinition(futures.getExpiryDate(), futures.getSettlementDate(), tradePrice, futures.getCurrency(), futures.getUnitAmount(), futures.getUnderlying());
          }

        };

    return securityDefinition.accept(visitor);
  }

}
