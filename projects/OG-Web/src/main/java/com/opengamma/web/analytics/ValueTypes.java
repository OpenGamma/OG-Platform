/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Maps value names to their types.
 * TODO this is very much a work in progress
 * TODO this needs to be extensible so new types can be added in client projects without editing the main list
 * TODO do we need the ability to exclude types that are in the main list?
 * this might be necessary for values where a function can return different types at different times. these need to have
 * no type associated with them so the UI gets sent the type with every set of results. if that is required for a value
 * name which has a type in the main list there needs to be a mechanism to override it
 */
public class ValueTypes {

  private static final Logger s_logger = LoggerFactory.getLogger(ValueTypes.class);
  private static final Map<String, Class<?>> s_valueNameToType = Maps.newHashMap();

  static {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(ValueTypes.class.getResourceAsStream("ValueTypes.txt")));
      String line;
      Pattern pattern = Pattern.compile("'(.*)' (.*)");
      while ((line = reader.readLine()) != null) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
          throw new RuntimeException(line);
        }
        String valueName = matcher.group(1);
        String className = matcher.group(2);
        Class<?> valueType;
        try {
          valueType = Class.forName(className);
        } catch (ClassNotFoundException e) {
          s_logger.warn("Failed to load class " + className, e);
          continue;
        }
        s_valueNameToType.put(valueName, valueType);
      }
    } catch (IOException e) {
      s_logger.warn("Failed to load type mappings for value names", e);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /*public static void main(String[] args) throws IOException {
    SortedMap<String, String> reqTypes = new TreeMap<String, String>();
    for (Field field : ValueRequirementNames.class.getFields()) {
      if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC)
          && String.class.equals(field.getType())) {
        try {
          reqTypes.put((String) field.get(null), field.getName());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("#valueReqs=" + reqTypes.size());
    BufferedReader reader = new BufferedReader(new InputStreamReader(ValueRequirementTypes.class.getResourceAsStream("ValueTypes.txt")));
    String line;
    Pattern pattern = Pattern.compile("'(.*)' (.*)");
    while ((line = reader.readLine()) != null) {
      Matcher matcher = pattern.matcher(line);
      if (!matcher.matches()) {
        throw new RuntimeException(line);
      }
      String name = matcher.group(1);
      //String className = matcher.group(2);
      reqTypes.remove(name);
    }
    TreeSet<String> reqVarNames = new TreeSet<String>(reqTypes.values());
    System.out.println("#valueReqs=" + reqVarNames.size());
    System.out.println(reqVarNames);
    for (String reqVarName : reqVarNames) {
      System.out.println(reqVarName);
    }
  }*/

  public static Class<?> getTypeForValueName(String valueName) {
    return s_valueNameToType.get(valueName);
  }
}
