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
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Base class for portfolio comparison tests.
 */
/* package */class AbstractTest {

  private final AtomicInteger _nextId = new AtomicInteger();
  private final Random _rnd = new Random(0);

  protected UniqueId createUniqueId(final String scheme) {
    return UniqueId.of(scheme, Integer.toString(_nextId.incrementAndGet()));
  }

  protected ExternalId createExternalId(final String scheme) {
    return ExternalId.of(scheme, Integer.toString(_nextId.incrementAndGet()));
  }

  protected ExternalIdBundle createExternalIdBundle() {
    switch (_rnd.nextInt(3)) {
      case 0:
        return ExternalIdBundle.of(createExternalId("Foo"));
      case 1:
        return ExternalIdBundle.of(createExternalId("Bar"));
      case 2:
        return ExternalIdBundle.of(createExternalId("Foo"), createExternalId("Bar"));
      default:
        throw new IllegalStateException();
    }
  }

  protected Trade createTrade(final int quantity, final Security security, final String attr1Value, final String attr2Value) {
    final SimpleTrade trade = new SimpleTrade();
    trade.setQuantity(new BigDecimal(quantity));
    trade.setUniqueId(createUniqueId("Trade"));
    trade.setSecurityLink(SimpleSecurityLink.ofBundleId(security));
    if (attr1Value != null) {
      trade.addAttribute("Attr1", attr1Value);
    }
    if (attr2Value != null) {
      trade.addAttribute("Attr2", attr2Value);
    }
    return trade;
  }

  protected Position createPosition(final String uid, final int quantity, final Security security, final String attr1Value, final String attr2Value, final Trade trade1, final Trade trade2) {
    final SimplePosition position = new SimplePosition(new BigDecimal(quantity), security.getExternalIdBundle());
    position.setParentNodeId(createUniqueId("Node"));
    position.setSecurityLink(SimpleSecurityLink.of(security));
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
    position.setUniqueId(UniqueId.of("Position", uid));
    return position;
  }

  protected Security createRawSecurity(final String name, final int data) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(data);
    final RawSecurity security = new RawSecurity("WOSSNAME", baos.toByteArray());
    security.setExternalIdBundle(createExternalIdBundle());
    security.setName(name);
    security.setUniqueId(createUniqueId("Security"));
    return security;
  }

  protected Security createEquityOptionSecurity(final String name, final OptionType type, final double strike, final ExternalId underlying) {
    final EquityOptionSecurity security = new EquityOptionSecurity(type, strike, Currency.USD, underlying, new AmericanExerciseType(), new Expiry(ZonedDateTime.of(2010, 10,
        10, 12, 0, 0, 0, TimeZone.UTC)), 0d, "EXCH");
    security.setExternalIdBundle(createExternalIdBundle());
    security.setName(name);
    security.setUniqueId(createUniqueId("Security"));
    return security;
  }

  protected Security createSwaptionSecurity(final String name, final boolean isPayer, final Currency currency, final ExternalId underlying) {
    final SwaptionSecurity security = new SwaptionSecurity(isPayer, underlying, false, new Expiry(ZonedDateTime.of(2010, 10, 10, 12, 0, 0, 0, TimeZone.UTC)), false, currency);
    security.setExternalIdBundle(createExternalIdBundle());
    security.setName(name);
    security.setUniqueId(createUniqueId("Security"));
    return security;
  }

  protected Security createFRASecurity(final String name, final Currency currency, final double rate, final ExternalId underlying) {
    final ZonedDateTime startDate = ZonedDateTime.of(2010, 10, 10, 12, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime settlementDate = startDate.plusDays(2);
    final FRASecurity security = new FRASecurity(currency, ExternalId.of("Region", "US"), startDate, ZonedDateTime.of(2012, 10, 10, 12, 0, 0, 0,
        TimeZone.UTC), rate, 0d, underlying, settlementDate);
    security.setExternalIdBundle(createExternalIdBundle());
    security.setName(name);
    security.setUniqueId(createUniqueId("Security"));
    return security;
  }

}
