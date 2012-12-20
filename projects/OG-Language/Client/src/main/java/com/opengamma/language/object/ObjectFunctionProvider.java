/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.FunctionProvider;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.security.CreateSecurityFunction;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A {@link FunctionProvider} that builds functions based on object constructors and their accessor/mutator
 * methods. The object information is read in from a resource file. The basic OG-Language functionality
 * can be extended for a custom language binding by inheriting from this class and re-implementing the
 * {@link #loadDefinitions} method.
 */
public class ObjectFunctionProvider extends AbstractFunctionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(ObjectFunctionProvider.class);

  private static final String[] EMPTY = new String[0];

  // TODO: move these into the resource file

  private static final String CONSTRUCTOR_DESCRIPTION_PREFIX = "Defines ";
  private static final String PARAMETER_DESCRIPTION_PREFIX = "The ";

  /**
   * 
   */
  protected final class AttributeInfo {

    /**
     * The name of the attribute to use when constructing parameter names. 
     */
    private final String _name;
    /**
     * The display name of the attribute to use in descriptions.
     */
    private final String _label;
    /**
     * Whether the attribute *should* be readable. The attribute is exposed if this is set and there is an accessible getter.
     */
    private final boolean _readable;
    /**
     * Whether the attribute *should* be writeable. The attribute is exposed if this is set and there is an accessible setter.
     */
    private final boolean _writeable;
    /**
     * The preferred alias to use when constructing method names for the bound language.
     */
    private final String _alias;

    public AttributeInfo(final String name, final String label) {
      this(name, label, true, true, name);
    }

    public AttributeInfo(final String name, final String label, final boolean readable, final boolean writeable, final String alias) {
      _name = name;
      _label = label;
      _readable = readable;
      _writeable = writeable;
      _alias = alias;
    }

    public String getName() {
      return _name;
    }

    public String getLabel() {
      return _label;
    }

    public boolean isReadable() {
      return _readable;
    }

    public boolean isWriteable() {
      return _writeable;
    }

    public String getAlias() {
      return _alias;
    }

    public String getDescription() {
      return PARAMETER_DESCRIPTION_PREFIX + getLabel();
    }

  }

  /**
   * 
   */
  protected final class ObjectInfo {

    private final Class<?> _clazz;
    private final Map<String, AttributeInfo> _attributeInfo = new HashMap<String, AttributeInfo>();
    private boolean _abstract;
    private String _name;
    private String _category;
    private String _label;
    private String _constructorDescription;
    private String[] _constructorParameters = EMPTY;
    private ObjectInfo _superclass;

    private ObjectInfo(final Class<?> clazz) {
      _clazz = clazz;
      _abstract = Modifier.isAbstract(clazz.getModifiers());
    }

    public Class<?> getObjectClass() {
      return _clazz;
    }

    public boolean isAbstract() {
      return _abstract;
    }

    private void setAbstract(final boolean abztract) {
      _abstract = abztract;
    }

    private void setName(final String name) {
      _name = name;
    }

    public String getName() {
      if (_name != null) {
        return _name;
      } else {
        return getObjectClass().getSimpleName();
      }
    }

    private void setCategory(final String category) {
      _category = category;
    }

    public String getCategory() {
      return _category;
    }

    private void setAttributeLabel(final String attribute, final String label) {
      final int bracket = attribute.indexOf('[');
      if (bracket > 0) {
        final String name = attribute.substring(0, bracket);
        final String[] properties = attribute.substring(bracket + 1, attribute.length() - 1).split(",\\s*");
        final String alias;
        if (properties[0].length() > 0) {
          alias = properties[0];
        } else {
          alias = name;
        }
        boolean readable = true;
        boolean writeable = true;
        if (properties.length > 1) {
          if ("r/o".equals(properties[1])) {
            writeable = false;
          } else if ("w/o".equals(properties[1])) {
            readable = false;
          } else {
            throw new OpenGammaRuntimeException("Bad attribute property '" + attribute + "'");
          }
        }
        _attributeInfo.put(name, new AttributeInfo(attribute, label, readable, writeable, alias));
      } else {
        _attributeInfo.put(attribute, new AttributeInfo(attribute, label));
      }
    }

    public AttributeInfo getDirectAttribute(final String attribute) {
      return _attributeInfo.get(attribute);
    }

    public Set<String> getDirectAttributes() {
      return _attributeInfo.keySet();
    }

    public AttributeInfo getInheritedAttribute(final String attribute) {
      final AttributeInfo info = getDirectAttribute(attribute);
      if ((info == null) && (getSuperclass() != null)) {
        return getSuperclass().getInheritedAttribute(attribute);
      }
      return info;
    }

    public Set<String> getAttributes() {
      return _attributeInfo.keySet();
    }

    private void setLabel(final String label) {
      _label = label;
    }

    public String getLabel() {
      return _label;
    }

    private void setConstructorDescription(final String constructorDescription) {
      _constructorDescription = constructorDescription;
    }

    public String getConstructorDescription() {
      if (_constructorDescription == null) {
        if (getLabel() != null) {
          return CONSTRUCTOR_DESCRIPTION_PREFIX + getLabel();
        } else {
          return null;
        }
      } else {
        return _constructorDescription;
      }
    }

    private void setConstructorParameters(final String[] constructorParameters) {
      _constructorParameters = constructorParameters;
    }

    public String[] getConstructorParameters() {
      return _constructorParameters;
    }

    private void setSuperclass(final ObjectInfo superclass) {
      _superclass = superclass;
    }

    public ObjectInfo getSuperclass() {
      return _superclass;
    }

    public String getParameterDescription(final String parameter) {
      return getInheritedAttribute(parameter).getDescription();
    }

    @Override
    public String toString() {
      final String str = "ObjectInfo[" + getObjectClass().getName() + "]";
      if (getSuperclass() != null) {
        return str + "<-" + getSuperclass().toString();
      } else {
        return str;
      }
    }

  }

  protected ResourceBundle getResourceBundle() {
    return ResourceBundle.getBundle(ObjectFunctionProvider.class.getName());
  }

  protected String capitalize(final String str) {
    return StringUtils.capitalize(str);
  }

  protected String uncapitalize(final String str) {
    if (str.length() > 1) {
      if (Character.isUpperCase(str.charAt(0)) && Character.isUpperCase(str.charAt(1))) {
        return str;
      }
    }
    return StringUtils.uncapitalize(str);
  }

  @SuppressWarnings("unchecked")
  protected MetaFunction getCreateSecurityInstance(final ObjectInfo object) {
    final String[] parameterNames = object.getConstructorParameters();
    final String[] parameterDescriptions = new String[parameterNames.length];
    for (int i = 0; i < parameterNames.length; i++) {
      try {
        parameterDescriptions[i] = object.getParameterDescription(parameterNames[i]);
      } catch (NullPointerException npe) {
        s_logger.warn("Error processing parameter {} of {}", parameterNames[i], object);
        throw npe;
      }
    }
    final MetaFunction function = new CreateSecurityFunction(object.getObjectClass(), object.getConstructorDescription(), parameterNames, parameterDescriptions).getMetaFunction();
    function.setCategory(Categories.SECURITY);
    return function;
  }

  protected MetaFunction getObjectValuesInstance(final ObjectInfo object, final String category) {
    // TODO: the string constants here should be at the top of the file
    final Class<?> clazz = object.getObjectClass();
    final String name = "Expand" + object.getName();
    final String description = "Expand the contents of " + object.getLabel();
    final MetaParameter target = new MetaParameter(uncapitalize(object.getName()), JavaTypeInfo.builder(clazz).get());
    target.setDescription(capitalize(object.getLabel()) + " to query");
    final Map<String, Method> readers = new HashMap<String, Method>();
    for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clazz)) {
      AttributeInfo info = object.getInheritedAttribute(prop.getName());
      if ((info == null) || !info.isReadable()) {
        continue;
      }
      final Method read = PropertyUtils.getReadMethod(prop);
      if (read != null) {
        readers.put(info.getLabel(), read);
      }
    }
    if (readers.isEmpty()) {
      return null;
    } else {
      return new ObjectValuesFunction(category, name, description, readers, target).getMetaFunction();
    }
  }

  protected void loadManageableSecurityDefinitions(final ObjectInfo object, final Collection<MetaFunction> definitions) {
    if (!object.isAbstract() && (object.getConstructorDescription() != null)) {
      definitions.add(getCreateSecurityInstance(object));
    }
    if (object.getLabel() != null) {
      final MetaFunction func = getObjectValuesInstance(object, Categories.SECURITY);
      if (func != null) {
        definitions.add(func);
      }
    }
    loadGetAndSet(object, definitions, Categories.SECURITY);
  }

  @SuppressWarnings("unchecked")
  protected MetaFunction getCreateObjectInstance(final ObjectInfo object, final String category) {
    final String[] parameterNames = object.getConstructorParameters();
    final String[] parameterDescriptions = new String[parameterNames.length];
    for (int i = 0; i < parameterNames.length; i++) {
      parameterDescriptions[i] = object.getParameterDescription(parameterNames[i]);
    }
    return new CreateObjectFunction(category, object.getName(), object.getObjectClass(), object.getConstructorDescription(), parameterNames, parameterDescriptions).getMetaFunction();
  }

  protected void loadObjectDefinitions(final ObjectInfo object, final Collection<MetaFunction> definitions, final String category) {
    if (!object.isAbstract() && (object.getConstructorDescription() != null)) {
      definitions.add(getCreateObjectInstance(object, category));
    }
    if (object.getLabel() != null) {
      final MetaFunction func = getObjectValuesInstance(object, category);
      if (func != null) {
        definitions.add(func);
      }
    }
    loadGetAndSet(object, definitions, category);
  }

  protected MetaFunction getGetAttributeInstance(final ObjectInfo object, final AttributeInfo attribute, final Method read, final String category) {
    // TODO: the string constants here should be at the top of the file
    final String name = "Get" + object.getName() + capitalize(attribute.getAlias());
    final String description = "Returns the " + attribute.getLabel() + " from " + object.getLabel();
    final MetaParameter target = new MetaParameter(uncapitalize(object.getName()), JavaTypeInfo.builder(object.getObjectClass()).get());
    target.setDescription(capitalize(object.getLabel()) + " to query");
    return new GetAttributeFunction(category, name, description, read, target).getMetaFunction();
  }

  protected MetaFunction getSetAttributeInstance(final ObjectInfo object, final AttributeInfo attribute, final Method write, final String category) {
    // TODO: the string constants here should be at the top of the file
    final String name = "Set" + object.getName() + capitalize(attribute.getAlias());
    final String description = "Updates the " + attribute.getLabel() + " of " + object.getLabel() +
        ". The original object is unchanged - a new object is returned with the updated value";
    final MetaParameter target = new MetaParameter(uncapitalize(object.getName()), JavaTypeInfo.builder(object.getObjectClass()).get());
    target.setDescription(capitalize(object.getLabel()) + " to update");
    final MetaParameter value = new MetaParameter(attribute.getName(), JavaTypeInfo.builder(write.getParameterTypes()[0]).allowNull().get());
    value.setDescription(attribute.getDescription());
    return new SetAttributeFunction(category, name, description, write, target, value).getMetaFunction();
  }

  protected void loadObjectGetter(final ObjectInfo object, final AttributeInfo attribute, final Method read, final Collection<MetaFunction> definitions, final String category) {
    definitions.add(getGetAttributeInstance(object, attribute, read, category));
  }

  protected void loadObjectSetter(final ObjectInfo object, final AttributeInfo attribute, final Method write, final Collection<MetaFunction> definitions, final String category) {
    definitions.add(getSetAttributeInstance(object, attribute, write, category));
  }

  protected void loadGetAndSet(final ObjectInfo object, final Collection<MetaFunction> definitions, final String category) {
    if (object.getLabel() != null) {
      final Class<?> clazz = object.getObjectClass();
      final Set<String> attributes = new HashSet<String>(object.getDirectAttributes());
      for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clazz)) {
        final AttributeInfo attribute = object.getDirectAttribute(prop.getName());
        if (attribute != null) {
          if (attribute.isReadable()) {
            final Method read = PropertyUtils.getReadMethod(prop);
            if (read != null) {
              loadObjectGetter(object, attribute, read, definitions, category);
              attributes.remove(prop.getName());
            }
          }
          if (attribute.isWriteable()) {
            final Method write = PropertyUtils.getWriteMethod(prop);
            if (write != null) {
              loadObjectSetter(object, attribute, write, definitions, category);
              attributes.remove(prop.getName());
            }
          }
        }
      }
      if (!attributes.isEmpty()) {
        for (String attribute : attributes) {
          throw new OpenGammaRuntimeException("Attribute " + attribute + " is not exposed on object " + object);
        }
      }
    }
  }

  protected String categoriseObject(final Class<?> clazz) {
    final String n = clazz.getName();
    if (n.startsWith("com.opengamma.financial.security.")) {
      return Categories.SECURITY;
    }
    if (n.startsWith("com.opengamma.analytics.math.curve.")) {
      return Categories.CURVE;
    }
    if (n.startsWith("com.opengamma.core.marketdatasnapshot.")) {
      return Categories.MARKET_DATA;
    }
    if (n.startsWith("com.opengamma.core.position.")) {
      return Categories.POSITION;
    }
    return null;
  }

  protected void loadDefinitions(final ObjectInfo object, final Collection<MetaFunction> definitions) {
    s_logger.debug("Loading definitions for {}", object);
    if (ManageableSecurity.class.isAssignableFrom(object.getObjectClass())) {
      loadManageableSecurityDefinitions(object, definitions);
    } else {
      if (object.getCategory() != null) {
        loadObjectDefinitions(object, definitions, object.getCategory());
      } else {
        loadObjectDefinitions(object, definitions, categoriseObject(object.getObjectClass()));
      }
    }
  }

  protected void loadDefinitions(final Collection<ObjectInfo> objects, final Collection<MetaFunction> definitions) {
    for (ObjectInfo object : objects) {
      loadDefinitions(object, definitions);
    }
  }

  // AbstractFunctionProvider

  @Override
  protected final void loadDefinitions(final Collection<MetaFunction> definitions) {
    s_logger.info("Starting loadDefinitions with {} in collection", definitions.size());
    final Map<String, ObjectInfo> functions = new HashMap<String, ObjectInfo>();
    final ResourceBundle mapping = getResourceBundle();
    for (final String key : mapping.keySet()) {
      int dot = key.lastIndexOf('.');
      final String clazz = key.substring(0, dot);
      final String attribute = key.substring(dot + 1);
      final String value = mapping.getString(key);
      ObjectInfo func = functions.get(clazz);
      if (func == null) {
        try {
          func = new ObjectInfo(Class.forName(clazz));
        } catch (Throwable t) {
          throw new OpenGammaRuntimeException("Class '" + clazz + "' not available for function definition", t);
        }
        functions.put(clazz, func);
      }
      if ("_abstract".equals(attribute)) {
        func.setAbstract("true".equalsIgnoreCase(value));
      } else if ("_description".equals(attribute)) {
        func.setConstructorDescription(value);
      } else if ("_label".equals(attribute)) {
        func.setLabel(value);
      } else if ("_name".equals(attribute)) {
        func.setName(value);
      } else if ("_category".equals(attribute)) {
        func.setCategory(value);
      } else if ("_parameters".equals(attribute)) {
        func.setConstructorParameters(value.split(",\\s*"));
      } else {
        func.setAttributeLabel(attribute, value);
      }
    }
    final Collection<ObjectInfo> objects = functions.values();
    for (ObjectInfo object : objects) {
      Class<?> superClazz = object.getObjectClass().getSuperclass();
      while ((superClazz != null) && !Object.class.equals(superClazz)) {
        final ObjectInfo superclass = functions.get(superClazz.getName());
        if (superclass != null) {
          object.setSuperclass(superclass);
          break;
        }
        superClazz = superClazz.getSuperclass();
      }
    }
    loadDefinitions(objects, definitions);
    s_logger.debug("Finished loadDefinitions with {} definitions in collection", definitions.size());
  }

}
