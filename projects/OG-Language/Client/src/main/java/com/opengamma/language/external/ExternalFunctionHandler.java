/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.lang.annotation.ExternalFunctionParam;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInternalException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.text.Ordinal;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * Handles external functions, producing {@link MetaFunction} wrappers for
 * them.
 */
/* package */final class ExternalFunctionHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(ExternalFunctionHandler.class);

  private abstract static class Wrapper implements PublishedFunction {

    private final ExternalFunction _annotation;
    private final Class<?>[] _parameterTypes;
    private final String[] _parameterNames;
    private final Annotation[][] _parameterAnnotations;

    protected Wrapper(final AnnotatedElement element, final Class<?>[] parameterTypes, final String[] parameterNames, final Annotation[][] parameterAnnotations) {
      ExternalFunction annotation = null;
      for (Annotation anno : element.getAnnotations()) {
        if (anno instanceof ExternalFunction) {
          annotation = (ExternalFunction) anno;
        }
      }
      assert annotation != null;
      _annotation = annotation;
      _parameterTypes = parameterTypes;
      _parameterNames = parameterNames;
      _parameterAnnotations = parameterAnnotations;
    }

    private String getCategory() {
      final String category = _annotation.category();
      if (category.length() == 0) {
        return null;
      } else {
        return category;
      }
    }

    protected abstract String getNameImpl();

    protected abstract String getLongNameImpl();

    private String getName() {
      final String name = _annotation.name();
      if (name.length() == 0) {
        return getNameImpl();
      } else {
        return name;
      }
    }

    private String getDescription() {
      final String description = _annotation.description();
      if (description.length() == 0) {
        return null;
      } else {
        return description;
      }
    }

    private Collection<String> getAlias() {
      final String[] aliases = _annotation.alias();
      if ((aliases != null) && (aliases.length > 0)) {
        return Arrays.asList(aliases);
      } else {
        return Sets.newHashSet(getNameImpl(), getLongNameImpl());
      }
    }

    private static String generateParamName(int index) {
      // Name parameters a, b, c, ..., y, z, aa, ab, ... up to the Java limit of 255 parameters
      if (index >= ('z' - 'a' + 1)) {
        return generateParamName((index / ('z' - 'a' + 1)) - 1) + (char) ('a' + index % ('z' - 'a' + 1));
      } else {
        return String.valueOf((char) ('a' + index % ('z' - 'a' + 1)));
      }
    }

    private List<MetaParameter> getParameters() {
      final List<MetaParameter> parameters = new ArrayList<MetaParameter>(_parameterTypes.length);
      for (int i = 0; i < _parameterTypes.length; i++) {
        String name = null;
        String description = null;
        JavaTypeInfo<?> type = null;
        for (Annotation annotation : _parameterAnnotations[i]) {
          if (annotation instanceof ExternalFunctionParam) {
            final ExternalFunctionParam param = (ExternalFunctionParam) annotation;
            String s = param.type();
            if (s.length() == 0) {
              final JavaTypeInfo.Builder<?> builder = JavaTypeInfo.builder(_parameterTypes[i]);
              if (param.allowNull() && !_parameterTypes[i].isPrimitive()) {
                builder.allowNull();
              }
              type = builder.get();
            } else {
              type = JavaTypeInfo.parseString(s);
            }
            s = param.name();
            if (s.length() != 0) {
              name = s;
            }
            s = param.description();
            if (s.length() != 0) {
              description = s;
            }
          }
        }
        if (name == null) {
          if (_parameterNames != null && StringUtils.isNotBlank(_parameterNames[i])) {
            name = _parameterNames[i];
          } else {
            name = generateParamName(i);
          }
        }
        if (description == null) {
          description = "The " + Ordinal.get(i + 1) + " parameter";
        }
        if (type == null) {
          final JavaTypeInfo.Builder<?> builder = JavaTypeInfo.builder(_parameterTypes[i]);
          if (!_parameterTypes[i].isPrimitive()) {
            builder.allowNull();
          }
          type = builder.get();
        }
        final MetaParameter parameter = new MetaParameter(name, type);
        parameter.setDescription(description);
        parameters.add(parameter);
      }
      return parameters;
    }

    protected abstract FunctionInvoker getInvoker(final List<MetaParameter> parameters);

    @Override
    public final MetaFunction getMetaFunction() {
      final List<MetaParameter> parameters = getParameters();
      final String name = getName();
      final MetaFunction function = new MetaFunction(getCategory(), name, parameters, getInvoker(parameters));
      function.setDescription(getDescription());
      final Collection<String> aliases = getAlias();
      aliases.remove(name);
      function.setAlias(aliases);
      return function;
    }

  }

  private static final class MethodInvoker extends AbstractFunctionInvoker {

    private final Method _method;
    private final Object _instance;

    public MethodInvoker(final Method method, final Object instance, final List<MetaParameter> parameters) {
      super(parameters);
      _method = method;
      _instance = instance;
    }

    @Override
    protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
      try {
        return _method.invoke(_instance, parameters);
      } catch (Throwable t) {
        throw new InvokeInternalException(t);
      }
    }

  }

  private static final class MethodWrapper extends Wrapper {

    private final Method _method;
    private final Object _instance;

    public MethodWrapper(final Method method, final Object instance) {
      super(method, method.getParameterTypes(), tryGetParameterNames(method), method.getParameterAnnotations());
      _method = method;
      _instance = instance;
    }

    @Override
    protected FunctionInvoker getInvoker(final List<MetaParameter> parameters) {
      return new MethodInvoker(_method, _instance, parameters);
    }

    @Override
    protected String getNameImpl() {
      return _method.getDeclaringClass().getSimpleName() + "." + _method.getName();
    }

    @Override
    protected String getLongNameImpl() {
      return _method.getDeclaringClass().getName() + "." + _method.getName();
    }

    @Override
    public String toString() {
      return "Method " + _method + " on " + _instance;
    }

  }

  private static final class ConstructorInvoker extends AbstractFunctionInvoker {

    private final Constructor<?> _constructor;

    public ConstructorInvoker(final Constructor<?> constructor, final List<MetaParameter> parameters) {
      super(parameters);
      _constructor = constructor;
    }

    @Override
    protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
      try {
        return _constructor.newInstance(parameters);
      } catch (Throwable t) {
        throw new InvokeInternalException(t);
      }
    }

  }

  private static final class ConstructorWrapper extends Wrapper {

    private final Constructor<?> _constructor;

    public ConstructorWrapper(final Constructor<?> constructor) {
      super(constructor, constructor.getParameterTypes(), tryGetParameterNames(constructor), constructor.getParameterAnnotations());
      _constructor = constructor;
    }

    @Override
    protected FunctionInvoker getInvoker(final List<MetaParameter> parameters) {
      return new ConstructorInvoker(_constructor, parameters);
    }

    @Override
    protected String getNameImpl() {
      return _constructor.getDeclaringClass().getSimpleName();
    }

    @Override
    protected String getLongNameImpl() {
      return _constructor.getDeclaringClass().getName();
    }

    @Override
    public String toString() {
      return "Constructor invocation " + _constructor;
    }

  }

  private final Collection<PublishedFunction> _functions;

  /**
   * Creates a handler wrapper for a given class.
   * 
   * @param clazz the class containing external function methods
   */
  public ExternalFunctionHandler(final Class<?> clazz) {
    final Constructor<?>[] constructors = clazz.getConstructors();
    final Method[] methods = clazz.getMethods();
    final ArrayList<PublishedFunction> functions = new ArrayList<PublishedFunction>(constructors.length + methods.length);
    // Only need an instance of the class if one or more annotated methods are not static. In this case, the same
    // instance will be re-used for each non-static method. If instantiation fails (e.g. no default constructor), just
    // skip instance methods and log warnings.
    Object sharedInstance = null;
    boolean instantiateFailed = false;
    for (Constructor<?> constructor : constructors) {
      if (!constructor.isAnnotationPresent(ExternalFunction.class)) {
        continue;
      }
      s_logger.debug("Found constructor {}", constructor);
      // If there is a constructor method, can only have static declarations
      instantiateFailed = true;
      functions.add(new ConstructorWrapper(constructor));
    }
    for (Method method : methods) {
      if (!method.isAnnotationPresent(ExternalFunction.class)) {
        continue;
      }
      s_logger.debug("Found method {}", method);
      final Object instance;
      if (Modifier.isStatic(method.getModifiers())) {
        instance = null;
      } else {
        if (instantiateFailed) {
          s_logger.warn("Skipping method {}", method);
          continue;
        } else if (sharedInstance == null) {
          sharedInstance = tryGetInstance(clazz);
          if (sharedInstance == null) {
            s_logger.warn("Default instantiation failed for {}", clazz);
            s_logger.warn("Skipping method {}", method);
            instantiateFailed = true;
            continue;
          }
        }
        instance = sharedInstance;
      }
      functions.add(new MethodWrapper(method, instance));
    }
    functions.trimToSize();
    _functions = functions;
  }

  private static String[] tryGetParameterNames(AccessibleObject methodOrConstructor) {
    Paranamer paranamer = new BytecodeReadingParanamer();
    String[] parameterNames = null;
    try {
      parameterNames = paranamer.lookupParameterNames(methodOrConstructor);
    } catch (Exception e) {
      // Requires debugging information in the bytecode which might not be present, so not really an error 
      s_logger.info("Error looking up parameter names from bytecode for: " + methodOrConstructor, e);
    }
    return parameterNames;
  }
  
  private static Object tryGetInstance(final Class<?> clazz) {
    try {
      return clazz.newInstance();
    } catch (Throwable t) {
      return null;
    }
  }

  /**
   * Returns meta function wrappers for all methods defined within the class
   * 
   * @return the function definitions
   */
  public Collection<MetaFunction> getFunctions() {
    final Collection<MetaFunction> functions = new ArrayList<MetaFunction>(_functions.size());
    for (PublishedFunction function : _functions) {
      final MetaFunction meta = function.getMetaFunction();
      s_logger.info("Registering function {}", meta.getName());
      functions.add(meta);
    }
    return functions;
  }

}
