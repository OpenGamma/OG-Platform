/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.object.CreateObjectFunction;
import com.opengamma.language.object.GetAttributeFunction;
import com.opengamma.language.object.ObjectValuesFunction;
import com.opengamma.language.object.SetAttributeFunction;
import com.opengamma.language.security.CreateSecurityFunction;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A {@link FunctionProvider} that builds functions based on object constructors and their accessor/mutator
 * methods. The object information is read in from a resource file. The basic OG-Language functionality
 * can be extended for a custom language binding by inheriting from this class and re-implementing the
 * {@link #loadDefinitions} method.
 */
public class ObjectFunctionProvider extends AbstractFunctionProvider {

  private static final String[] EMPTY = new String[0];

  // TODO: move these into the resource file

  private static final String CONSTRUCTOR_DESCRIPTION_PREFIX = "Defines ";
  private static final String PARAMETER_DESCRIPTION_PREFIX = "The ";

  /**
   * 
   */
  protected final class ObjectInfo {

    private final Class<?> _clazz;
    private final Map<String, String> _attributeLabel = new HashMap<String, String>();
    private String _name;
    private String _label;
    private String _constructorDescription;
    private String[] _constructorParameters = EMPTY;
    private ObjectInfo _superclass;

    private ObjectInfo(final Class<?> clazz) {
      _clazz = clazz;
    }

    public Class<?> getObjectClass() {
      return _clazz;
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

    private void setAttributeLabel(final String attribute, final String label) {
      _attributeLabel.put(attribute, label);
    }

    public String getAttributeLabel(final String attribute) {
      return _attributeLabel.get(attribute);
    }

    public String getParameterDescription(final String attribute) {
      return PARAMETER_DESCRIPTION_PREFIX + getAttributeLabel(attribute);
    }

    public Set<String> getAttributes() {
      return _attributeLabel.keySet();
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
      parameterDescriptions[i] = object.getParameterDescription(parameterNames[i]);
    }
    return new CreateSecurityFunction(object.getObjectClass(), object.getConstructorDescription(), parameterNames, parameterDescriptions).getMetaFunction();
  }

  protected MetaFunction getObjectValuesInstance(final ObjectInfo object) {
    // TODO: the string constants here should be at the top of the file
    final Class<?> clazz = object.getObjectClass();
    final String name = "Expand" + object.getName();
    final String description = "Expand the contents of " + object.getLabel();
    final MetaParameter target = new MetaParameter(uncapitalize(object.getName()), JavaTypeInfo.builder(clazz).get());
    target.setDescription(capitalize(object.getLabel()) + " to query");
    final Map<String, Method> readers = new HashMap<String, Method>();
    for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clazz)) {
      String label = object.getAttributeLabel(prop.getName());
      if (label == null) {
        ObjectInfo src = object.getSuperclass();
        while (src != null) {
          label = src.getAttributeLabel(prop.getName());
          if (label != null) {
            break;
          }
          src = src.getSuperclass();
        }
        if (label == null) {
          continue;
        }
      }
      final Method read = PropertyUtils.getReadMethod(prop);
      if (read != null) {
        readers.put(label, read);
      }
    }
    return new ObjectValuesFunction(name, description, readers, target).getMetaFunction();
  }

  protected void loadManageableSecurityDefinitions(final ObjectInfo object, final Collection<MetaFunction> definitions) {
    final Class<?> clazz = object.getObjectClass();
    if (!Modifier.isAbstract(clazz.getModifiers()) && (object.getConstructorDescription() != null)) {
      definitions.add(getCreateSecurityInstance(object));
    }
    if (object.getLabel() != null) {
      definitions.add(getObjectValuesInstance(object));
    }
    loadGetAndSet(object, definitions);
  }

  @SuppressWarnings("unchecked")
  protected MetaFunction getCreateObjectInstance(final ObjectInfo object) {
    final String[] parameterNames = object.getConstructorParameters();
    final String[] parameterDescriptions = new String[parameterNames.length];
    for (int i = 0; i < parameterNames.length; i++) {
      parameterDescriptions[i] = object.getParameterDescription(parameterNames[i]);
    }
    return new CreateObjectFunction(object.getObjectClass(), object.getConstructorDescription(), parameterNames, parameterDescriptions).getMetaFunction();
  }

  protected void loadObjectDefinitions(final ObjectInfo object, final Collection<MetaFunction> definitions) {
    final Class<?> clazz = object.getObjectClass();
    if (!Modifier.isAbstract(clazz.getModifiers()) && (object.getConstructorDescription() != null)) {
      definitions.add(getCreateObjectInstance(object));
    }
    if (object.getLabel() != null) {
      definitions.add(getObjectValuesInstance(object));
    }
    loadGetAndSet(object, definitions);
  }

  protected MetaFunction getGetAttributeInstance(final ObjectInfo object, final String attribute, final Method read) {
    // TODO: the string constants here should be at the top of the file
    final String name = "Get" + object.getName() + capitalize(attribute);
    final String description = "Returns the " + object.getAttributeLabel(attribute) + " from " + object.getLabel();
    final MetaParameter target = new MetaParameter(uncapitalize(object.getName()), JavaTypeInfo.builder(object.getObjectClass()).get());
    target.setDescription(capitalize(object.getLabel()) + " to query");
    return new GetAttributeFunction(name, description, read, target).getMetaFunction();
  }

  protected MetaFunction getSetAttributeInstance(final ObjectInfo object, final String attribute, final Method write) {
    // TODO: the string constants here should be at the top of the file
    final String name = "Set" + object.getName() + capitalize(attribute);
    final String description = "Updates the " + object.getAttributeLabel(attribute) + " of " + object.getLabel() +
        ". The original object is unchanged - a new object is returned with the updated value";
    final MetaParameter target = new MetaParameter(uncapitalize(object.getName()), JavaTypeInfo.builder(object.getObjectClass()).get());
    target.setDescription(capitalize(object.getLabel()) + " to update");
    final MetaParameter value = new MetaParameter(attribute, JavaTypeInfo.builder(write.getParameterTypes()[0]).get());
    value.setDescription(object.getParameterDescription(attribute));
    return new SetAttributeFunction(name, description, write, target, value).getMetaFunction();
  }

  protected void loadObjectGetter(final ObjectInfo object, final String attribute, final Method read, final Collection<MetaFunction> definitions) {
    definitions.add(getGetAttributeInstance(object, attribute, read));
  }

  protected void loadObjectSetter(final ObjectInfo object, final String attribute, final Method write, final Collection<MetaFunction> definitions) {
    definitions.add(getSetAttributeInstance(object, attribute, write));
  }

  protected void loadGetAndSet(final ObjectInfo object, final Collection<MetaFunction> definitions) {
    if (object.getLabel() != null) {
      final Class<?> clazz = object.getObjectClass();
      final PropertyDescriptor[] superclassPropArray = PropertyUtils.getPropertyDescriptors(clazz.getSuperclass());
      final Set<String> superclassPropSet = Sets.newHashSetWithExpectedSize(superclassPropArray.length);
      for (PropertyDescriptor superclassProp : superclassPropArray) {
        superclassPropSet.add(superclassProp.getName());
      }
      for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clazz)) {
        if (object.getAttributeLabel(prop.getName()) != null) {
          final Method read = PropertyUtils.getReadMethod(prop);
          if (read != null) {
            loadObjectGetter(object, prop.getName(), read, definitions);
          }
          final Method write = PropertyUtils.getWriteMethod(prop);
          if (write != null) {
            loadObjectSetter(object, prop.getName(), write, definitions);
          }
        }
      }
    }
  }

  protected void loadDefinitions(final ObjectInfo object, final Collection<MetaFunction> definitions) {
    if (ManageableSecurity.class.isAssignableFrom(object.getObjectClass())) {
      loadManageableSecurityDefinitions(object, definitions);
    } else {
      loadObjectDefinitions(object, definitions);
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
      if ("_description".equals(attribute)) {
        func.setConstructorDescription(value);
      } else if ("_label".equals(attribute)) {
        func.setLabel(value);
      } else if ("_name".equals(attribute)) {
        func.setName(value);
      } else if ("_parameters".equals(attribute)) {
        func.setConstructorParameters(value.split(",\\s*"));
      } else {
        //System.out.println(clazz + "; " + attribute + "; " + value);
        func.setAttributeLabel(attribute, value);
      }
    }
    final Collection<ObjectInfo> objects = functions.values();
    for (ObjectInfo object : objects) {
      final Class<?> superClazz = object.getObjectClass().getSuperclass();
      final ObjectInfo superclass = functions.get(superClazz.getName());
      if (superclass != null) {
        object.setSuperclass(superclass);
      }
    }
    loadDefinitions(objects, definitions);
  }

}
