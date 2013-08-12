/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractRedisTestCase;

/**
 * 
 */
@Test(enabled=false)
public class NonVersionedRedisHolidaySourceTest extends AbstractRedisTestCase {
  
  public void addGetByUniqueIdEmpty() {
    NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());
    
    Holiday result = source.get(UniqueId.of("TEST", "No Such Thing"));
    assertNull(result);
  }
  
  public void addGetByUniqueIdCurrency() {
    NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());
    
    SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    source.addHoliday(usd);
    
    Holiday result = source.get(usd.getUniqueId());
    assertNotNull(result);
    assertEquals(Currency.USD, result.getCurrency());
    assertEquals(usd.getUniqueId(), result.getUniqueId());
    assertEquals(HolidayType.CURRENCY, result.getType());
    assertNull(usd.getExchangeExternalId());
    assertNull(usd.getRegionExternalId());
  }
  
  public void isHolidayCurrency() {
    NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());
    
    SimpleHoliday usd = generateHoliday(20);
    usd.setCurrency(Currency.USD);
    usd.setType(HolidayType.CURRENCY);
    usd.setUniqueId(UniqueId.of("TEST", "USD Test Holiday"));
    source.addHoliday(usd);
    
    assertTrue(source.isHoliday(LocalDate.now(), Currency.USD));
    assertFalse(source.isHoliday(LocalDate.now(), Currency.CAD));
    assertFalse(source.isHoliday(LocalDate.now().plusDays(1), Currency.USD));
  }
  
  public void addGetByUniqueIdRegion() {
    NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());
    
    SimpleHoliday usBank = generateHoliday(20);
    usBank.setType(HolidayType.BANK);
    usBank.setRegionExternalId(ExternalId.of("RegionScheme", "Chicago"));
    usBank.setUniqueId(UniqueId.of("TEST", "USD Test Bank Holiday"));
    source.addHoliday(usBank);
    
    Holiday result = source.get(usBank.getUniqueId());
    assertNotNull(result);
    assertEquals(usBank.getUniqueId(), result.getUniqueId());
    assertEquals(HolidayType.BANK, result.getType());
    assertNull(usBank.getCurrency());
    assertEquals(usBank.getRegionExternalId(), result.getRegionExternalId());
    assertNull(result.getExchangeExternalId());
  }
  
  public void isHolidayByTypeExternalId() {
    NonVersionedRedisHolidaySource source = new NonVersionedRedisHolidaySource(getJedisPool(), getRedisPrefix());
    
    ExternalId exchangeId = ExternalId.of("ExchangeScheme", "Eurex");
    SimpleHoliday holiday1 = generateHoliday(20);
    holiday1.setType(HolidayType.TRADING);
    
    holiday1.setExchangeExternalId(exchangeId);
    holiday1.setUniqueId(UniqueId.of("EUREX", "1"));
    source.addHoliday(holiday1);
    
    SimpleHoliday holiday2 = generateHoliday(20);
    holiday2.setType(HolidayType.SETTLEMENT);
    holiday2.setExchangeExternalId(exchangeId);
    holiday2.setUniqueId(UniqueId.of("EUREX", "2"));
    source.addHoliday(holiday2);
    
    assertTrue(source.isHoliday(LocalDate.now(), HolidayType.TRADING, exchangeId));
    assertFalse(source.isHoliday(LocalDate.now().minusDays(10), HolidayType.TRADING, exchangeId));
    assertFalse(source.isHoliday(LocalDate.now().plusYears(1), HolidayType.TRADING, exchangeId));
    
    assertTrue(source.isHoliday(LocalDate.now(), HolidayType.SETTLEMENT, exchangeId));
    assertFalse(source.isHoliday(LocalDate.now().minusDays(10), HolidayType.SETTLEMENT, exchangeId));
    assertFalse(source.isHoliday(LocalDate.now().plusYears(1), HolidayType.SETTLEMENT, exchangeId));
    
  }
  
  protected SimpleHoliday generateHoliday(int nHolidays) {
    SimpleHoliday holiday = new SimpleHoliday();
    
    LocalDate date = LocalDate.now();
    for (int i = 0; i < nHolidays; i++) {
      holiday.addHolidayDate(date);
      date = date.plusDays(3);
    }
    
    return holiday;
  }

}
