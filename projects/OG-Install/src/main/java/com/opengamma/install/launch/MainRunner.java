/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.launch;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.util.tuple.Pair;

/**
 * Utility Main method invoker
 */
public class MainRunner implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(MainRunner.class);
  
  private final List<Pair<String, String>> _tasks;
  
  public MainRunner(final String[] args) {
    if ((args.length % 2) != 0)
      throw new IllegalArgumentException();
    _tasks = new ArrayList<Pair<String, String>>(args.length);
    for (int i = 0; i < args.length; i += 2) {
      _tasks.add(Pair.of(args[i], args[i + 1]));
    }
  }

  protected Method getMainMethod(final String clazz) {
    try {
      s_logger.debug("processing class [{}]", clazz);
      return Class.forName(clazz).getMethod("main", String[].class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Class " + clazz + " not found or does not have a main method");
    }
  }

  protected String[] getParameters(String args) {
    s_logger.debug("processing args [{}]", args);
    args = StringUtils.trimToNull(args);
    final String[] tokens = StringUtils.split(args);
    String[] result = null;
    if (tokens != null) {
      List<String> argList = Lists.newArrayList();
      StringBuilder strBuilder = new StringBuilder();
      for (String token : tokens) {
        boolean processed = false;
        if (token.startsWith("'")) {
          strBuilder.append(token);
          processed = true;
        }
        if (token.endsWith("'")) {
          if (strBuilder.length() != 0) {
            if (!processed) {
              strBuilder.append(" ").append(token);
            }
            argList.add(strBuilder.substring(1, strBuilder.length() - 1));
          } else {
            argList.add(token);
          }
          strBuilder = new StringBuilder();
          processed = true;
        }
        if (!processed) {
          if (strBuilder.length() == 0) {
            argList.add(token);
          } else {
            strBuilder.append(" ").append(token);
          }
        }
      }
      if (strBuilder.length() != 0) {
        argList.addAll(Arrays.asList(StringUtils.split(strBuilder.toString())));
      }
      result = argList.toArray(new String[] {});
    }
    s_logger.debug("processed args {}", ArrayUtils.toString(result));
    return result;
  }

  protected void invokeMain(final String clazz, final String args) {
    final Method method = getMainMethod(clazz);
    final String[] params = getParameters(args);
    s_logger.info("Invoking {}.main with {}", clazz, args);
    try {
      method.invoke(null, (Object) params);
      s_logger.debug("{} finished", clazz);
    } catch (Throwable t) {
      s_logger.error("Error invoking {}.main", clazz);
      s_logger.warn("Caught exception", t);
    }
  }

  @Override
  public void run() {
    for (Pair<String, String> task : _tasks) {
      invokeMain(task.getFirst(), task.getSecond());
    }
  }

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) { //CSIGNORE
    final MainRunner main = new MainRunner(args);
    main.run();
  }

}
