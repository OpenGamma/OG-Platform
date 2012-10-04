/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RandomTradeProducer implements TradeProducer {
  
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(RandomTradeProducer.class);
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "TEST"));
  private static final String PROVIDER_ID_NAME  = "providerId";
  private static final String RANDOM_ID_SCHEME = "Rnd";
  
  private final Set<TradeListener> _tradeListeners = new CopyOnWriteArraySet<TradeListener>();
  private final SecurityMaster _securityMaster;
  private final List<ManageableSecurity> _securities;
  private final Random _random = new Random();
  
  public RandomTradeProducer(final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityMaster, "security master");
    _securityMaster = securityMaster;
    
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType(EquitySecurity.SECURITY_TYPE);
    SecuritySearchResult searchResult = _securityMaster.search(request);
    _securities = searchResult.getSecurities();
    
    Executors.newSingleThreadExecutor().submit(new Runnable() {
      
      @Override
      public void run() {
        while (true) {
          Trade trade = generateTrade();
          notifyTrade(trade);
          try {
            Thread.sleep(60000);
          } catch (InterruptedException ex) {
            Thread.interrupted();
            s_logger.warn("interrupted while waiting to generate trades", ex);
          }
        }
      }
    });
  }

  @Override
  public void addTradeListener(TradeListener tradeListener) {
    ArgumentChecker.notNull(tradeListener, "trade listener");
    _tradeListeners.add(tradeListener);
  }

  @Override
  public void removeTradeListener(TradeListener tradeListener) {
    ArgumentChecker.notNull(tradeListener, "trade listener");
    _tradeListeners.remove(tradeListener);
  }
  
  private Trade generateTrade() {
    SimpleTrade trade = new SimpleTrade();
    trade.setCounterparty(COUNTERPARTY);
    trade.setPremiumCurrency(Currency.USD);
    trade.setQuantity(BigDecimal.valueOf(_random.nextInt(10) + 10));
    trade.setTradeDate(LocalDate.now());
    trade.addAttribute(PROVIDER_ID_NAME, RANDOM_ID_SCHEME + "~" + GUIDGenerator.generate().toString());
    ManageableSecurity manageableSecurity = _securities.get(_random.nextInt(_securities.size()));
    trade.setSecurityLink(new SimpleSecurityLink(manageableSecurity.getExternalIdBundle().getExternalId(ExternalSchemes.OG_SYNTHETIC_TICKER)));
    return trade;
  }
  
  private void notifyTrade(Trade trade) {
    for (TradeListener listener : _tradeListeners) {
      listener.tradeReceived(trade);
    }
  }

}
