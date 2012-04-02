/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.examples;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Utility Main method invoker
 */
public class MainRunner {
  private static final Logger s_logger = LoggerFactory.getLogger(MainRunner.class);
  
  private List<String> _clazzes = Lists.newArrayList();
  private Map<String, String> _args = Maps.newHashMap();
  
  /**
   * Gets the clazzes.
   * @return the clazzes
   */
  public List<String> getClazzes() {
    return _clazzes;
  }

  /**
   * Sets the clazzes.
   * @param clazzes  the clazzes
   */
  public void setClazzes(List<String> clazzes) {
    _clazzes.clear();
    _clazzes.addAll(clazzes);
  }

  /**
   * Gets the args.
   * @return the args
   */
  public Map<String, String> getArgs() {
    return _args;
  }

  /**
   * Sets the args.
   * @param args  the args
   */
  public void setArgs(Map<String, String> args) {
    _args.clear();
    for (Entry<String, String> entry : args.entrySet()) {
      _args.put(entry.getKey(), entry.getValue());
    }
  }
  
  public void run() throws Exception {
    for (String clazz : getClazzes()) {
      final Class<?> cls = Class.forName(clazz);
      final Method method = cls.getMethod("main", String[].class);
      final String args = getArgs().get(clazz);
      String[] params = args != null ? getParameters(args) : new String[] {};
      s_logger.info("invoking {} with parameter {}", clazz, Arrays.asList(params));
//      System.out.println("invoking " + clazz + " with parameter " + Arrays.asList(params));
      method.invoke(null, (Object) params);
      s_logger.info("finished");
//      System.out.println("finished");
    }
  }

  private String[] getParameters(String args) {
    args = StringUtils.trimToNull(args);
//    StringUtils.replaceChars(args, "'", "\"");    
    return StringUtils.split(args);
  }

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception { //CSIGNORE    
    s_logger.debug("processing args : {}" + Arrays.asList(args));
    if (args == null || args.length == 0) {
      s_logger.debug("command args is required");
      return;
    }
    
    int num = getClassSize(args);
    Options options = new Options();
    options.addOption(createSizeOption());
    for (int i = 0; i < num; i++) {
      options.addOption(createClazzOption("class" + i));
      options.addOption(createArgsOption("arg" + i));
    }
    
    PosixParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      usage(options);
      System.exit(-1);
    }
    MainRunner examplesInitializer = new MainRunner();
    examplesInitializer.setClazzes(getClassList(line, num));
    examplesInitializer.setArgs(getArgsMap(line, num));
    examplesInitializer.run();
  }

  private static int getClassSize(String[] args) {
    List<String> argsList = Arrays.asList(args);
    String sizeArg = null;
    int nIndex = argsList.indexOf("-n");
    if (nIndex >= 0) {
      sizeArg = argsList.get(nIndex + 1);
    } else {
      int numIndex = argsList.indexOf("--num");
      if (numIndex >= 0) {
        sizeArg = argsList.get(numIndex + 1);
      }
    }
    Options options = new Options();
    options.addOption(createSizeOption());
    int result = 0;
    if (sizeArg != null) {
      try {
        result = Integer.parseInt(sizeArg);
      } catch (Exception ex) {
        usage(options);
        System.exit(-1);
      }
    } else {
      usage(options);
      System.exit(-1);
    }
    return result;
  }

  private static Option createArgsOption(String optionName) {
    OptionBuilder.withLongOpt(optionName);
    OptionBuilder.withDescription("arguments to classes");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("arg");
    OptionBuilder.isRequired(false);
    return OptionBuilder.create(optionName);
  }

  private static Option createClazzOption(String optionName) {
    OptionBuilder.withLongOpt(optionName);
    OptionBuilder.withDescription("classes to run");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("class");
    OptionBuilder.isRequired(true);
    return OptionBuilder.create(optionName);
  }

  private static Option createSizeOption() {
    OptionBuilder.withLongOpt("num");
    OptionBuilder.withDescription("size of classes to start in order");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("SIZE");
    OptionBuilder.isRequired(true);
    return OptionBuilder.create("n");
  }

  private static Map<String, String> getArgsMap(CommandLine line, int num) {
    Map<String, String> result = Maps.newHashMap();
    for (int i = 0; i < num; i++) {
      String args = line.getOptionValue("arg" + i);
      if (args != null) {
        String clazz = line.getOptionValue("class" + i);
        result.put(clazz, args);
      }
    }
    return result;
  }

  private static List<String> getClassList(CommandLine line, int num) {
    List<String> result = Lists.newArrayList();
    for (int i = 0; i < num; i++) {
      result.add(line.getOptionValue("class" + i));
    }
    return result;
  }

  
  
  public static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java " + MainRunner.class.getName(), options);
  }

}
