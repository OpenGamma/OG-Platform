/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 *
 */
/* package */ class BlotterUtils {

  // TODO this should be configurable, should be able to add from client projects
  /** All the securities and related types supported by the blotter. */
  private static final Set<MetaBean> s_metaBeans = Sets.<MetaBean>newHashSet(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      SwaptionSecurity.meta(),
      CapFloorCMSSpreadSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta(),
      FXOptionSecurity.meta(),
      FRASecurity.meta(),
      CapFloorSecurity.meta(),
      EquityVarianceSwapSecurity.meta(),
      FXBarrierOptionSecurity.meta(),
      FXDigitalOptionSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      FloatingSpreadIRLeg.meta(),
      FloatingGearingIRLeg.meta(),
      FixedInflationSwapLeg.meta(),
      InflationIndexSwapLeg.meta(),
      InterestRateNotional.meta(),
      LegacyVanillaCDSSecurity.meta(),
      LegacyRecoveryLockCDSSecurity.meta(),
      LegacyFixedRecoveryCDSSecurity.meta(),
      StandardVanillaCDSSecurity.meta(),
      StandardRecoveryLockCDSSecurity.meta(),
      StandardFixedRecoveryCDSSecurity.meta(),
      CreditDefaultSwapIndexSecurity.meta(),
      CreditDefaultSwapOptionSecurity.meta(),
      YearOnYearInflationSwapSecurity.meta(),
      ZeroCouponInflationSwapSecurity.meta(),
      CashSecurity.meta());

  /** Meta bean factory for looking up meta beans by type name. */
  private static final MetaBeanFactory s_metaBeanFactory = new MapMetaBeanFactory(s_metaBeans);
  /** Formatter for decimal numbers, DecimalFormat isn't thread safe. */
  private static final ThreadLocal<DecimalFormat> s_decimalFormat = new ThreadLocal<DecimalFormat>() {
    @Override
    protected DecimalFormat initialValue() {
      DecimalFormat decimalFormat = new DecimalFormat("#,###.#####");
      decimalFormat.setParseBigDecimal(true);
      return decimalFormat;
    }
  };

  /**
   * For traversing trade and security {@link MetaBean}s and building instances from the data sent from the blotter.
   * The security type name is filtered out because it is a read-only property. The external ID bundle is filtered
   * out because it is always empty for trades and securities entered via the blotter but isn't nullable. Therefore
   * it has to be explicitly set to an empty bundle after the client data is processed but before the object is built.
   */
  private static final BeanTraverser s_beanBuildingTraverser = new BeanTraverser(
      new PropertyFilter(FinancialSecurity.meta().externalIdBundle()),
      new PropertyFilter(ManageableSecurity.meta().securityType()));

  /** For converting between strings values used by the UI and real objects. */
  private static final StringConvert s_stringConvert;
  /** For converting property values when creating trades and securities from JSON. */
  private static final Converters s_beanBuildingConverters;
  /** For converting property values when creating JSON objects from trades and securities. */
  private static final Converters s_jsonBuildingConverters;

  static {
    StringToRegionIdConverter stringToRegionIdConverter = new StringToRegionIdConverter();
    // for building beans from JSON
    Map<MetaProperty<?>, Converter<?, ?>> beanRegionConverters = Maps.newHashMap();
    beanRegionConverters.putAll(
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            CashSecurity.meta().regionId(), stringToRegionIdConverter,
            CreditDefaultSwapSecurity.meta().regionId(), stringToRegionIdConverter,
            EquityVarianceSwapSecurity.meta().regionId(), stringToRegionIdConverter,
            FRASecurity.meta().regionId(), stringToRegionIdConverter,
            SwapLeg.meta().regionId(), stringToRegionIdConverter));
    beanRegionConverters.putAll(
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            FXForwardSecurity.meta().regionId(), new FXRegionConverter(),
            NonDeliverableFXForwardSecurity.meta().regionId(), new FXRegionConverter()));

    // for building JSON from beans
    RegionIdToStringConverter regionIdToStringConverter = new RegionIdToStringConverter();
    Map<MetaProperty<?>, Converter<?, ?>> jsonRegionConverters =
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            CashSecurity.meta().regionId(), regionIdToStringConverter,
            CreditDefaultSwapSecurity.meta().regionId(), regionIdToStringConverter,
            EquityVarianceSwapSecurity.meta().regionId(), regionIdToStringConverter,
            FRASecurity.meta().regionId(), regionIdToStringConverter,
            SwapLeg.meta().regionId(), regionIdToStringConverter);

    s_stringConvert = new StringConvert();
    s_stringConvert.register(BigDecimal.class, new BigDecimalConverter());
    s_stringConvert.register(Double.class, new DoubleConverter());
    s_stringConvert.register(Double.TYPE, new DoubleConverter());
    s_stringConvert.register(ExternalIdBundle.class, new JodaBeanConverters.ExternalIdBundleConverter());
    s_stringConvert.register(Expiry.class, new ExpiryConverter());
    s_stringConvert.register(MonitoringType.class, new EnumConverter<MonitoringType>());
    s_stringConvert.register(BarrierType.class, new EnumConverter<BarrierType>());
    s_stringConvert.register(BarrierDirection.class, new EnumConverter<BarrierDirection>());
    s_stringConvert.register(SamplingFrequency.class, new EnumConverter<SamplingFrequency>());
    s_stringConvert.register(LongShort.class, new EnumConverter<LongShort>());
    s_stringConvert.register(OptionType.class, new EnumConverter<OptionType>());
    s_stringConvert.register(ZonedDateTime.class, new ZonedDateTimeConverter());
    s_stringConvert.register(OffsetTime.class, new OffsetTimeConverter());
    s_stringConvert.register(DebtSeniority.class, new EnumConverter<DebtSeniority>());
    s_stringConvert.register(StubType.class, new EnumConverter<StubType>());

    s_jsonBuildingConverters = new Converters(jsonRegionConverters, s_stringConvert);
    s_beanBuildingConverters = new Converters(beanRegionConverters, s_stringConvert);
  }

  /**
   * Filters out region ID for FX forwards when building JSON for the security and HTML screens showing the structure.
   * The property value is hard-coded to {@code FINANCIAL_REGION~GB} for FX forwards so its value is of no interest
   * to the client and it can't be updated.
   */
  private static final PropertyFilter s_fxRegionFilter =
      new PropertyFilter(FXForwardSecurity.meta().regionId(), NonDeliverableFXForwardSecurity.meta().regionId());

  /**
   * Filters out the {@code externalIdBundle} property from OTC securities when building the HTML showing the security
   * structure. OTC security details are passed to the blotter back end which generates the ID so this
   * info is irrelevant to the client.
   */
  private static final BeanVisitorDecorator s_externalIdBundleFilter = new PropertyNameFilter("externalIdBundle");

  /**
   * Filters out the underlying ID field of {@link SwaptionSecurity} when building the HTML showing the security
   * structure. The back end creates the underlying security and fills this field in so it's of no interest
   * to the client.
   */
  private static final PropertyFilter s_swaptionUnderlyingFilter = new PropertyFilter(SwaptionSecurity.meta().underlyingId());

  /**
   * Filters out the underlying ID field of {@link CreditDefaultSwapOptionSecurity} when building the HTML showing the security
   * structure. The back end creates the underlying security and fills this field in so it's of no interest
   * to the client.
   */
  private static final PropertyFilter s_cdsOptionUnderlyingFilter = new PropertyFilter(CreditDefaultSwapOptionSecurity.meta().underlyingId());

  /**
   * Filters out the {@code securityType} field for all securities when building the HTML showing the security
   * structure. This value is read-only in each security type and is of no interest to the client.
   */
  private static final PropertyFilter s_securityTypeFilter = new PropertyFilter(ManageableSecurity.meta().securityType());

  /**
   * @return A thread-local formatter instance set to parse numbers into BigDecimals.
   */
  /* package */ static DecimalFormat getDecimalFormat() {
    return s_decimalFormat.get();
  }

  /* package */ static FinancialSecurity buildSecurity(BeanDataSource data) {
    return buildSecurity(data, ExternalIdBundle.EMPTY);
  }

  @SuppressWarnings("unchecked")
  /* package */ static FinancialSecurity buildSecurity(BeanDataSource data, ExternalIdBundle idBundle) {
    BeanVisitor<BeanBuilder<FinancialSecurity>> visitor = new BeanBuildingVisitor<>(data, s_metaBeanFactory,
                                                                                    s_beanBuildingConverters);
    MetaBean metaBean = s_metaBeanFactory.beanFor(data);
    // TODO check it's a FinancialSecurity metaBean
    if (!(metaBean instanceof FinancialSecurity.Meta)) {
      throw new IllegalArgumentException("MetaBean " + metaBean + " isn't for a FinancialSecurity");
    }
    BeanBuilder<FinancialSecurity> builder = (BeanBuilder<FinancialSecurity>) s_beanBuildingTraverser.traverse(metaBean, visitor);
    // externalIdBundle needs to be specified or building fails because it's not nullable
    builder.set(FinancialSecurity.meta().externalIdBundle(), idBundle);
    return builder.build();
  }

  // TODO move to BlotterUtils
  /* package */ static StringConvert getStringConvert() {
    return s_stringConvert;
  }

  /* package */ static Converters getJsonBuildingConverters() {
    return s_jsonBuildingConverters;
  }

  /* package */ static Converters getBeanBuildingConverters() {
    return s_beanBuildingConverters;
  }

  /* package */ static Set<MetaBean> getMetaBeans() {
    return s_metaBeans;
  }

  /* package */ static BeanTraverser structureBuildingTraverser() {
    return new BeanTraverser(s_externalIdBundleFilter, s_securityTypeFilter, s_swaptionUnderlyingFilter, s_cdsOptionUnderlyingFilter, s_fxRegionFilter);
  }

  /* package */
  static BeanTraverser securityJsonBuildingTraverser() {
    return new BeanTraverser(s_securityTypeFilter, s_fxRegionFilter);
  }
}

