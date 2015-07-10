/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.timeseries.date.localdate.LocalDateToIntConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.metric.OpenGammaMetricRegistry;
import com.opengamma.util.money.Currency;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/*
 * REDIS DATA STRUCTURES:
 * Data structure for holiday metadata:
 *     Key["UNQ-"UniqueId] -> Hash
 *        Hash[REGION_SCHEME] -> Region Scheme
 *        Hash[REGION] -> Region code
 *        Hash[EXCHANGE_SCHEME] -> Exchange scheme
 *        Hash[EXCHANGE] -> Exchange code
 *        Hash[CURRENCY] -> ISO currency code
 *        Hash[TYPE] -> HolidayType
 * Data structure for holiday days themselves:
 *     Key["UNQ-"UniqueId"-DAYS"] -> Sorted Set (days as ints)
 * 
 * Those give the core data, but we need search capabilities as well.
 * 
 *     Key["EXT-"ExternalId"-TYPE-"HolidayType] -> Hash
 *        Hash[UNIQUE_ID] -> UniqueId
 *     Key["CUR-"currencyCode] -> Hash
 *        Hash[UNIQUE_ID] -> UniqueId
 * 
 * While this data structure is more than necessary (in that you could cut out the hash for
 * the lookups), it allows future expansion if more data is required to be stored
 * later without reformatting the Redis instance.
 */

/**
 * A lightweight {@link HolidaySource} that cannot handle any versioning, and
 * which stores all Holiday documents as individual Redis elements using direct
 * Redis types rather than Fudge encoding.
 *
 * Treats Saturday & Sunday as non working days.
 */
public class NonVersionedRedisHolidaySource implements HolidaySource {
  private static final Logger s_logger = LoggerFactory.getLogger(NonVersionedRedisHolidaySource.class);
  private static final String EXCHANGE = "EXCHANGE";
  private static final String EXCHANGE_SCHEME = "EXCHANGE_SCHEME";
  private static final String CUSTOM_SCHEME = "CUSTOM_SCHEME";
  /** Currency key */
  public static final String CURRENCY = "CURRENCY";
  /** Type key */
  public static final String TYPE = "TYPE";
  /** UniqueId key */
  public static final String UNIQUE_ID = "UNIQUE_ID";
  /** Region value key */
  public static final String REGION = "REGION";
  /** Custom value key */
  public static final String CUSTOM = "CUSTOM";
  /** Region scheme key */
  public static final String REGION_SCHEME = "REGION_SCHEME";
  /** The default scheme for unique identifiers. */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "RedisHol";
  
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private Timer _getTimer = new Timer();
  private Timer _putTimer = new Timer();
  private Timer _isHolidayTimer = new Timer();
  

  public NonVersionedRedisHolidaySource(JedisPool jedisPool) {
    this(jedisPool, "");
  }
  
