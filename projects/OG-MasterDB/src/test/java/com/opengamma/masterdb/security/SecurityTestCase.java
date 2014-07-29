/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertNotNull;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.daycount.DayCounts;
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
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
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
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.index.BondIndex;
import com.opengamma.financial.security.index.BondIndexComponent;
import com.opengamma.financial.security.index.EquityIndex;
import com.opengamma.financial.security.index.EquityIndexComponent;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.IndexFamily;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.AssetOrNothingPayoffStyle;
import com.opengamma.financial.security.option.AsymmetricPoweredPayoffStyle;
import com.opengamma.financial.security.option.BarrierPayoffStyle;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.CappedPoweredPayoffStyle;
import com.opengamma.financial.security.option.CashOrNothingPayoffStyle;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExtremeSpreadPayoffStyle;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
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
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.test.AbstractSecurityTestCaseAdapter;
import com.opengamma.financial.security.test.SecurityTestCaseMethods;
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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pairs;

/**
 * Creates random securities.
 */
@SuppressWarnings("unchecked")
@Test(groups = TestGroup.UNIT)
public abstract class SecurityTestCase extends AbstractSecurityTestCaseAdapter implements SecurityTestCaseMethods {

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
    s_dataProviders.put(Map.class, new TestDataProvider<Map<?, ?>>() {
      private Map<?, ?> generateRandomMap(int count) {
        Map<String, String> map = new HashMap<String, String>(count);
        while (count > 0) {
          map.put(RandomStringUtils.randomAlphanumeric(16), RandomStringUtils.randomAlphanumeric(16));
          count--;
        }
        return map;
      }

      @Override
      public void getValues(final Collection<Map<?, ?>> values) {
        double qty = 1 + s_random.nextInt(9);
        while (qty > 0) {
          values.add(generateRandomMap(1 + s_random.nextInt(9)));
          qty--;
        }
        values.add(new HashMap<Object, Object>());
      }
    });
    s_dataProviders.put(SortedMap.class, new TestDataProvider<SortedMap<Tenor, ExternalId>>() {
      private SortedMap<Tenor, ExternalId> generateRandomMap(int count) {
        SortedMap<Tenor, ExternalId> map = new TreeMap<Tenor, ExternalId>();
        while (count > 0) {
          Tenor tenor;
          switch (s_random.nextInt(3)) {
            case 0:
              tenor = Tenor.ofDays(s_random.nextInt(28) + 1);
              break;
            case 1:
              tenor = Tenor.ofMonths(s_random.nextInt(12) + 1);
              break;
            case 2:
              tenor = Tenor.ofYears(s_random.nextInt(20) + 2000);
              break;
            default:
              throw new OpenGammaRuntimeException("Should never happen");
          }
          map.put(tenor, ExternalId.of(RandomStringUtils.randomAlphanumeric(16), RandomStringUtils.randomAlphanumeric(16)));
          count--;
        }
        return map;
      }

      @Override
      public void getValues(final Collection<SortedMap<Tenor, ExternalId>> values) {
        double qty = 1 + s_random.nextInt(9);
        while (qty > 0) {
          values.add(generateRandomMap(1 + s_random.nextInt(9)));
          qty--;
        }
        values.add(new TreeMap<Tenor, ExternalId>());
      }
    });
    s_dataProviders.put(Set.class, new TestDataProvider<Set<String>>() {

      @Override
      public void getValues(Collection<Set<String>> values) {
        values.add(Sets.newHashSet(getRandomPermissions()));
        values.add(Sets.newHashSet(getRandomPermissions()));
        values.add(Sets.newHashSet(getRandomPermissions()));
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
        values.add(ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)));
        values.add(ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)));
      }
    });
    s_dataProviders.put(ExternalIdBundle.class, new TestDataProvider<ExternalIdBundle>() {
      @Override
      public void getValues(final Collection<ExternalIdBundle> values) {
        values.add(
            ExternalIdBundle.of(
                ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16))));
        values.add(
            ExternalIdBundle.of(
                ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)),
                ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16))));
        values.add(
            ExternalIdBundle.of(
                ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)),
                ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16)),
                ExternalId.of(RandomStringUtils.randomAlphanumeric(8), RandomStringUtils.randomAlphanumeric(16))));
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
        values.add(SimpleYieldConvention.US_TREASURY_EQUIVALENT);
        values.add(SimpleYieldConvention.TRUE);
      }
    });
    s_dataProviders.put(Expiry.class, DefaultObjectPermute.of(Expiry.class));
    s_dataProviders.put(ZonedDateTime.class, new TestDataProvider<ZonedDateTime>() {
      private final ZoneId[] _timezones = new ZoneId[] {ZoneOffset.UTC, ZoneId.of("UTC-01:00"), ZoneId.of("UTC+01:00") };

      @Override
      public void getValues(final Collection<ZonedDateTime> values) {
        for (final ZoneId timezone : _timezones) {
          values.add(ZonedDateTime.now(Clock.system(timezone)).withNano(0));
          // TODO: random date in the past
          // TODO: random date in the future
        }
      }
    });
    s_dataProviders.put(LocalDate.class, new TestDataProvider<LocalDate>() {
      @Override
      public void getValues(final Collection<LocalDate> values) {
        values.add(LocalDate.now());
        // TODO: random date in the past
        // TODO: random date in the future
      }
    });
    s_dataProviders.put(LocalTime.class, new TestDataProvider<LocalTime>() {
      @Override
      public void getValues(final Collection<LocalTime> values) {
        values.add(LocalTime.now().withNano(0));
        // TODO: random time in the past
        // TODO: random time in the future
      }
    });
    s_dataProviders.put(LocalDateTime.class, new TestDataProvider<LocalDateTime>() {
      @Override
      public void getValues(final Collection<LocalDateTime> values) {
        final Collection<LocalDate> dates = getTestObjects(LocalDate.class, null);
        final Collection<LocalTime> times = getTestObjects(LocalTime.class, null);
        for (final LocalDate date : dates) {
          for (final LocalTime time : times) {
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
    s_dataProviders.put(Tenor.class, new TestDataProvider<Tenor>() {
      @Override
      public void getValues(final Collection<Tenor> values) {
        values.add(Tenor.ONE_DAY);
        values.add(Tenor.ONE_WEEK);
        values.add(Tenor.ONE_MONTH);
        values.add(Tenor.ONE_YEAR);
      }
    });
    s_dataProviders.put(DayCount.class, new TestDataProvider<DayCount>() {
      @Override
      public void getValues(final Collection<DayCount> values) {
        values.add(DayCounts.ACT_ACT_ISDA);
        values.add(DayCountFactory.of("1/1"));
        values.add(DayCountFactory.of("Bond Basis"));
      }
    });
    s_dataProviders.put(BusinessDayConvention.class, new TestDataProvider<BusinessDayConvention>() {
      @Override
      public void getValues(final Collection<BusinessDayConvention> values) {
        values.add(BusinessDayConventions.FOLLOWING);
        values.add(BusinessDayConventions.MODIFIED_FOLLOWING);
        values.add(BusinessDayConventions.PRECEDING);
      }
    });
    s_dataProviders.put(GICSCode.class, new TestDataProvider<GICSCode>() {
      @Override
      public void getValues(final Collection<GICSCode> values) {
        int code = (((s_random.nextInt(90) + 10) * 100 + (s_random.nextInt(90) + 10)) * 100 + (s_random.nextInt(90) + 10)) * 100 + (s_random.nextInt(90) + 10);
        values.add(GICSCode.of(Integer.toString(code)));
      }
    });
    s_dataProviders.put(Pairs.of(BondFutureSecurity.class, Collection.class), DefaultCollection.of(ArrayList.class, BondFutureDeliverable.class));
    s_dataProviders.put(Pairs.of(BondFutureSecurity.class, List.class), DefaultList.of(ArrayList.class, BondFutureDeliverable.class));
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
        values.add(new ExtremeSpreadPayoffStyle(ZonedDateTime.now().withNano(0), s_random.nextBoolean()));
        values.add(new SimpleChooserPayoffStyle(ZonedDateTime.now().withNano(0), s_random.nextDouble(),
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
        values.addAll(permuteTestObjects(FixedVarianceSwapLeg.class));
        values.addAll(permuteTestObjects(FloatingVarianceSwapLeg.class));
      }
    });
    s_dataProviders.put(Region.class, new TestDataProvider<Region>() {
      @Override
      public void getValues(final Collection<Region> values) {
        values.add(getRegionSource().getHighestLevelRegion(ExternalSchemes.countryRegionId(Country.US)));
        values.add(getRegionSource().getHighestLevelRegion(ExternalSchemes.countryRegionId(Country.GB)));
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
    s_dataProviders.put(BigDecimal.class, new TestDataProvider<BigDecimal>() {
      @Override
      public void getValues(final Collection<BigDecimal> values) {
        values.add(BigDecimal.ONE);
      }
    });

    s_dataProviders.put(InterestRateNotional.class, new TestDataProvider<Notional>() {
      @Override
      public void getValues(final Collection<Notional> values) {
        values.addAll(permuteTestObjects(InterestRateNotional.class));
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
    s_dataProviders.put(int.class, new TestDataProvider<Integer>() {
      @Override
      public void getValues(Collection<Integer> values) {
        values.add(0);
        int i;
        do {
          i = s_random.nextInt();
        } while (i == 0);
        values.add(i * 100);
        values.add(i * -100);
      }
    });
    s_dataProviders.put(CDSIndexTerms.class, new TestDataProvider<CDSIndexTerms>() {
      @Override
      public void getValues(Collection<CDSIndexTerms> values) {
        values.add(CDSIndexTerms.EMPTY);
        List<Tenor> tenors = getTestObjects(Tenor.class, null);
        if (!tenors.isEmpty()) {
          values.add(CDSIndexTerms.of(tenors.iterator().next()));
        }
        values.add(CDSIndexTerms.of(getTestObjects(Tenor.class, null)));
      }
    });
    s_dataProviders.put(CreditDefaultSwapIndexComponent.class, new TestDataProvider<CreditDefaultSwapIndexComponent>() {
      @Override
      public void getValues(Collection<CreditDefaultSwapIndexComponent> values) {
        values.add(new CreditDefaultSwapIndexComponent(null, null, null, null));
      }

    });
    s_dataProviders.put(CDSIndexComponentBundle.class, new TestDataProvider<CDSIndexComponentBundle>() {
      @Override
      public void getValues(Collection<CDSIndexComponentBundle> values) {
        Collection<CreditDefaultSwapIndexComponent> components = permuteTestObjects(CreditDefaultSwapIndexComponent.class);
        if (!components.isEmpty()) {
          values.add(CDSIndexComponentBundle.of(components.iterator().next()));
        }
        values.add(CDSIndexComponentBundle.of(permuteTestObjects(CreditDefaultSwapIndexComponent.class)));
      }
    });
    s_dataProviders.put(Pairs.of(BondIndex.class, Collection.class), DefaultCollection.of(ArrayList.class, BondIndexComponent.class));
    s_dataProviders.put(Pairs.of(BondIndex.class, List.class), DefaultList.of(ArrayList.class, BondIndexComponent.class));
    s_dataProviders.put(Pairs.of(EquityIndex.class, Collection.class), DefaultCollection.of(ArrayList.class, EquityIndexComponent.class));
    s_dataProviders.put(Pairs.of(EquityIndex.class, List.class), DefaultList.of(ArrayList.class, EquityIndexComponent.class));
    s_dataProviders.put(Pairs.of(IndexFamily.class, Collection.class), DefaultCollection.of(ArrayList.class, IndexFamily.class));
    s_dataProviders.put(Pairs.of(IndexFamily.class, List.class), DefaultList.of(ArrayList.class, IndexFamily.class));
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
        key = Pairs.of(parent, clazz);
      } else if (List.class.equals(clazz)) {
        key = Pairs.of(parent, clazz);
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

  @SuppressWarnings("rawtypes")
  private static List getRandomPermissions() {
    final List permissions = Lists.newArrayList();
    s_dataProviders.get(String.class).getValues(permissions);
    return permissions;
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
    List<MetaProperty<?>> mps = new ArrayList<MetaProperty<?>>(mb.metaPropertyMap().values());

    // find the longest set of available data
    final List<?>[] parameterValues = new List<?>[mps.size()];
    int longest = 0;
    for (int i = 0; i < mps.size(); i++) {
      MetaProperty<?> metaProperty = mps.get(i);
      if (metaProperty.style().isSerializable() && "permissions".equals(metaProperty.name())) {
        parameterValues[i] = getTestObjects(metaProperty.propertyType(), null);
      } else {
        parameterValues[i] = getTestObjects(metaProperty.propertyType(), clazz);
      }
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
          MetaProperty<?> metaProperty = mps.get(j);
          if (metaProperty.style().isSerializable() && metaProperty.name().equals("securityType") == false) {
            builder.set(metaProperty.name(), value);
          }
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
      try {
        securityType = (String) c.getDeclaredField("INDEX_TYPE").get(null);
      } catch (final Throwable t) {
        // Ignore
      }
      try {
        securityType = (String) c.getDeclaredField("METADATA_TYPE").get(null);
      } catch (final Throwable t) {
        // Ignore
      }
      c = c.getSuperclass();
    }
    if (securityClass != RawSecurity.class) {
      assertNotNull(securityType);
    }
    for (final T security : securities) {
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
  public void testAgricultureFutureSecurity() {
    assertSecurities(AgricultureFutureSecurity.class);
  }

  @Override
  public void testBondFutureSecurity() {
    assertSecurities(BondFutureSecurity.class);
  }

  @Override
  public void testCashSecurity() {
    assertSecurities(CashSecurity.class);
  }

  @Override
  public void testCorporateBondSecurity() {
    assertSecurities(CorporateBondSecurity.class);
  }

  @Override
  public void testEnergyFutureSecurity() {
    assertSecurities(EnergyFutureSecurity.class);
  }

  @Override
  public void testEquityOptionSecurity() {
    assertSecurities(EquityOptionSecurity.class);
  }

  @Override
  public void testEquityBarrierOptionSecurity() {
    assertSecurities(EquityBarrierOptionSecurity.class);
  }

  @Override
  public void testEquitySecurity() {
    assertSecurities(EquitySecurity.class);
  }

  @Override
  public void testFRASecurity() {
    assertSecurities(FRASecurity.class);
  }

  @Override
  public void testFXFutureSecurity() {
    assertSecurities(FXFutureSecurity.class);
  }

  @Override
  public void testFXOptionSecurity() {
    assertSecurities(FXOptionSecurity.class);
  }

  @Override
  public void testNonDeliverableFXOptionSecurity() {
    assertSecurities(NonDeliverableFXOptionSecurity.class);
  }

  @Override
  public void testFXBarrierOptionSecurity() {
    assertSecurities(FXBarrierOptionSecurity.class);
  }

  @Override
  public void testForwardSwapSecurity() {
    assertSecurities(ForwardSwapSecurity.class);
  }

  @Override
  public void testIRFutureOptionSecurity() {
    assertSecurities(IRFutureOptionSecurity.class);
  }

  @Override
  public void testEquityIndexDividendFutureOptionSecurity() {
    assertSecurities(EquityIndexDividendFutureOptionSecurity.class);
  }

  @Override
  public void testGovernmentBondSecurity() {
    assertSecurities(GovernmentBondSecurity.class);
  }

  @Override
  public void testIndexFutureSecurity() {
    assertSecurities(IndexFutureSecurity.class);
  }

  @Override
  public void testInterestRateFutureSecurity() {
    assertSecurities(InterestRateFutureSecurity.class);
  }

  @Override
  public void testMetalFutureSecurity() {
    assertSecurities(MetalFutureSecurity.class);
  }

  @Override
  public void testMunicipalBondSecurity() {
    assertSecurities(MunicipalBondSecurity.class);
  }

  @Override
  public void testStockFutureSecurity() {
    assertSecurities(StockFutureSecurity.class);
  }

  @Override
  public void testSwaptionSecurity() {
    assertSecurities(SwaptionSecurity.class);
  }

  @Override
  public void testSwapSecurity() {
    assertSecurities(SwapSecurity.class);
  }

  @Override
  public void testEquityIndexOptionSecurity() {
    assertSecurities(EquityIndexOptionSecurity.class);
  }

  @Override
  public void testFXDigitalOptionSecurity() {
    assertSecurities(FXDigitalOptionSecurity.class);
  }

  @Override
  public void testFXForwardSecurity() {
    assertSecurities(FXForwardSecurity.class);
  }

  @Override
  public void testNonDeliverableFXForwardSecurity() {
    assertSecurities(NonDeliverableFXForwardSecurity.class);
  }

  @Override
  public void testCapFloorSecurity() {
    assertSecurities(CapFloorSecurity.class);
  }

  @Override
  public void testCapFloorCMSSpreadSecurity() {
    assertSecurities(CapFloorCMSSpreadSecurity.class);
  }

  @Override
  public void testRawSecurity() {
    assertSecurities(RawSecurity.class);
  }

  @Override
  public void testEquityVarianceSwapSecurity() {
    assertSecurities(EquityVarianceSwapSecurity.class);
  }

  @Override
  public void testCDSSecurity() {
    assertSecurities(CDSSecurity.class);
  }

  @Override
  public void testStandardFixedRecoveryCDSSecurity() {
    assertSecurities(StandardFixedRecoveryCDSSecurity.class);
  }

  @Override
  public void testStandardRecoveryLockCDSSecurity() {
    assertSecurities(StandardRecoveryLockCDSSecurity.class);
  }

  @Override
  public void testStandardVanillaCDSSecurity() {
    assertSecurities(StandardVanillaCDSSecurity.class);
  }

  @Override
  public void testLegacyFixedRecoveryCDSSecurity() {
    assertSecurities(LegacyFixedRecoveryCDSSecurity.class);
  }

  @Override
  public void testLegacyRecoveryLockCDSSecurity() {
    assertSecurities(LegacyRecoveryLockCDSSecurity.class);
  }

  @Override
  public void testLegacyVanillaCDSSecurity() {
    assertSecurities(LegacyVanillaCDSSecurity.class);
  }

  @Override
  public void testCashFlowSecurity() {
    assertSecurities(CashFlowSecurity.class);
  }

  @Override
  public void testCreditDefaultSwapIndexDefinitionSecurity() {
    assertSecurities(CreditDefaultSwapIndexDefinitionSecurity.class);
  }

  @Override
  public void testCreditDefaultSwapIndexSecurity() {
    assertSecurities(CreditDefaultSwapIndexSecurity.class);
  }

  @Override
  public void testCreditDefaultSwapOptionSecurity() {
    assertSecurities(CreditDefaultSwapOptionSecurity.class);
  }

  @Override
  public void testBondIndex() {
    assertSecurities(BondIndex.class);
  }

  @Override
  public void testEquityIndex() {
    assertSecurities(EquityIndex.class);
  }

  @Override
  public void testIborIndex() {
    assertSecurities(IborIndex.class);
  }

  @Override
  public void testOvernightIndex() {
    assertSecurities(OvernightIndex.class);
  }

  @Override
  public void testIndexFamily() {
    assertSecurities(IndexFamily.class);
  }

}