// ----------------------------------------------------------------------------------


/**
 * For converting between enum instances and strings. The enum value names are made more readable by downcasing
 * and capitalizing them and replacing underscores with spaces.
 * @param <T> Type of the enum
 */
@SuppressWarnings({"rawtypes", "unchecked" })
/* package */ class EnumConverter<T extends Enum> implements StringConverter<T> {

  @Override
  public T convertFromString(Class<? extends T> type, String str) {
    // IntelliJ says this cast is redundant but javac disagrees
    //noinspection RedundantCast
    return (T) Enum.valueOf(type, str.toUpperCase().replace(' ', '_'));
  }

  @Override
  public String convertToString(T e) {
    return WordUtils.capitalize(e.name().toLowerCase().replace('_', ' '));
  }
}

/**
 * Converts {@link ZonedDateTime} to a local date string (e.g. 2012-12-21) and creates a {@link ZonedDateTime} from
 * a local date string with a time of 11:00 and a zone of UTC.
 */
/* package */ class ZonedDateTimeConverter implements StringConverter<ZonedDateTime> {

  @Override
  public ZonedDateTime convertFromString(Class<? extends ZonedDateTime> cls, String localDateString) {
    LocalDate localDate = LocalDate.parse(localDateString);
    return localDate.atTime(11, 0).atZone(ZoneOffset.UTC);
  }

  @Override
  public String convertToString(ZonedDateTime dateTime) {
    return dateTime.toLocalDate().toString();
  }
}

