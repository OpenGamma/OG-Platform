/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Registers converters with Joda Beans for converting bean fields to and from strings.  The registration is
 * done in the constructor and after that the instance doesn't do anything.
 */
public final class JodaBeanConverters {
  
  /**
   * Singleton instance.
   */
  private static final JodaBeanConverters INSTANCE = new JodaBeanConverters();

  // TODO map of MetaProperties to converters for fields that cant rely on the default conversion e.g.
  // dates that are stored with time info and need the times setting to default values
  // ZonedDateTime needs a default zone, based on OpenGammaClock.getTimeZone(), only for certain properties

  private JodaBeanConverters() {
    StringConvert stringConvert = JodaBeanUtils.stringConverter();
    stringConvert.register(ExternalIdBundle.class, new ExternalIdBundleConverter());
    stringConvert.register(Expiry.class, new ExpiryConverter());
    stringConvert.register(Notional.class, new NotionalConverter());
    stringConvert.register(BondFutureDeliverable.class, new BondFutureDeliverableConverter());
    stringConvert.register(CDSIndexTerms.class, new CDSIndexTermsConverter());
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

  public static class ExternalIdBundleConverter extends AbstractConverter<ExternalIdBundle> {
  
    @Override
    public String convertToString(ExternalIdBundle object) {
      String str = object.toString();
      return str.substring(str.indexOf('[') + 1, str.lastIndexOf(']')).trim();
    }
    
    @Override
    public ExternalIdBundle convertFromString(Class<? extends ExternalIdBundle> cls, String str) {
      if (StringUtils.isEmpty(str)) {
        return ExternalIdBundle.EMPTY;
      }
      List<String> strings = Lists.newArrayList();
      for (String s : str.split(",")) {
        strings.add(s.trim());
      }
      return ExternalIdBundle.parse(strings);
    }

  }

  public static class ExpiryConverter extends AbstractConverter<Expiry> {

    @Override
    public String convertToString(Expiry expiry) {
      return expiry.getExpiry().toString();
    }

    @Override
    public Expiry convertFromString(Class<? extends Expiry> cls, String str) {
      return new Expiry(ZonedDateTime.parse(str));
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

  private static class BondFutureDeliverableConverter extends AbstractConverter<BondFutureDeliverable> {

    @Override
    public String convertToString(BondFutureDeliverable object) {
      String ids = object.getIdentifiers().toString();
      ids = ids.substring(ids.indexOf("["), ids.indexOf("]") + 1);
      return ids + " " + Double.toString(object.getConversionFactor());
    }

    @Override
    public BondFutureDeliverable convertFromString(Class<? extends BondFutureDeliverable> cls, String str) {
      List<String> ids = Lists.newArrayList();
      for (String s : str.substring(str.indexOf('[') + 1, str.lastIndexOf(']')).trim().split(",")) {
        ids.add(s.trim());
      }
      BondFutureDeliverable result = new BondFutureDeliverable();
      result.setIdentifiers(ExternalIdBundle.parse(ids));
      result.setConversionFactor(Double.parseDouble(str.substring(str.indexOf(']') + 1).trim()));
      return result;
    }
  }
  
  private static class CDSIndexTermsConverter extends AbstractConverter<CDSIndexTerms> {

    @Override
    public String convertToString(CDSIndexTerms object) {
      Set<Tenor> tenors = object.getTenors();
      return tenors.toString();
    }
    
    @Override
    public CDSIndexTerms convertFromString(Class<? extends CDSIndexTerms> cls, String str) {
      String[] tenorStrs = str.split(",");
      Tenor[] tenors = new Tenor[tenorStrs.length];
      for (int i = 0; i < tenorStrs.length; i++) {
        Tenor tenor = Tenor.parse(tenorStrs[i]);
        tenors[i] = tenor;
      }
      return CDSIndexTerms.of(tenors);
    }
    
  }
  
//  private static class CDSIndexComponentBundleConverter extends AbstractConverter<CDSIndexComponentBundle> {
//
//    @Override
//    public String convertToString(CDSIndexComponentBundle object) {
//      StringBuilder sb = new StringBuilder();
//      Iterable<CreditDefaultSwapIndexComponent> components = object.getComponents();
//      for (CreditDefaultSwapIndexComponent component : components) {
//        sb.append(JodaBeanUtils.stringConverter().convertToString(component));
//        sb.append(",");
//      }
//      if (sb.length() > 0) {
//        sb.deleteCharAt(sb.length() - 1);
//      }
//      return sb.toString();
//    }
//    @Override
//    public CDSIndexComponentBundle convertFromString(Class<? extends CDSIndexComponentBundle> cls, String str) {
//      return null;
//    }
//    
//  }
}
