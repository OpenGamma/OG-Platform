/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Base class for portfolio comparison tests.
 */
/* package */class AbstractTest {

  private final AtomicInteger _nextIdentifier = new AtomicInteger();
  private final Random _rnd = new Random(0);

  protected UniqueIdentifier createUniqueIdentifier(final String scheme) {
    return UniqueIdentifier.of(scheme, Integer.toString(_nextIdentifier.incrementAndGet()));
  }

  protected Identifier createIdentifier(final String scheme) {
    return Identifier.of(scheme, Integer.toString(_nextIdentifier.incrementAndGet()));
  }

  protected IdentifierBundle createIdentifierBundle() {
    switch (_rnd.nextInt(3)) {
      case 0:
        return IdentifierBundle.of(createIdentifier("Foo"));
      case 1:
        return IdentifierBundle.of(createIdentifier("Bar"));
      case 2:
        return IdentifierBundle.of(createIdentifier("Foo"), createIdentifier("Bar"));
      default:
        throw new IllegalStateException();
    }
  }

  protected Trade createTrade(final int quantity, final Security security, final String attr1Value, final String attr2Value) {
    final TradeImpl trade = new TradeImpl();
    trade.setQuantity(new BigDecimal(quantity));
    trade.setUniqueId(createUniqueIdentifier("Trade"));
    trade.setSecurityLink(SecurityLink.ofWeakId(security));
    if (attr1Value != null) {
      trade.addAttribute("Attr1", attr1Value);
    }
    if (attr2Value != null) {
      trade.addAttribute("Attr2", attr2Value);
    }
    return trade;
  }

  protected Position createPosition(final String uid, final int quantity, final Security security, final String attr1Value, final String attr2Value, final Trade trade1, final Trade trade2) {
    final PositionImpl position = new PositionImpl(new BigDecimal(quantity), security.getIdentifiers());
    position.setParentNodeId(createUniqueIdentifier("Node"));
    position.getSecurityLink().setTarget(security);
    if (attr1Value != null) {
      position.addAttribute("Attr1", attr1Value);
    }
    if (attr2Value != null) {
      position.addAttribute("Attr2", attr2Value);
    }
    if (trade1 != null) {
      position.addTrade(trade1);
    }
    if (trade2 != null) {
      position.addTrade(trade2);
    }
    position.setUniqueId(UniqueIdentifier.of("Position", uid));
    return position;
  }

  protected Security createRawSecurity(final String name, final int data) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(data);
    final RawSecurity security = new RawSecurity("WOSSNAME", baos.toByteArray());
    security.setIdentifiers(createIdentifierBundle());
    security.setName(name);
    security.setUniqueId(createUniqueIdentifier("Security"));
    return security;
  }

  protected Security createEquityOptionSecurity(final String name, final OptionType type, final double strike, final Identifier underlying) {
    final EquityOptionSecurity security = new EquityOptionSecurity(type, strike, Currency.USD, underlying, new AmericanExerciseType(), new Expiry(ZonedDateTime.of(2010, 10,
        10, 12, 0, 0, 0, TimeZone.UTC)), 0d, "EXCH");
    security.setIdentifiers(createIdentifierBundle());
    security.setName(name);
    security.setUniqueId(createUniqueIdentifier("Security"));
    return security;
  }

  protected Security createSwaptionSecurity(final String name, final boolean isPayer, final Currency currency, final Identifier underlying) {
    final SwaptionSecurity security = new SwaptionSecurity(isPayer, underlying, false, new Expiry(ZonedDateTime.of(2010, 10, 10, 12, 0, 0, 0, TimeZone.UTC)), false, currency);
    security.setIdentifiers(createIdentifierBundle());
    security.setName(name);
    security.setUniqueId(createUniqueIdentifier("Security"));
    return security;
  }

  protected Security createFRASecurity(final String name, final Currency currency, final double rate, final Identifier underlying) {
    final FRASecurity security = new FRASecurity(currency, Identifier.of("Region", "US"), ZonedDateTime.of(2010, 10, 10, 12, 0, 0, 0, TimeZone.UTC), ZonedDateTime.of(2012, 10, 10, 12, 0, 0, 0,
        TimeZone.UTC), rate, 0d, underlying);
    security.setIdentifiers(createIdentifierBundle());
    security.setName(name);
    security.setUniqueId(createUniqueIdentifier("Security"));
    return security;
  }

}
