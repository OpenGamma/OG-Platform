/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.financial.Currency;

/**
 * 
 *
 * @author kirk
 */
public class DiscountCurveDefinition implements Serializable {
  private final Currency _currency;
  private final String _name;
  private final SortedSet<FixedIncomeStrip> _strips = new TreeSet<FixedIncomeStrip>(new Comparator<FixedIncomeStrip>() {
    @Override
    public int compare(FixedIncomeStrip o1, FixedIncomeStrip o2) {
      double n1 = o1.getNumYears();
      double n2 = o2.getNumYears();
      if(n1 < n2) {
        return -1;
      } else if (n1 > n2) {
        return 1;
      }
      return 0;
    }
  });
  
  public DiscountCurveDefinition(Currency currency, String name) {
    this(currency, name, Collections.<FixedIncomeStrip>emptySet());
  }

  public DiscountCurveDefinition(String isoCode, String name, Collection<FixedIncomeStrip> strips) {
    this(Currency.getInstance(isoCode), name, strips);
  }

  public DiscountCurveDefinition(Currency currency, String name, Collection<FixedIncomeStrip> strips) {
    _currency = currency;
    _name = name;
    for(FixedIncomeStrip strip : strips) {
      addStrip(strip);
    }
  }

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }
  
  public void addStrip(FixedIncomeStrip strip) {
    _strips.add(strip);
  }
  
  public Collection<FixedIncomeStrip> getStrips() {
    return Collections.unmodifiableSet(_strips);
  }
  
  public Set<AnalyticValueDefinition<?>> getRequiredInputs() {
    Set<AnalyticValueDefinition<?>> requiredInputs = new HashSet<AnalyticValueDefinition<?>>();
    for(FixedIncomeStrip strip : _strips) {
      requiredInputs.add(strip.getStripValueDefinition());
    }
    return requiredInputs;
  }

}
