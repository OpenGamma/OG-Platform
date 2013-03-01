/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class, type = LocalDate.class),
    @XmlJavaTypeAdapter(value = OptionTypeAdapter.class, type = OptionType.class),
    @XmlJavaTypeAdapter(value = CurrencyAdapter.class, type = Currency.class),
})
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.money.Currency;
