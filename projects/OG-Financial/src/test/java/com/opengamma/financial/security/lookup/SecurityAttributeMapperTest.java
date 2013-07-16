/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

import static com.opengamma.financial.security.lookup.SecurityAttribute.MATURITY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.PRODUCT;
import static com.opengamma.financial.security.lookup.SecurityAttribute.QUANTITY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.RATE;
import static com.opengamma.financial.security.lookup.SecurityAttribute.TYPE;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityAttributeMapperTest {

  private static final CurrencyPairs s_currencyPairs = CurrencyPairs.of(ImmutableSet.of(CurrencyPair.of(Currency.GBP,
                                                                                                        Currency.USD)));
  private static final SecurityAttributeMapper s_defaultMappings = DefaultSecurityAttributeMappings.create(s_currencyPairs);

  /**
   * Simple security where fields are mapped using bean properties
   */
  @Test
  public void fra() {
    ExternalId regionId = ExternalId.of("Reg", "123");
    ExternalId underlyingId = ExternalId.of("Und", "321");
    ZonedDateTime startDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime endDate = zdt(2013, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    ZonedDateTime fixingDate = zdt(2013, 12, 20, 11, 0, 0, 0, ZoneOffset.UTC);
    FRASecurity security = new FRASecurity(Currency.AUD, regionId, startDate, endDate, 0.1, 1000, underlyingId, fixingDate);
    assertEquals("FRA", s_defaultMappings.valueFor(TYPE, security));
    assertEquals(Currency.AUD, s_defaultMappings.valueFor(PRODUCT, security));
    assertEquals(1000d, s_defaultMappings.valueFor(QUANTITY, security));
  }

  /**
   * Custom providers for values derived from multiple security properties
   */
  @Test
  public void fxForward() {
    ZonedDateTime forwardDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    ExternalId regionId = ExternalId.of("Reg", "123");
    FXForwardSecurity security = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    assertEquals("FX Forward", s_defaultMappings.valueFor(TYPE, security));
    assertEquals("GBP/USD", s_defaultMappings.valueFor(PRODUCT, security));
    assertEquals(forwardDate, s_defaultMappings.valueFor(MATURITY, security));
    FXAmounts expected = FXAmounts.forForward(security.getPayCurrency(),
                                              security.getReceiveCurrency(),
                                              security.getPayAmount(),
                                              security.getReceiveAmount(),
                                              s_currencyPairs);
    assertEquals(expected, s_defaultMappings.valueFor(QUANTITY, security));
    assertEquals(1.5d, s_defaultMappings.valueFor(RATE, security));
  }

  /**
   * if no columns are mapped for a class then it should inherit mappings set up for its superclasses
   */
  @Test
  public void inheritSuperclassMappings() {
    class A extends ManageableSecurity {
      private static final long serialVersionUID = 1L;
    }
    class B extends A {
      private static final long serialVersionUID = 1L;
    }
    class C extends B {
      private static final long serialVersionUID = 1L;
    }
    SecurityAttributeMapper mapper = new SecurityAttributeMapper();
    String aType = "A type";
    String bProduct = "B product";
    mapper.mapColumn(TYPE, A.class, aType);
    mapper.mapColumn(PRODUCT, B.class, bProduct);
    C c = new C();

    // check the case where there are no columns mapped for a subtype
    assertEquals(aType, mapper.valueFor(TYPE, c));
    assertEquals(bProduct, mapper.valueFor(PRODUCT, c));

    // add a mapping for the subtype and check the supertype mappings are still picked up
    String cMaturity = "C maturity";
    mapper.mapColumn(MATURITY, C.class, cMaturity);

    assertEquals(aType, mapper.valueFor(TYPE, c));
    assertEquals(bProduct, mapper.valueFor(PRODUCT, c));
    assertEquals(cMaturity, mapper.valueFor(MATURITY, c));

    // check overriding works
    String cType = "C type";
    mapper.mapColumn(TYPE, C.class, cType);
    assertEquals(cType, mapper.valueFor(TYPE, c));
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
