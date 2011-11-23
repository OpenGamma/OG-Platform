/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertNotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.time.calendar.Clock;
import javax.time.calendar.DateProvider;
import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.AssetOrNothingPayoffStyle;
import com.opengamma.financial.security.option.AsymmetricPoweredPayoffStyle;
import com.opengamma.financial.security.option.BarrierPayoffStyle;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.CappedPoweredPayoffStyle;
import com.opengamma.financial.security.option.CashOrNothingPayoffStyle;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExtremeSpreadPayoffStyle;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FadeInPayoffStyle;
import com.opengamma.financial.security.option.FixedStrikeLookbackPayoffStyle;
import com.opengamma.financial.security.option.FloatingStrikeLookbackPayoffStyle;
import com.opengamma.financial.security.option.GapPayoffStyle;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.PayoffStyle;
import com.opengamma.financial.security.option.PoweredPayoffStyle;
import com.opengamma.financial.security.option.SimpleChooserPayoffStyle;
import com.opengamma.financial.security.option.SupersharePayoffStyle;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.option.VanillaPayoffStyle;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.tuple.Pair;

/**
 * Creates random securities.
 */
@SuppressWarnings("unchecked")
public abstract class SecurityTestCase implements SecurityTestCaseMethods {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityTestCase.class);

  private static interface TestDataProvider<T> {
    void getValues(Collection<T> values);
  }

  private static class DefaultObjectPermute<T> implements TestDataProvider<T> {
    private final Class<T> _clazz;

    private DefaultObjectPermute(final Class<T> clazz) {
      _clazz = clazz;
    }

    public static <T> DefaultObjectPermute<T> of(final Class<T> clazz) {
      return new DefaultObjectPermute<T>(clazz);
    }

    @Override
    public void getValues(final Collection<T> values) {
      values.addAll(permuteTestObjects(_clazz));
    }
  }

  private static class DefaultCollection<T, C extends Collection<T>> implements TestDataProvider<C> {
    private final Class<C> _collection;
    private final Class<T> _clazz;

    private DefaultCollection(final Class<C> collection, final Class<T> clazz) {
      _collection = collection;
      _clazz = clazz;
    }

    public static <T, C extends Collection<T>> DefaultCollection<T, C> of(final Class<C> collection, final Class<T> clazz) {
      return new DefaultCollection<T, C>(collection, clazz);
    }

    @Override
    public void getValues(final Collection<C> values) {
      try {
        final C collection = _collection.newInstance();
        collection.addAll(permuteTestObjects(_clazz));
        if (collection.size() > 0) {
          values.add(_collection.newInstance());
          if (collection.size() > 1) {
            final C value = _collection.newInstance();
            value.add(collection.iterator().next());
            values.add(value);
          }
        }
        values.add(collection);
      } catch (final InstantiationException ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      } catch (final IllegalAccessException ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
    }
  }

  private static class DefaultList<T, C extends List<T>> implements TestDataProvider<C> {
    private final Class<C> _collection;
    private final Class<T> _clazz;

    private DefaultList(final Class<C> collection, final Class<T> clazz) {
      _collection = collection;
      _clazz = clazz;
    }

    public static <T, C extends List<T>> DefaultList<T, C> of(final Class<C> collection, final Class<T> clazz) {
      return new DefaultList<T, C>(collection, clazz);
    }

    @Override
    public void getValues(final Collection<C> values) {
      try {
        final C collection = _collection.newInstance();
        collection.addAll(permuteTestObjects(_clazz));
        if (collection.size() > 0) {
          values.add(_collection.newInstance());
          if (collection.size() > 1) {
            final C value = _collection.newInstance();
            value.add(collection.iterator().next());
            values.add(value);
          }
        }
        values.add(collection);
      } catch (final InstantiationException ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      } catch (final IllegalAccessException ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
    }
  }

  private static Map<Object, TestDataProvider<?>> s_dataProviders = new HashMap<Object, TestDataProvider<?>>();
  private static Random s_random = new Random();

  private static RegionSource s_regionSource;
  static {
    final RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.createPopulated(regionMaster);
    s_regionSource = new MasterRegionSource(regionMaster);
  }

  protected static RegionSource getRegionSource() {
    return s_regionSource;
  }

  static {
    final long seed = s_random.nextLong();
    s_logger.info("Random seed = {}", seed);
    s_random.setSeed(seed);
    TestDataProvider<?> provider;
    s_dataProviders.put(String.class, new TestDataProvider<String>() {
      @Override
      public void getValues(final Collection<String> values) {
        values.add("");
        values.add(RandomStringUtils.randomAlphabetic(16));
        values.add(RandomStringUtils.randomNumeric(16));
        values.add(RandomStringUtils.randomAlphanumeric(16));
      }
    });
    s_dataProviders.put(Map.class, new TestDataProvider<Map>() {
      private Map generateRandomMap(int count){
        Map<String, String> map = new HashMap<String, String>(count);
        while(count>0){
          map.put(RandomStringUtils.randomAlphanumeric(16), RandomStringUtils.randomAlphanumeric(16));
          count--;
        }
        return map;
      }
      @Override
      public void getValues(final Collection<Map> values) {
        Random random = new Random();
        double qty = 1 + random.nextInt(9);
        while(qty>0){
          values.add(generateRandomMap(1 + random.nextInt(9)));
          qty--;
        }
        values.add(new HashMap());
      }
    });
    s_dataProviders.put(Double.class, provider = new TestDataProvider<Double>() {
      @Override
      public void getValues(final Collection<Double> values) {
        values.add(0.0);
        double d;
        do {
          d = s_random.nextDouble();
        } while (d == 0);
        values.add(d * 100.0);
        values.add(d * -100.0);
      }
    });
    s_dataProviders.put(Double.TYPE, provider);
    s_dataProviders.put(UniqueId.class, new TestDataProvider<UniqueId>() {
      @Override
      public void getValues(final Collection<UniqueId> values) {
        values.add(UniqueId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)));
      }
    });
    s_dataProviders.put(ExternalId.class, new TestDataProvider<ExternalId>() {
      @Override
      public void getValues(final Collection<ExternalId> values) {
        values.add(ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)));
      }
    });
    s_dataProviders.put(ExternalIdBundle.class, new TestDataProvider<ExternalIdBundle>() {
      @Override
      public void getValues(final Collection<ExternalIdBundle> values) {
        values.add(ExternalIdBundle.EMPTY);
        values.add(ExternalIdBundle.of(ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16))));
        values.add(ExternalIdBundle.of(ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)), ExternalId.of(RandomStringUtils.randomAlphanumeric(8),
            RandomStringUtils.randomAlphanumeric(16))));
      }
    });
    s_dataProviders.put(Currency.class, new TestDataProvider<Currency>() {
      @Override
      public void getValues(final Collection<Currency> values) {
        values.add(Currency.of(RandomStringUtils.randomAlphabetic(3).toUpperCase(Locale.ENGLISH)));
      }
    });
    s_dataProviders.put(YieldConvention.class, new TestDataProvider<YieldConvention>() {
      @Override
      public void getValues(final Collection<YieldConvention> values) {
        values.add(SimpleYieldConvention.US_STREET);
        values.add(SimpleYieldConvention.US_TREASURY_EQUIVALANT);
        values.add(SimpleYieldConvention.TRUE);
      }
    });
    s_dataProviders.put(Expiry.class, DefaultObjectPermute.of(Expiry.class));
    s_dataProviders.put(ZonedDateTime.class, new TestDataProvider<ZonedDateTime>() {
      private final TimeZone[] _timezones = new TimeZone[] {TimeZone.UTC, TimeZone.of("UTC-01:00"), TimeZone.of("UTC+01:00")};

      @Override
      public void getValues(final Collection<ZonedDateTime> values) {
        for (final TimeZone timezone : _timezones) {
          values.add(ZonedDateTime.now(Clock.system(timezone)).withNanoOfSecond(0));
          // TODO: random date in the past
          // TODO: random date in the future
        }
      }
    });
    s_dataProviders.put(DateProvider.class, new TestDataProvider<DateProvider>() {
      @Override
      public void getValues(final Collection<DateProvider> values) {
        values.add(LocalDate.now());
        // TODO: random date in the past
        // TODO: random date in the future
      }
    });
    s_dataProviders.put(TimeProvider.class, new TestDataProvider<TimeProvider>() {
      @Override
      public void getValues(final Collection<TimeProvider> values) {
        values.add(LocalTime.now().withNanoOfSecond(0));
        // TODO: random time in the past
        // TODO: random time in the future
      }
    });
    s_dataProviders.put(DateTimeProvider.class, new TestDataProvider<DateTimeProvider>() {
      @Override
      public void getValues(final Collection<DateTimeProvider> values) {
        final Collection<DateProvider> dates = getTestObjects(DateProvider.class, null);
        final Collection<TimeProvider> times = getTestObjects(TimeProvider.class, null);
        for (final DateProvider date : dates) {
          for (final TimeProvider time : times) {
            values.add(LocalDateTime.of(date, time));
          }
        }
      }
    });
    s_dataProviders.put(Frequency.class, new TestDataProvider<Frequency>() {
      @Override
      public void getValues(final Collection<Frequency> values) {
        values.add(SimpleFrequency.ANNUAL);
        values.add(SimpleFrequency.SEMI_ANNUAL);
        values.add(SimpleFrequency.CONTINUOUS);
      }
    });
    s_dataProviders.put(DayCount.class, new TestDataProvider<DayCount>() {
      @Override
      public void getValues(final Collection<DayCount> values) {
        values.add(DayCountFactory.INSTANCE.getDayCount("Act/Act"));
        values.add(DayCountFactory.INSTANCE.getDayCount("1/1"));
        values.add(DayCountFactory.INSTANCE.getDayCount("Bond Basis"));
      }
    });
    s_dataProviders.put(BusinessDayConvention.class, new TestDataProvider<BusinessDayConvention>() {
      @Override
      public void getValues(final Collection<BusinessDayConvention> values) {
        values.add(BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"));
        values.add(BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"));
        values.add(BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding"));
      }
    });
    s_dataProviders.put(GICSCode.class, new TestDataProvider<GICSCode>() {
      @Override
      public void getValues(final Collection<GICSCode> values) {
        int code = (((s_random.nextInt(90) + 10) * 100 + (s_random.nextInt(90) + 10)) * 100 + (s_random.nextInt(90) + 10)) * 100 + (s_random.nextInt(90) + 10);
        values.add(GICSCode.of(Integer.toString(code)));
      }
    });
    s_dataProviders.put(Pair.of(BondFutureSecurity.class, Collection.class), DefaultCollection.of(ArrayList.class, BondFutureDeliverable.class));
    s_dataProviders.put(Pair.of(BondFutureSecurity.class, List.class), DefaultList.of(ArrayList.class, BondFutureDeliverable.class));
    s_dataProviders.put(ExerciseType.class, new TestDataProvider<ExerciseType>() {
      @Override
      public void getValues(final Collection<ExerciseType> values) {
        values.add(new AmericanExerciseType());
        values.add(new AsianExerciseType());
        values.add(new BermudanExerciseType());
        values.add(new EuropeanExerciseType());
      }
    });
    s_dataProviders.put(PayoffStyle.class, new TestDataProvider<PayoffStyle>() {
      @Override
      public void getValues(final Collection<PayoffStyle> values) {
        values.add(new AssetOrNothingPayoffStyle());
        values.add(new AsymmetricPoweredPayoffStyle(s_random.nextDouble()));
        values.add(new BarrierPayoffStyle());
        values.add(new CappedPoweredPayoffStyle(s_random.nextDouble(), s_random.nextDouble()));
        values.add(new CashOrNothingPayoffStyle(s_random.nextDouble()));
        values.add(new FadeInPayoffStyle(s_random.nextDouble(), s_random.nextDouble()));
        values.add(new FixedStrikeLookbackPayoffStyle());
        values.add(new FloatingStrikeLookbackPayoffStyle());
        values.add(new GapPayoffStyle(s_random.nextDouble()));
        values.add(new PoweredPayoffStyle(s_random.nextDouble()));
        values.add(new SupersharePayoffStyle(s_random.nextDouble(), s_random.nextDouble()));
        values.add(new VanillaPayoffStyle());
        values.add(new ExtremeSpreadPayoffStyle(ZonedDateTime.now().withNanoOfSecond(0), s_random.nextBoolean()));
        values.add(new SimpleChooserPayoffStyle(ZonedDateTime.now().withNanoOfSecond(0), s_random.nextDouble(),
            new Expiry(ZonedDateTime.now(Clock.systemDefaultZone()), ExpiryAccuracy.MONTH_YEAR)));
      }
    });
    s_dataProviders.put(Boolean.class, provider = new TestDataProvider<Boolean>() {
      @Override
      public void getValues(final Collection<Boolean> values) {
        values.add(true);
        values.add(false);
      }
    });
    s_dataProviders.put(Boolean.TYPE, provider);
    s_dataProviders.put(SwapLeg.class, new TestDataProvider<SwapLeg>() {
      @Override
      public void getValues(final Collection<SwapLeg> values) {
        values.addAll(permuteTestObjects(FloatingSpreadIRLeg.class));
        values.addAll(permuteTestObjects(FloatingGearingIRLeg.class));
        values.addAll(permuteTestObjects(FixedInterestRateLeg.class));
        values.addAll(permuteTestObjects(FloatingInterestRateLeg.class));
      }
    });
    s_dataProviders.put(Region.class, new TestDataProvider<Region>() {
      @Override
      public void getValues(final Collection<Region> values) {
        values.add(getRegionSource().getHighestLevelRegion(RegionUtils.countryRegionId(Country.US)));
        values.add(getRegionSource().getHighestLevelRegion(RegionUtils.countryRegionId(Country.GB)));
      }
    });
    s_dataProviders.put(Notional.class, new TestDataProvider<Notional>() {
      @Override
      public void getValues(final Collection<Notional> values) {
        values.add(new CommodityNotional());
        values.addAll(permuteTestObjects(InterestRateNotional.class));
        values.addAll(permuteTestObjects(SecurityNotional.class));
      }
    });
    s_dataProviders.put(byte[].class, new TestDataProvider<byte[]>() {
      @Override
      public void getValues(Collection<byte[]> values) {
        
        values.add(getRandomBytes());
      }

      private byte[] getRandomBytes() {
        byte[] randomBytes = new byte[s_random.nextInt(100) + 10];
        s_random.nextBytes(randomBytes);
        return randomBytes;
      }
    });
  }

  protected static <T> List<T> getTestObjects(final Class<T> clazz, final Class<?> parent) {
    final List<T> objects = new ArrayList<T>();
    if (clazz.isEnum()) {
      for (final T value : clazz.getEnumConstants()) {
        objects.add(value);
      }
    } else {
      final Object key;
      if (Collection.class.equals(clazz)) {
        key = Pair.of(parent, clazz);
      } else if (List.class.equals(clazz)) {
        key = Pair.of(parent, clazz);
      } else {
        key = clazz;
      }
      final TestDataProvider<T> provider = (TestDataProvider<T>) s_dataProviders.get(key);
      if (provider == null) {
        throw new IllegalArgumentException("No random provider for " + clazz);
      }
      provider.getValues(objects);
    }
    Collections.shuffle(objects);
    return objects;
  }

  private static <T> Constructor<T> getBiggestConstructor(final Class<T> clazz) {
    final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
    int max = -1, bestIndex = -1;
    for (int i = 0; i < constructors.length; i++) {
      final Class<?>[] parameters = constructors[i].getParameterTypes();
      if (parameters.length > max) {
        max = parameters.length;
        bestIndex = i;
      }
    }
    return constructors[bestIndex];
  }

  private static <T> Collection<T> permuteTestObjects(final Class<T> clazz, final Constructor<T> constructor) {
    final Collection<T> objects = new LinkedList<T>();
    final Class<?>[] parameters = constructor.getParameterTypes();
    final List<?>[] parameterValues = new List<?>[parameters.length];
    final int[] parameterIndex = new int[parameters.length];
    int longest = 0;
    for (int i = 0; i < parameters.length; i++) {
      // System.out.println(clazz + ", " + i + ", " + parameters[i]);
      parameterValues[i] = getTestObjects(parameters[i], clazz);
      if (parameterValues[i].size() > longest) {
        longest = parameterValues[i].size();
      }
    }
    final Object[] construct = new Object[parameters.length];
    final List<Throwable> exceptions = new LinkedList<Throwable>();
    // TODO: what about nulls ?
    for (int i = 0; i < longest; i++) {
      for (int j = 0; j < parameters.length; j++) {
        construct[j] = parameterValues[j].get(parameterIndex[j]);
        parameterIndex[j] = (parameterIndex[j] + 1) % parameterValues[j].size();
      }
      try {
        objects.add(constructor.newInstance(construct));
      } catch (final Throwable t) {
        exceptions.add(t);
      }
    }
    if (objects.size() == 0) {
      for (final Throwable t : exceptions) {
        t.printStackTrace();
      }
      throw new IllegalArgumentException("couldn't create test objects");
    }
    s_logger.info("{} objects created for {}", objects.size(), clazz);
    for (final Object o : objects) {
      s_logger.debug("{}", o);
    }
    return objects;
  }

  @SuppressWarnings("rawtypes")
  private static <T> Collection<T> permuteTestObjects(final Class<T> clazz) {
    if (ManageableSecurity.class.isAssignableFrom(clazz)) {
      return permuteTestSecurities((Class) clazz);
    }
    return permuteTestObjects(clazz, getBiggestConstructor(clazz));
  }

  private static <T extends ManageableSecurity> Collection<T> permuteTestSecurities(final Class<T> clazz) {
    intializeClass(clazz);
    MetaBean mb = JodaBeanUtils.metaBean(clazz);
    List<MetaProperty<Object>> mps = new ArrayList<MetaProperty<Object>>(mb.metaPropertyMap().values());
    
    // find the longest set of available data
    final List<?>[] parameterValues = new List<?>[mps.size()];
    int longest = 0;
    for (int i = 0; i < mps.size(); i++) {
      // System.out.println(clazz + ", " + i + ", " + parameters[i]);
      parameterValues[i] = getTestObjects(mps.get(i).propertyType(), clazz);
      if (parameterValues[i].size() > longest) {
        longest = parameterValues[i].size();
      }
    }
    
    // prepare
    final List<Throwable> exceptions = new ArrayList<Throwable>();
    final Collection<T> objects = new ArrayList<T>();
    final int[] parameterIndex = new int[mps.size()];
    for (int i = 0; i < longest; i++) {
      try {
        BeanBuilder<?> builder = mb.builder();
        for (int j = 0; j < mps.size(); j++) {
          Object value = parameterValues[j].get(parameterIndex[j]);
          parameterIndex[j] = (parameterIndex[j] + 1) % parameterValues[j].size();
          builder.set(mps.get(j).name(), value);
        }
        objects.add((T) builder.build());
      } catch (final Throwable t) {
        exceptions.add(t);
      }
    }
    if (objects.size() == 0) {
      for (final Throwable t : exceptions) {
        t.printStackTrace();
      }
      throw new IllegalArgumentException("couldn't create test objects");
    }
    s_logger.info("{} objects created for {}", objects.size(), clazz);
    for (final Object o : objects) {
      s_logger.debug("{}", o);
    }
    return objects;
  }

  private static <T> void intializeClass(final Class<T> clazz) {
    // call the default constructor to initialize the class
    try {
      Constructor<T> defaultConstructor = getDefaultConstructor(clazz);
      if (defaultConstructor != null) {
        defaultConstructor.setAccessible(true);
        defaultConstructor.newInstance();
      }
    } catch (Exception ex) {
    }
  }

  private static <T> Constructor<T> getDefaultConstructor(final Class<T> clazz) {
    Constructor<T> defaultConstructor = null;
    Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
    for (Constructor<?> constructor : declaredConstructors) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length == 0) {
        defaultConstructor = (Constructor<T>) constructor;
        break;
      }
    }
    return defaultConstructor;
  }

  protected abstract <T extends ManageableSecurity> void assertSecurity(final Class<T> securityClass, final T security);

  protected <T extends ManageableSecurity> void assertSecurities(final Class<T> securityClass, final Collection<T> securities) {
    String securityType = null;
    Class<?> c = securityClass;
    while (c != null) {
      try {
        securityType = (String) c.getDeclaredField("SECURITY_TYPE").get(null);
      } catch (final Throwable t) {
        // Ignore
      }
      c = c.getSuperclass();
    }
    if (securityClass != RawSecurity.class) {
      assertNotNull(securityType);
    }
    for (final T security : securities) {
      // Force the security type to be a valid string; they're random nonsense otherwise
      if (securityClass != RawSecurity.class) {
        security.setSecurityType(securityType);
      }
      assertSecurity(securityClass, security);
    }
  }

  protected <T extends ManageableSecurity> void assertSecurities(final Class<T> securityClass) {
    if (isInitialized()) {
      assertSecurities(securityClass, permuteTestSecurities(securityClass));
    }
  }

  /**
   * Allow subclasses to block testing.
   * 
   * @return true if initialized
   */
  protected boolean isInitialized() {
    return true;
  }

  // SecurityMasterTestCaseMethods

  @Override
  @Test
  public void testAgricultureFutureSecurity() {
    assertSecurities(AgricultureFutureSecurity.class);
  }

  @Override
  @Test
  public void testBondFutureSecurity() {
    assertSecurities(BondFutureSecurity.class);
  }

  @Override
  @Test
  public void testCashSecurity() {
    assertSecurities(CashSecurity.class);
  }

  @Override
  @Test
  public void testCorporateBondSecurity() {
    assertSecurities(CorporateBondSecurity.class);
  }

  @Override
  @Test
  public void testEnergyFutureSecurity() {
    assertSecurities(EnergyFutureSecurity.class);
  }

  @Override
  @Test
  public void testEquityOptionSecurity() {
    assertSecurities(EquityOptionSecurity.class);
  }
  
  @Override
  @Test
  public void testEquityBarrierOptionSecurity() {
    assertSecurities(EquityBarrierOptionSecurity.class);
  }

  @Override
  @Test
  public void testEquitySecurity() {
    assertSecurities(EquitySecurity.class);
  }

  @Override
  @Test
  public void testFRASecurity() {
    assertSecurities(FRASecurity.class);
  }

  @Override
  @Test
  public void testFXFutureSecurity() {
    assertSecurities(FXFutureSecurity.class);
  }

  @Override
  @Test
  public void testFXOptionSecurity() {
    assertSecurities(FXOptionSecurity.class);
  }
  
  @Override
  @Test
  public void testNonDeliverableFXOptionSecurity() {
    assertSecurities(NonDeliverableFXOptionSecurity.class);
  }
  
  @Override
  @Test
  public void testFXBarrierOptionSecurity() {
    assertSecurities(FXBarrierOptionSecurity.class);
  }

  @Override
  @Test
  public void testForwardSwapSecurity() {
    assertSecurities(ForwardSwapSecurity.class);
  }

  @Override
  @Test
  public void testIRFutureOptionSecurity() {
    assertSecurities(IRFutureOptionSecurity.class);
  }

  @Override
  @Test
  public void testGovernmentBondSecurity() {
    assertSecurities(GovernmentBondSecurity.class);
  }

  @Override
  @Test
  public void testIndexFutureSecurity() {
    assertSecurities(IndexFutureSecurity.class);
  }

  @Override
  @Test
  public void testInterestRateFutureSecurity() {
    assertSecurities(InterestRateFutureSecurity.class);
  }

  @Override
  @Test
  public void testMetalFutureSecurity() {
    assertSecurities(MetalFutureSecurity.class);
  }

  @Override
  @Test
  public void testMunicipalBondSecurity() {
    assertSecurities(MunicipalBondSecurity.class);
  }

  @Override
  @Test
  public void testStockFutureSecurity() {
    assertSecurities(StockFutureSecurity.class);
  }

  @Override
  @Test
  public void testSwaptionSecurity() {
    assertSecurities(SwaptionSecurity.class);
  }

  @Override
  @Test
  public void testSwapSecurity() {
    assertSecurities(SwapSecurity.class);
  }

  @Override
  @Test
  public void testEquityIndexOptionSecurity() {
    assertSecurities(EquityIndexOptionSecurity.class);
  }
  @Override
  @Test
  public void testFXSecurity() {
    assertSecurities(FXSecurity.class);
  }

  @Override
  @Test
  public void testFXForwardSecurity() {
    assertSecurities(FXForwardSecurity.class);
  }

  @Override
  @Test
  public void testCapFloorSecurity() {
    assertSecurities(CapFloorSecurity.class);
  }

  @Override
  @Test
  public void testCapFloorCMSSpreadSecurity() {
    assertSecurities(CapFloorCMSSpreadSecurity.class);
  }
  
  @Override
  @Test
  public void testRawSecurity() {
    assertSecurities(RawSecurity.class);
  }

  @Override
  @Test
  public void testEquityVarianceSwapSecurity() {
    assertSecurities(EquityVarianceSwapSecurity.class);
  }
  
}
