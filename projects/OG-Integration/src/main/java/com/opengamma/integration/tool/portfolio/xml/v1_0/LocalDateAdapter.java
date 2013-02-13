package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatters;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

  private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatters.pattern("yyyy-MM-dd");

  @Override
  public LocalDate unmarshal(String v) throws Exception {
    return DATE_FORMATTER.parse(v, LocalDate.class);
  }

  @Override
  public String marshal(LocalDate v) throws Exception {
    return DATE_FORMATTER.print(v);
  }
}