/**
 * Converts an {@link OffsetTime} to a time string (e.g. 11:35) and discards the offset. Creates
 * an {@link OffsetTime} instance by parsing a local date string and using UTC as the offset.
 */
/* package */ class OffsetTimeConverter implements StringConverter<OffsetTime> {

  @Override
  public OffsetTime convertFromString(Class<? extends OffsetTime> cls, String timeString) {
    if (!StringUtils.isEmpty(timeString)) {
      return OffsetTime.of(LocalTime.parse(timeString.trim()), ZoneOffset.UTC);
    } else {
      return null;
    }
  }

  @Override
  public String convertToString(OffsetTime time) {
    return time.toLocalTime().toString();
  }
}

/**
 * Converts between an {@link Expiry} and a local date string (e.g. 2011-03-08).
 */
/* package */ class ExpiryConverter implements StringConverter<Expiry> {

  @Override
  public Expiry convertFromString(Class<? extends Expiry> cls, String localDateString) {
    LocalDate localDate = LocalDate.parse(localDateString);
    return new Expiry(localDate.atTime(11, 0).atZone(ZoneOffset.UTC));
  }

  @Override
  public String convertToString(Expiry expiry) {
    return expiry.getExpiry().toLocalDate().toString();
  }
}

/**
 * Converts doubles to strings in simple format (i.e. no scientific notation). Limits to 5DP.
 */
