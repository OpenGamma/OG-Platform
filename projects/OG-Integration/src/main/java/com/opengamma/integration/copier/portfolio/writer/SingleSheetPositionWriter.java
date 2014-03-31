/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.writer;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for a portfolio writer that writes to a single sheet
 */
public abstract class SingleSheetPositionWriter implements PositionWriter {

  private SheetWriter _sheet;         // The spreadsheet to which to export

  public SingleSheetPositionWriter(SheetWriter sheet) {
    ArgumentChecker.notNull(sheet, "sheet");
    _sheet = sheet;
  }

  public SheetWriter getSheet() {
    return _sheet;
  }

  public void setSheet(SheetWriter sheet) {
    ArgumentChecker.notNull(sheet, "sheet");
    _sheet = sheet;
  }

  @Override
  public void flush() {
    _sheet.flush();
  }

  @Override
  public void close() {
    flush();
    _sheet.close();
  }

  public static String attributesToString(Map<String, String> attributes) {
    final StringBuilder sb = new StringBuilder();
    for (String key : newTreeSet(attributes.keySet())) {
      String value = attributes.get(key);
      sb.append("[").append(key).append(":").append(value).append("]");
    }
    return sb.toString();
  }

  public static Map<String, String> attributesToMap(String attributes) {
    final Map<String, String> result = newHashMap();
    if (attributes != null) {
      final Pattern attributesPattern = Pattern.compile("(\\[.*?\\])");
      final Pattern attributePattern = Pattern.compile("\\[(.*?):(.*?)\\]");
      final Matcher attrsMatcher = attributesPattern.matcher(attributes);
      while (attrsMatcher.find()) {
        final Matcher attrMatcher = attributePattern.matcher(attrsMatcher.group());
        if (attrMatcher.matches()) {
          result.put(attrMatcher.group(1), attrMatcher.group(2));
        }
      }
    }
    return result;
  }

}
