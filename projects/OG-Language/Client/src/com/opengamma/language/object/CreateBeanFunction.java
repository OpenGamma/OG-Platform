package com.opengamma.language.object;

import com.opengamma.id.UniqueId;
import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO export documentation?
 */
public class CreateBeanFunction<T extends Bean> implements PublishedFunction {

  private final String _functionName;
  private final MetaBean _metaBean;

  /*public CreateBeanFunction(Class<T> beanClass) {
    this(beanClass.getSimpleName(), beanClass);
  }*/

  public CreateBeanFunction(String functionName, Class<T> beanClass) {
    _functionName = functionName;
    _metaBean = JodaBeanUtils.metaBean(beanClass);
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> metaParameters = new ArrayList<MetaParameter>();
    for (MetaProperty<Object> metaProperty : _metaBean.metaPropertyIterable()) {
      Class<Object> propertyType = metaProperty.propertyType();
      // it makes no sense for the user to specify the unique ID in the function
      if (!UniqueId.class.equals(propertyType)) {
        JavaTypeInfo<?> typeInfo = JavaTypeInfo.builder(propertyType).get();
        metaParameters.add(new MetaParameter(metaProperty.name(), typeInfo));
      }
    }
    return new MetaFunction(_functionName, metaParameters, new Invoker(metaParameters));
  }

  /**
   * Package scoped for easier testing - the test can use {@link #invokeImpl(SessionContext, Object[])} instead
   * of {@link #invoke(SessionContext, List)} and doesn't have to convert the parameters to {@link Data}.
   */
  /* package */class Invoker extends AbstractFunctionInvoker {

    private final List<MetaParameter> _metaParameters;

    public Invoker(List<MetaParameter> metaParameters) {
      super(metaParameters);
      _metaParameters = metaParameters;
    }

    @Override
    protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
      ArgumentChecker.notNull(parameters, "parameters");
      if (parameters.length > _metaParameters.size()) {
        throw new IllegalArgumentException("Too many parameters received: " + parameters.length + ", expected: " +
                                               _metaParameters.size());
      }
      BeanBuilder<? extends Bean> builder = _metaBean.builder();
      int i = 0;
      for (Object parameter : parameters) {
        MetaParameter metaParameter = _metaParameters.get(i++);
        builder.set(metaParameter.getName(), parameter);
      }
      return builder.build();
    }
  }
}