  public NonVersionedRedisHolidaySource(JedisPool jedisPool, String redisPrefix) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "NonVersionedRedisHolidaySource");
  }
  
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailRegistry, String namePrefix) {
    _getTimer = summaryRegistry.timer(namePrefix + ".get");
    _putTimer = summaryRegistry.timer(namePrefix + ".put");
    _isHolidayTimer = summaryRegistry.timer(namePrefix + ".isHoliday");
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  public JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  public String getRedisPrefix() {
    return _redisPrefix;
  }

  // ---------------------------------------------------------------------
  // REDIS KEY MANAGEMENT
  // ---------------------------------------------------------------------
  
  public String toRedisKey(UniqueId uniqueId) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("UNQ-");
    sb.append(uniqueId);
    String keyText = sb.toString();
    return keyText;
  }
  
  protected String toRedisKey(ObjectId objectId) {
    return toRedisKey(UniqueId.of(objectId, null));
  }
  
  public String toRedisKey(ExternalId externalId, HolidayType holidayType) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("EXT-");
    sb.append(externalId);
    sb.append("-");
    sb.append(holidayType.name());
    return sb.toString();
  }
  
  private String toRedisKey(Currency currency) {
    StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("CUR-");
    sb.append(currency.getCode());
    return sb.toString();
  }
  
  
  // ---------------------------------------------------------------------
  // DATA MANIPULATION
  // ---------------------------------------------------------------------
  
  /**
   * Add a fully manifested holiday.
   * Where the holiday has been loaded from a file or another source, this is
   * a bulk operation.
   * 
   * @param holiday The holiday to be added.
   */
  public void addHoliday(Holiday holiday) {
    ArgumentChecker.notNull(holiday, "holiday");
    
    UniqueId uniqueId = (holiday.getUniqueId() == null) ? generateUniqueId() : holiday.getUniqueId();
    if (holiday instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) holiday).setUniqueId(uniqueId);
    }
    try (Timer.Context context = _putTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      try {
        String uniqueRedisKey = toRedisKey(uniqueId);
        String daysKey = uniqueRedisKey + "-DAYS";
        jedis.del(uniqueRedisKey, daysKey);
        jedis.hset(uniqueRedisKey, TYPE, holiday.getType().toString());
        if (holiday.getCurrency() != null) {
          jedis.hset(uniqueRedisKey, CURRENCY, holiday.getCurrency().getCode());
          jedis.hset(toRedisKey(holiday.getCurrency()), UNIQUE_ID, uniqueId.toString());
        }
        if (holiday.getRegionExternalId() != null) {
          jedis.hset(uniqueRedisKey, REGION_SCHEME, holiday.getRegionExternalId().getScheme().getName());
          jedis.hset(uniqueRedisKey, REGION, holiday.getRegionExternalId().getValue());
          jedis.hset(toRedisKey(holiday.getRegionExternalId(), holiday.getType()), UNIQUE_ID, uniqueId.toString());
        }
        if (holiday.getExchangeExternalId() != null) {
          jedis.hset(uniqueRedisKey, EXCHANGE_SCHEME, holiday.getExchangeExternalId().getScheme().getName());
          jedis.hset(uniqueRedisKey, EXCHANGE, holiday.getExchangeExternalId().getValue());
          jedis.hset(toRedisKey(holiday.getExchangeExternalId(), holiday.getType()), UNIQUE_ID, uniqueId.toString());
        }
        if (holiday.getCustomExternalId() != null) {
          jedis.hset(uniqueRedisKey, CUSTOM_SCHEME, holiday.getCustomExternalId().getScheme().getName());
          jedis.hset(uniqueRedisKey, CUSTOM, holiday.getCustomExternalId().getValue());
          jedis.hset(toRedisKey(holiday.getCustomExternalId(), holiday.getType()), UNIQUE_ID, uniqueId.toString());
        }
        
        for (LocalDate holidayDate : holiday.getHolidayDates()) {
          jedis.zadd(daysKey, LocalDateToIntConverter.convertToInt(holidayDate), holidayDate.toString());
        }
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to add holiday " + holiday, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to add holiday " + holiday, e);
      }
    }
  }
  
  private UniqueId generateUniqueId() {
    return UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
  }

  // ---------------------------------------------------------------------
  // IMPLEMENTATION OF HOLIDAY SOURCE
  // ---------------------------------------------------------------------
  
  /**
   * @param days
   * @param simpleHoliday
   */
  private void convertDaysToLocalDates(Set<String> days, SimpleHoliday simpleHoliday) {
    for (String dayText : days) {
      LocalDate localDate = LocalDate.parse(dayText);
      simpleHoliday.addHolidayDate(localDate);
    }
  }

  @Override
  public Holiday get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    Holiday result = null;
    try (Timer.Context context = _getTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      try {
        result = loadFromRedis(jedis, uniqueId);
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to load holiday " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to load holiday " + uniqueId, e);
      }
    }
    return result;
  }
  
  protected Holiday loadFromRedis(Jedis jedis, UniqueId uniqueId) {
    String uniqueRedisKey = toRedisKey(uniqueId);
    String daysKey = uniqueRedisKey + "-DAYS";
    Map<String, String> hashValues = jedis.hgetAll(uniqueRedisKey);
    Set<String> days = jedis.zrange(daysKey, 0, -1);
    
    if ((hashValues != null) && !hashValues.isEmpty()) {
      SimpleHoliday simpleHoliday = new SimpleHoliday();
      
      simpleHoliday.setUniqueId(uniqueId);
      if (hashValues.containsKey(EXCHANGE_SCHEME)) {
        simpleHoliday.setExchangeExternalId(ExternalId.of(hashValues.get(EXCHANGE_SCHEME), hashValues.get(EXCHANGE)));
      }
      if (hashValues.containsKey(REGION_SCHEME)) {
        simpleHoliday.setRegionExternalId(ExternalId.of(hashValues.get(REGION_SCHEME), hashValues.get(REGION)));
      }
      if (hashValues.containsKey(CURRENCY)) {
        simpleHoliday.setCurrency(Currency.of(hashValues.get(CURRENCY)));
      }
      simpleHoliday.setType(HolidayType.valueOf(hashValues.get(TYPE)));
      convertDaysToLocalDates(days, simpleHoliday);
      
      return simpleHoliday;
    }
    return null;
  }

  @Override
  public Holiday get(ObjectId objectId, VersionCorrection versionCorrection) {
    UniqueId uniqueId = UniqueId.of(objectId, null);
    return get(uniqueId);
  }

  @Override
  public Map<UniqueId, Holiday> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, Holiday> result = new HashMap<UniqueId, Holiday>();
    
    for (UniqueId uniqueId : uniqueIds) {
      result.put(uniqueId, get(uniqueId));
    }
    
    return result;
  }

  @Override
  public Map<ObjectId, Holiday> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    Map<ObjectId, Holiday> result = new HashMap<ObjectId, Holiday>();
    
    for (ObjectId objectId : objectIds) {
      result.put(objectId, get(objectId, null));
    }
    
    return result;
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, Currency currency) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(currency, "currency");

    if (isWeekend(dateToCheck)) {  // this ignores the foundHoliday flag - not sure if that is correct or not
      return true;
    }

    boolean result = false;
    
    try (Timer.Context context = _isHolidayTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      try {
        
        String currencyIdKey = toRedisKey(currency);
        String uniqueId = jedis.hget(currencyIdKey, UNIQUE_ID);
        if (uniqueId != null) {
          String daysKey = toRedisKey(UniqueId.parse(uniqueId)) + "-DAYS";
          if (jedis.zscore(daysKey, dateToCheck.toString()) != null) {
            result = true;
          }
        }
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to check if holiday " + dateToCheck + " - " + currency, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to check if holiday " + dateToCheck + " - " + currency, e);
      }
    }
    
    return result;
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    // Any is the only supported type underneath, so we use the same logic.
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");

    if (isWeekend(dateToCheck)) {  // this ignores the foundHoliday flag - not sure if that is correct or not
      return true;
    }
    
    boolean foundHoliday = false;
    boolean result = false;
    
    try (Timer.Context context = _isHolidayTimer.time()) {
      Jedis jedis = getJedisPool().getResource();
      try {
        for (ExternalId externalId : regionOrExchangeIds) {
          String uniqueIdText = jedis.hget(toRedisKey(externalId, holidayType), UNIQUE_ID);
          if (uniqueIdText == null) {
            continue;
          }
          UniqueId uniqueId = UniqueId.parse(uniqueIdText);
          String uniqueIdKey = toRedisKey(uniqueId);
          Map<String, String> hash = jedis.hgetAll(uniqueIdKey);
          if (holidayType.name().equals(hash.get(TYPE))) {
            foundHoliday = true;
            String daysKey = uniqueIdKey + "-DAYS";
            if (jedis.zscore(daysKey, dateToCheck.toString()) != null) {
              result = true;
            }
            break;
          }
        }
        
        getJedisPool().returnResource(jedis);
      } catch (Exception e) {
        s_logger.error("Unable to check if holiday " + dateToCheck + " - " + holidayType + " - " + regionOrExchangeIds, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to check if holiday " + dateToCheck + " - " + holidayType + " - " + regionOrExchangeIds, e);
      }
    }
    
    // NOTE kirk 2013-06-05 -- The whole use of foundHoliday is basically to make it easy
    // to set a breakpoint inside the block below so that you can tell the difference in a debugger
    // between the two cases: one where you've actually found the holiday entry and you know
    // definitively whether it's a holiday, and one where you haven't so you really don't know.
    if (foundHoliday) {
      return result;
    }
    
    return false;
  }

  @Override
  public Collection<Holiday> get(HolidayType holidayType,
                                 ExternalIdBundle regionOrExchangeIds) {
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");

    Jedis jedis = getJedisPool().getResource();
    try {
      for (ExternalId externalId : regionOrExchangeIds) {
        String uniqueIdText = jedis.hget(toRedisKey(externalId, holidayType), UNIQUE_ID);
        if (uniqueIdText == null) {
          continue;
        }
        UniqueId uniqueId = UniqueId.parse(uniqueIdText);
        String uniqueIdKey = toRedisKey(uniqueId);
        Map<String, String> hash = jedis.hgetAll(uniqueIdKey);
        if (holidayType.name().equals(hash.get(TYPE))) {
          String daysKey = uniqueIdKey + "-DAYS";
          Set<String> dates = jedis.zrange(daysKey, 0, -1);

          SimpleHoliday holiday = new SimpleHoliday();
          holiday.setUniqueId(uniqueId);
          holiday.setType(holidayType);
          holiday.setExchangeExternalId(regionOrExchangeIds.getExternalId(ExternalScheme.of(EXCHANGE_SCHEME)));
          holiday.setRegionExternalId(regionOrExchangeIds.getExternalId(ExternalScheme.of(REGION_SCHEME)));
          holiday.setHolidayDates(parseHolidayDates(dates));
          return ImmutableList.<Holiday>of(holiday);
        }
      }

      return ImmutableList.of();
    } catch (JedisConnectionException e) {
      s_logger.error("Unable to get holiday - " + holidayType + " - " + regionOrExchangeIds, e);
      getJedisPool().returnBrokenResource(jedis);
      // Prevent returning the resource twice when the finally block runs
      jedis = null;
      throw new OpenGammaRuntimeException("Unable to get holiday - " + holidayType + " - " + regionOrExchangeIds, e);
    } finally {
      if (jedis != null) {
        getJedisPool().returnResource(jedis);
      }
    }
  }

  @Override
  public Collection<Holiday> get(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");

    Jedis jedis = getJedisPool().getResource();
    try {

      String currencyIdKey = toRedisKey(currency);
      String uniqueIdText = jedis.hget(currencyIdKey, UNIQUE_ID);
      if (uniqueIdText != null) {
        UniqueId uniqueId = UniqueId.parse(uniqueIdText);
        String daysKey = toRedisKey(uniqueId) + "-DAYS";
        Set<String> dates = jedis.zrange(daysKey, 0, -1);

        SimpleHoliday holiday = new SimpleHoliday();
        holiday.setUniqueId(uniqueId);
        holiday.setType(HolidayType.CURRENCY);
        holiday.setCurrency(currency);
        holiday.setHolidayDates(parseHolidayDates(dates));
        return ImmutableList.<Holiday>of(holiday);
      } else {
        return ImmutableList.of();
      }
    } catch (JedisConnectionException e) {
      s_logger.error("Unable to get holiday - " + currency, e);
      getJedisPool().returnBrokenResource(jedis);
      // Prevent returning the resource twice when the finally block runs
      jedis = null;
      throw new OpenGammaRuntimeException("Unable to get holiday - " + currency, e);
    } finally {
      if (jedis != null) {
        getJedisPool().returnResource(jedis);
      }
    }
  }

  private ImmutableList<LocalDate> parseHolidayDates(Set<String> dates) {
    ImmutableList.Builder<LocalDate> builder = ImmutableList.builder();
    for (String date : dates) {
      builder.add(LocalDate.parse(date));
    }
    return builder.build();
  }

  @Override
  public boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId) {
    return isHoliday(dateToCheck, holidayType, ExternalIdBundle.of(regionOrExchangeId));
  }

  /**
   * Checks if the date is at the weekend, defined as a Saturday or Sunday.
   *
   * @param date  the date to check, not null
   * @return true if it is a weekend
   */
  protected boolean isWeekend(LocalDate date) {
    return (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
  }
  
}