/* package */ class DoubleConverter implements StringConverter<Double> {

  @Override
  public Double convertFromString(Class<? extends Double> cls, String str) {
    try {
      return BlotterUtils.getDecimalFormat().parse(str).doubleValue();
    } catch (ParseException e) {
      throw new IllegalArgumentException("Failed to parse number", e);
    }
  }

  @Override
  public String convertToString(Double value) {
    return BlotterUtils.getDecimalFormat().format(value);
  }
}

/**
 * Converts big decimals to strings in simple format (i.e. no scientific notation). Limits to 5DP.
 */
/* package */ class BigDecimalConverter implements StringConverter<BigDecimal> {

  @Override
  public BigDecimal convertFromString(Class<? extends BigDecimal> cls, String str) {
    try {
      Number number = BlotterUtils.getDecimalFormat().parse(str);
      // bizarrely if you call setParseBigDecimal(true) on a DecimalFormat it returns a BigDecimal unless the number
      // is NaN or +/- infinity in which case it returns a Double
      if (number instanceof BigDecimal) {
        return (BigDecimal) number;
      } else {
        throw new IllegalArgumentException("Failed to parse number as BigDecimal: " + number);
      }
    } catch (ParseException e) {
      throw new IllegalArgumentException("Failed to parse number", e);
    }
  }

  @Override
  public String convertToString(BigDecimal value) {
    return BlotterUtils.getDecimalFormat().format(value);
  }
}

/**
 * Converts a string to an {@link ExternalId} with a scheme of {@link ExternalSchemes#FINANCIAL}.
 */
/* package */ class StringToRegionIdConverter implements Converter<String, ExternalId> {

  /**
   * Converts a string to an {@link ExternalId} with a scheme of {@link ExternalSchemes#FINANCIAL}.
   * @param region The region name, not empty
   * @return An {@link ExternalId} with a scheme of {@link ExternalSchemes#FINANCIAL} and a value of {@code region}.
   */
  @Override
  public ExternalId convert(String region) {
    if (StringUtils.isEmpty(region)) {
      throw new IllegalArgumentException("Region must not be empty");
    }
    return ExternalId.of(ExternalSchemes.FINANCIAL, region);
  }
}

/**
 * Converts an {@link ExternalId} to a string.
 */
/* package */ class RegionIdToStringConverter implements Converter<ExternalId, String> {

  /**
   * Converts an {@link ExternalId} to a string
   * @param regionId The region ID, not null
   * @return {@code regionId}'s value
   */
  @Override
  public String convert(ExternalId regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    return regionId.getValue();
  }
}
