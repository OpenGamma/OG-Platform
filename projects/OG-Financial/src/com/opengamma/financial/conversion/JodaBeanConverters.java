/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.ArrayList;
import javax.time.calendar.ZonedDateTime;

import org.joda.beans.JodaBeanUtils;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Registers converters with Joda Beans for converting bean fields to and from strings.  The registration is
 * done in the constructor and after that the instance doesn't do anything.
 */
public final class JodaBeanConverters {
  
  /**
   * Singleton instance.
   */
  private static final JodaBeanConverters INSTANCE = new JodaBeanConverters();

  private JodaBeanConverters() {
    StringConvert stringConvert = JodaBeanUtils.stringConverter();
    stringConvert.register(Frequency.class, new FrequencyConverter());
    stringConvert.register(Currency.class, new CurrencyConverter());
    stringConvert.register(DayCount.class, new DayCountConverter());
    stringConvert.register(ExternalId.class, new ExternalIdConverter());
    stringConvert.register(ExternalIdBundle.class, new ExternalIdBundleConverter());
    stringConvert.register(CurrencyPair.class, new CurrencyPairConverter());
    stringConvert.register(ObjectId.class, new ObjectIdConverter());
    stringConvert.register(UniqueId.class, new UniqueIdConverter());
    stringConvert.register(Expiry.class, new ExpiryConverter());
    stringConvert.register(ExerciseType.class, new ExerciseTypeConverter());
    stringConvert.register(Notional.class, new NotionalConverter());
    stringConvert.register(BusinessDayConvention.class, new BusinessDayConventionConverter());
  }
  
  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static JodaBeanConverters getInstance() {
    return INSTANCE;
  }

  private abstract static class AbstractConverter<T> implements StringConverter<T> {
    @Override
    public String convertToString(T t) {
      return t.toString();
    }
  }

  private static class FrequencyConverter implements StringConverter<Frequency> {

    @Override
    public String convertToString(Frequency frequency) {
      return frequency.getConventionName();
    }

    @Override
    public Frequency convertFromString(Class<? extends Frequency> cls, String conventionName) {
      return SimpleFrequencyFactory.INSTANCE.getFrequency(conventionName);
    }
  }

  /* package */ static class DayCountConverter implements StringConverter<DayCount> {

    @Override
    public String convertToString(DayCount dayCount) {
      return dayCount.getConventionName();
    }

    @Override
    public DayCount convertFromString(Class<? extends DayCount> cls, String conventionName) {
      return DayCountFactory.INSTANCE.getDayCount(conventionName);
    }
  }

  private static class CurrencyConverter extends AbstractConverter<Currency> {
    @Override
    public Currency convertFromString(Class<? extends Currency> cls, String str) {
      return Currency.of(str);
    }
  }
  
  private static class ExternalIdConverter extends AbstractConverter<ExternalId> {

    @Override
    public ExternalId convertFromString(Class<? extends ExternalId> cls, String str) {
      return ExternalId.parse(str);
    }
    
  }
  
  private static class ExternalIdBundleConverter extends AbstractConverter<ExternalIdBundle> {
  
    @Override
    public String convertToString(ExternalIdBundle object) {
      String str = object.toString();
      return str.substring(str.indexOf('[') + 1, str.lastIndexOf(']') - 1);
    }
    
    @Override
    public ExternalIdBundle convertFromString(Class<? extends ExternalIdBundle> cls, String str) {
      ArrayList<String> strings = new ArrayList<String>();
      for (String s : str.split(",")) {
        strings.add(s);
      }
      return ExternalIdBundle.parse(strings);
    }

  }
  
  private static class ObjectIdConverter extends AbstractConverter<ObjectId> {

    @Override
    public ObjectId convertFromString(Class<? extends ObjectId> cls, String str) {
      return ObjectId.parse(str);
    }
    
  }

  private static class UniqueIdConverter extends AbstractConverter<UniqueId> {

    @Override
    public UniqueId convertFromString(Class<? extends UniqueId> cls, String str) {
      return UniqueId.parse(str);
    }
    
    @Override
    public String convertToString(UniqueId uniqueId) {
      return uniqueId.toString();
    }
    
  }

  private static class CurrencyPairConverter implements StringConverter<CurrencyPair> {

    @Override
    public String convertToString(CurrencyPair object) {
      return object.getName();
    }

    @Override
    public CurrencyPair convertFromString(Class<? extends CurrencyPair> cls, String str) {
      return CurrencyPair.parse(str);
    }
    
  }

  private static class ExpiryConverter extends AbstractConverter<Expiry> {

    @Override
    public String convertToString(Expiry expiry) {
      return expiry.getExpiry().toString();
    }

    @Override
    public Expiry convertFromString(Class<? extends Expiry> cls, String str) {
      return new Expiry(ZonedDateTime.parse(str));
    }
  }

  private static class ExerciseTypeConverter extends AbstractConverter<ExerciseType> {

    @Override
    public String convertToString(ExerciseType exType) {
      return exType.getName();
    }

    @Override
    public ExerciseType convertFromString(Class<? extends ExerciseType> cls, String str) {
      if (str.equals("American")) {
        return new AmericanExerciseType();
      } else if (str.equals("Asian")) {
        return new AsianExerciseType();
      } else if (str.equals("Bermudan")) {
        return new BermudanExerciseType();
      } else if (str.equals("European")) {
        return new EuropeanExerciseType();
      } else {
        return new EuropeanExerciseType();
      }
    }
  }

  private static class NotionalConverter extends AbstractConverter<Notional> {

    @Override
    public String convertToString(Notional notional) {
      if (notional.getClass().isAssignableFrom(InterestRateNotional.class)) {
        return ((InterestRateNotional) notional).getCurrency().getCode() 
            + " " + String.format("%f", ((InterestRateNotional) notional).getAmount());
      } else if (notional.getClass().isAssignableFrom(CommodityNotional.class)) {
        return notional.toString();
      } else if (notional.getClass().isAssignableFrom(SecurityNotional.class)) {
        return ((SecurityNotional) notional).getNotionalId().toString();
      } else if (notional.getClass().isAssignableFrom(VarianceSwapNotional.class)) {
        return ((VarianceSwapNotional) notional).getCurrency().getCode() 
            + " " + String.format("%f", ((VarianceSwapNotional) notional).getAmount());
      }
      return notional.toString();
    }

    @Override
    public Notional convertFromString(Class<? extends Notional> cls, String str) {      
      if (cls.isAssignableFrom(InterestRateNotional.class)) {
        String[] s = str.split(" ", 2);        
        return new InterestRateNotional(Currency.of(s[0].trim()), Double.parseDouble(s[1].trim()));
      } else if (cls.isAssignableFrom(CommodityNotional.class)) {
        return new CommodityNotional();
      } else if (cls.isAssignableFrom(SecurityNotional.class)) {
        return new SecurityNotional(UniqueId.parse(str));
      } else if (cls.isAssignableFrom(VarianceSwapNotional.class)) {
        String[] s = str.split(" ", 2);
        return new VarianceSwapNotional(Currency.of(s[0].trim()), Double.parseDouble(s[1].trim()));
      }
      return null;
    }
  }

  private static class BusinessDayConventionConverter extends AbstractConverter<BusinessDayConvention> {

    @Override
    public String convertToString(BusinessDayConvention object) {
      return object.getConventionName();
    }

    @Override
    public BusinessDayConvention convertFromString(Class<? extends BusinessDayConvention> cls, String str) {
      return BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(str);
    }
    
  }

}
