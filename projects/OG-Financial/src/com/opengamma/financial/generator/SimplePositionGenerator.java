/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for creating a position as a quantity/security pair.
 * 
 * @param <T> the security type or a common super-type of the securities
 */
public class SimplePositionGenerator<T extends ManageableSecurity> implements PositionGenerator {

  private static final String DEFAULT_COUNTER_PARTY = "COUNTERPARTY";
  
  private final QuantityGenerator _quantityGenerator;
  private final SecurityGenerator<? extends T> _securityGenerator;
  private final SecurityPersister _securityPersister;
  private final NameGenerator _counterPartyGenerator;

  public SimplePositionGenerator(final QuantityGenerator quantityGenerator, final SecurityGenerator<? extends T> securityGenerator, 
      final SecurityPersister securityPersister, final NameGenerator counterPartyGenerator) {
    ArgumentChecker.notNull(quantityGenerator, "quantityGenerator");
    ArgumentChecker.notNull(securityGenerator, "securityGenerator");
    ArgumentChecker.notNull(securityPersister, "securityPersister");
    ArgumentChecker.notNull(counterPartyGenerator, "counterPartyGenerator");
    _quantityGenerator = quantityGenerator;
    _securityGenerator = securityGenerator;
    _securityPersister = securityPersister;
    _counterPartyGenerator = counterPartyGenerator;
  }

  public SimplePositionGenerator(final SecurityGenerator<? extends T> securityGenerator, final SecurityPersister securityPersister, final NameGenerator counterPartyGenerator) {
    this(new StaticQuantityGenerator(1), securityGenerator, securityPersister,  counterPartyGenerator);
  }
  
  public SimplePositionGenerator(final SecurityGenerator<? extends T> securityGenerator, final SecurityPersister securityPersister) {
    this(new StaticQuantityGenerator(1), securityGenerator, securityPersister);
  }
  
  public SimplePositionGenerator(final QuantityGenerator quantityGenerator, final SecurityGenerator<? extends T> securityGenerator, final SecurityPersister securityPersister) {
    this(quantityGenerator, securityGenerator, securityPersister, new StaticNameGenerator(DEFAULT_COUNTER_PARTY));
  }

  protected QuantityGenerator getQuantityGenerator() {
    return _quantityGenerator;
  }

  protected SecurityGenerator<? extends T> getSecurityGenerator() {
    return _securityGenerator;
  }

  protected SecurityPersister getSecurityPersister() {
    return _securityPersister;
  }
  
  protected NameGenerator getCounterPartyGenerator() {
    return _counterPartyGenerator;
  }

  public static Position createPositionFromTrade(final ManageableTrade trade) {
    if (trade != null) {
      final SimplePosition position = new SimplePosition(trade.getQuantity(), trade.getSecurityLink().getExternalId());
      position.addTrade(trade);
      return position;
    } else {
      return null;
    }
  }

  @Override
  public Position createPosition() {
//    final ManageableTrade trade = getSecurityGenerator().createSecurityTrade(getQuantityGenerator(), getSecurityPersister(), getCounterPartyGenerator());
//    if (trade == null) {
//      final BigDecimal quantity = getQuantityGenerator().createQuantity();
//      if (quantity == null) {
//        return null;
//      }
//      final T security = getSecurityGenerator().createSecurity();
//      if (security == null) {
//        return null;
//      }
//      return new SimplePosition(quantity, getSecurityPersister().storeSecurity(security));
//    } else {
//      return createPositionFromTrade(trade);
//    }
    
    Position position = null;
    final ManageableTrade trade = getSecurityGenerator().createSecurityTrade(getQuantityGenerator(), getSecurityPersister(), getCounterPartyGenerator());
    if (trade != null) {
      position = createPositionFromTrade(trade);
    }
    return position;
  }

}
