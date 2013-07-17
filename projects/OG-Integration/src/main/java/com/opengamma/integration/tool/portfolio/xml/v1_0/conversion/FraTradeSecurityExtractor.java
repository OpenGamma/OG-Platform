/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FraTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Security extractor for fra trades.
 */
public class FraTradeSecurityExtractor extends TradeSecurityExtractor<FraTrade> {

  private static final Logger s_logger = LoggerFactory.getLogger(FraTradeSecurityExtractor.class);
  
  /**
   * Create an extractor for the given trade.
   * @param trade the trade to 
   */
  public FraTradeSecurityExtractor(FraTrade trade) {
    super(trade);
  }

  @Override
  public ManageableSecurity[] extractSecurities() {
    FraTrade fraTrade = getTrade();
    
    validate(fraTrade);
    
    boolean payFixed = fraTrade.isPayFixed();
    
    double specifiedNotional = fraTrade.getNotional().doubleValue();
    
    //sign of notional determines the sides of the trade. 
    //pay fixed => positive; pay floating => negative
    double absoluteNotional = payFixed ? specifiedNotional : -specifiedNotional;
    
    ExternalId underlyingIdentifier = fraTrade.getFixingIndex().getIndex().toExternalId();
    
    
    FRASecurity fraSecurity = new FRASecurity(fraTrade.getCurrency(), 
                  fraTrade.getRegionId().toExternalId(), //region id not used.
                  convertLocalDate(fraTrade.getEffectiveDate()),
                  convertLocalDate(fraTrade.getTerminationDate()), 
                  fraTrade.getRate().doubleValue(), 
                  absoluteNotional, 
                  underlyingIdentifier, 
                  convertLocalDate(fraTrade.getFixingDate()));
    
    return securityArray(addIdentifier(fraSecurity));
  }

  /**
   * Checks all is as expected.
   * @param fraTrade fraTrade to validate
   */
  private void validate(FraTrade fraTrade) {
    String tradeId = fraTrade.getExternalSystemId().getExternalId().getId();
    
    LocalDate effectiveDate = fraTrade.getEffectiveDate();
    LocalDate paymentDate = fraTrade.getPaymentDate();
    if (!effectiveDate.equals(paymentDate)) {
      throw new OpenGammaRuntimeException(
          format("Effective date (%s) not equal to payment date (%s) on trade '%s'. "
              + "This is not currently supported.",
              effectiveDate.toString(),
              paymentDate.toString(),
              tradeId));
    }
    
    //TODO would be better to check values against conventions here.
    //this will require a refactor to get access to the ToolContext.
    if (fraTrade.getBusinessDayConvention() != null || fraTrade.getDayCount() != null) {
      s_logger.warn("businessDayConvention and/or dayCount specified for trade %s. " +
          "Note: this is currently ignored in favour of index defaults.", tradeId);
    }
    
  }
}
