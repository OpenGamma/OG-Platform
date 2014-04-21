/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZoneId;

import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.PublicSPI;

/**
 * Provides a form bean suitable for creating or amending a {@code ManageableUser}.
 */
@PublicSPI
@BeanDefinition
public class UserForm implements Bean {

  /**
   * Valid name regex.
   */
  public static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");
  /**
   * Valid name regex.
   */
  public static final Pattern VALID_PERMISSION = Pattern.compile("[a-zA-Z*][a-zA-Z0-9*_-]*(:[a-zA-Z*][a-zA-Z0-9*_,-]*)*");

  /**
   * The user name that uniquely identifies the user.
   * This is used with the password to authenticate.
   */
  @PropertyDefinition
  private String _userName;
  /**
   * The plain text version of the user password.
   */
  @PropertyDefinition
  private String _passwordRaw;
  /**
   * The primary email address associated with this user.
   */
  @PropertyDefinition
  private String _emailAddress;
  /**
   * The display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   */
  @PropertyDefinition
  private String _displayName;
  /**
   * The locale that the user prefers.
   */
  @PropertyDefinition
  private String _locale;
  /**
   * The time-zone used to display local times.
   */
  @PropertyDefinition
  private String _zone;
  /**
   * The date style that the user prefers.
   */
  @PropertyDefinition
  private String _dateStyle;
  /**
   * The time style that the user prefers.
   */
  @PropertyDefinition
  private String _timeStyle;

  /**
   * The base user, allowing other properties to be set.
   */
  @PropertyDefinition
  private ManageableUser _baseUser;

  /**
   * Creates a form object.
   */
  public UserForm() {
  }

  /**
   * Creates a form object.
   * 
   * @param user  the user to copy from, not null
   */
  public UserForm(ManageableUser user) {
    setUserName(user.getUserName());
    setEmailAddress(user.getEmailAddress());
    setDisplayName(user.getProfile().getDisplayName());
    setLocale(user.getProfile().getLocale().toString());
    setZone(user.getProfile().getZone().toString());
    setDateStyle(user.getProfile().getDateStyle().toString());
    setTimeStyle(user.getProfile().getTimeStyle().toString());
    setBaseUser(user);
  }

  /**
   * Creates a form object for changing the password.
   * 
   * @param user  the user to copy from, not null
   * @param passwordRaw  the new password
   */
  public UserForm(ManageableUser user, String passwordRaw) {
    setUserName(user.getUserName());
    setPasswordRaw(passwordRaw);
    setEmailAddress(user.getEmailAddress());
    setDisplayName(user.getProfile().getDisplayName());
    setLocale(user.getProfile().getLocale().toString());
    setZone(user.getProfile().getZone().toString());
    setDateStyle(user.getProfile().getDateStyle().toString());
    setTimeStyle(user.getProfile().getTimeStyle().toString());
    setBaseUser(user);
    getBaseUser().setPasswordHash(null);
  }

  /**
   * Creates a form object for changing everything except the name and password.
   * 
   * @param user  the user to copy from, not null
   * @param emailAddress  the email address, not null
   * @param displayName  the display name, not null
   * @param localeStr  the locale, not null
   * @param zoneStr  the time zone, not null
   * @param dateStyleStr  the date style, not null
   * @param timeStyleStr  the time style, not null
   */
  public UserForm(ManageableUser user, String emailAddress, String displayName,
      String localeStr, String zoneStr, String dateStyleStr, String timeStyleStr) {
    setUserName(user.getUserName());
    setPasswordRaw(null);
    setEmailAddress(emailAddress);
    setDisplayName(displayName);
    setLocale(localeStr);
    setZone(zoneStr);
    setDateStyle(dateStyleStr);
    setTimeStyle(timeStyleStr);
    setBaseUser(user);
  }

  /**
   * Creates a form object.
   * 
   * @param userName  the user name, not null
   * @param password  the plain text password, not null
   * @param emailAddress  the email address, not null
   * @param displayName  the display name, not null
   * @param localeStr  the locale, not null
   * @param zoneStr  the time zone, not null
   * @param dateStyleStr  the date style, not null
   * @param timeStyleStr  the time style, not null
   */
  public UserForm(String userName, String password, String emailAddress, String displayName,
      String localeStr, String zoneStr, String dateStyleStr, String timeStyleStr) {
    setUserName(userName);
    setPasswordRaw(password);
    setEmailAddress(emailAddress);
    setDisplayName(displayName);
    setLocale(localeStr);
    setZone(zoneStr);
    setDateStyle(dateStyleStr);
    setTimeStyle(timeStyleStr);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates and adds the proposed user to the master.
   * 
   * @param userMaster  the user master, not null
   * @param pwService  the password service
   * @return the added user
   * @throws UserFormException if the proposed user is invalid
   */
  public ManageableUser add(UserMaster userMaster, PasswordService pwService) {
    try {
      ManageableUser user = validate(userMaster, pwService, true);
      UniqueId uid = userMaster.add(user);
      user.setUniqueId(uid);
      return user;
    } catch (UserFormException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new UserFormException(ex);
    }
  }

  /**
   * Validates and updates the proposed user in the master.
   * 
   * @param userMaster  the user master, not null
   * @param pwService  the password service
   * @return the added user
   * @throws UserFormException if the proposed user is invalid
   */
  public ManageableUser update(UserMaster userMaster, PasswordService pwService) {
    try {
      ManageableUser user = validate(userMaster, pwService, false);
      UniqueId uid = userMaster.update(user);
      user.setUniqueId(uid);
      return user;
    } catch (UserFormException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new UserFormException(ex);
    }
  }

  /**
   * Validates and adds the proposed user to the master.
   * 
   * @param userMaster  the user master, not null
   * @param pwService  the password service
   * @param add  true if adding, false if updating
   * @return the added user
   * @throws UserFormException if the proposed user is invalid
   */
  protected ManageableUser validate(UserMaster userMaster, PasswordService pwService, boolean add) {
    userMaster = ArgumentChecker.notNull(userMaster, "userMaster");
    pwService = ArgumentChecker.notNull(pwService, "pwService");
    String userName = StringUtils.trimToNull(getUserName());
    String password = StringUtils.trimToNull(getPasswordRaw());
    String email = StringUtils.trimToNull(getEmailAddress());
    String displayName = StringUtils.trimToNull(getDisplayName());
    String localeStr = StringUtils.trimToNull(getLocale());
    String zoneStr = StringUtils.trimToNull(getZone());
    String dateStyleStr = StringUtils.trimToNull(getDateStyle());
    String timeStyleStr = StringUtils.trimToNull(getTimeStyle());
    List<UserFormError> errors = new ArrayList<>();
    // user name
    if (userName == null) {
      if (getBaseUser() != null) {
        userName = getBaseUser().getUserName();
      }
      if (userName == null) {
        errors.add(UserFormError.USERNAME_MISSING);
      }
    } else if (isUserNameTooShort(userName)) {
      errors.add(UserFormError.USERNAME_TOO_SHORT);
    } else if (isUserNameTooLong(userName)) {
      errors.add(UserFormError.USERNAME_TOO_LONG);
    } else if (isUserNameInvalid(userName)) {
      errors.add(UserFormError.USERNAME_INVALID);
    } else {
      if (add && userMaster.nameExists(userName)) {
        errors.add(UserFormError.USERNAME_ALREADY_IN_USE);
      }
    }
    // password
    String passwordHash = null;
    if (password == null) {
      if (getBaseUser() != null) {
        passwordHash = getBaseUser().getPasswordHash();
      }
      if (passwordHash == null) {
        errors.add(UserFormError.PASSWORD_MISSING);
      }
    } else if (isPasswordTooShort(password)) {
      errors.add(UserFormError.PASSWORD_TOO_SHORT);
    } else if (isPasswordTooLong(password)) {
      errors.add(UserFormError.PASSWORD_TOO_LONG);
    } else if (isPasswordWeak(userName, password)) {
      errors.add(UserFormError.PASSWORD_WEAK);
    } else {
      passwordHash = pwService.encryptPassword(password);
    }
    // email
    if (email == null) {
      errors.add(UserFormError.EMAIL_MISSING);
    } else if (isEmailAddressTooLong(email)) {
      errors.add(UserFormError.EMAIL_TOO_LONG);
    } else if (isEmailAddressInvalid(email)) {
      errors.add(UserFormError.EMAIL_INVALID);
    }
    // display name
    if (displayName == null) {
      errors.add(UserFormError.DISPLAYNAME_MISSING);
    } else if (isDisplayNameTooLong(displayName)) {
      errors.add(UserFormError.DISPLAYNAME_TOO_LONG);
    } else if (isDisplayNameInvalid(displayName)) {
      errors.add(UserFormError.DISPLAYNAME_INVALID);
    }
    // locale
    Locale locale = Locale.ENGLISH;
    if (localeStr != null) {
      try {
        locale = LocaleUtils.toLocale(localeStr);
      } catch (RuntimeException ex) {
        errors.add(UserFormError.LOCALE_INVALID);
      }
    }
    // time zone
    ZoneId zoneId = OpenGammaClock.getZone();
    if (zoneStr != null) {
      try {
        zoneId = ZoneId.of(zoneStr);
      } catch (RuntimeException ex) {
        errors.add(UserFormError.TIMEZONE_INVALID);
      }
    }
    // date style
    DateStyle dateStyle = DateStyle.TEXTUAL_MONTH;
    if (dateStyleStr != null) {
      try {
        dateStyle = DateStyle.valueOf(dateStyleStr);
      } catch (RuntimeException ex) {
        errors.add(UserFormError.DATESTYLE_INVALID);
      }
    }
    // time style
    TimeStyle timeStyle = TimeStyle.ISO;
    if (timeStyleStr != null) {
      try {
        timeStyle = TimeStyle.valueOf(timeStyleStr);
      } catch (RuntimeException ex) {
        errors.add(UserFormError.TIMESTYLE_INVALID);
      }
    }
    // errors
    if (errors.size() > 0) {
      throw new UserFormException(errors);
    }
    // build user object
    ManageableUser user = getBaseUser();
    if (user == null) {
      user = new ManageableUser(userName);
    } else {
      user.setUserName(userName);
    }
    user.setPasswordHash(passwordHash);
    user.setEmailAddress(email);
    user.getProfile().setDisplayName(displayName);
    user.getProfile().setLocale(locale);
    user.getProfile().setZone(zoneId);
    user.getProfile().setDateStyle(dateStyle);
    user.getProfile().setTimeStyle(timeStyle);
    return user;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the user name is too short.
   * 
   * @param userName  the user name, not null
   * @return true if short
   */
  protected boolean isUserNameTooShort(String userName) {
    return userName.length() < 5;
  }

  /**
   * Checks if the user name is too long.
   * 
   * @param userName  the user name, not null
   * @return true if long
   */
  protected boolean isUserNameTooLong(String userName) {
    return userName.length() > 20;
  }

  /**
   * Checks if the user name is invalid.
   * 
   * @param userName  the user name, not null
   * @return true if invalid
   */
  protected boolean isUserNameInvalid(String userName) {
    return VALID_NAME.matcher(userName).matches() == false;
  }

  /**
   * Checks if the password is too short.
   * 
   * @param password  the password, not null
   * @return true if short
   */
  protected boolean isPasswordTooShort(String password) {
    return password.length() < 6;
  }

  /**
   * Checks if the password is too long.
   * 
   * @param password  the password, not null
   * @return true if long
   */
  protected boolean isPasswordTooLong(String password) {
    return password.length() > 100;
  }

  /**
   * Checks if the password is weak.
   * 
   * @param userName  the user name, not null
   * @param password  the password, not null
   * @return true if weak
   */
  protected boolean isPasswordWeak(String userName, String password) {
    userName = userName.toLowerCase(Locale.ENGLISH);
    password = password.toLowerCase(Locale.ENGLISH);
    return userName.equals(password) || password.equals("password");
  }

  /**
   * Checks if the email address is too long.
   * 
   * @param emailAddress  the email address, not null
   * @return true if long
   */
  protected boolean isEmailAddressTooLong(String emailAddress) {
    return emailAddress.length() > 200;
  }

  /**
   * Checks if the email address is invalid.
   * 
   * @param email  the email address, not null
   * @return true if invalid
   */
  protected boolean isEmailAddressInvalid(String email) {
    return email.contains("@") == false;
  }

  /**
   * Checks if the display name is too long.
   * 
   * @param displayName  the display name, not null
   * @return true if invalid
   */
  protected boolean isDisplayNameTooLong(String displayName) {
    return displayName.length() > 200;
  }

  /**
   * Checks if the display name is invalid.
   * 
   * @param displayName  the display name, not null
   * @return true if invalid
   */
  protected boolean isDisplayNameInvalid(String displayName) {
    return false;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserForm}.
   * @return the meta-bean, not null
   */
  public static UserForm.Meta meta() {
    return UserForm.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserForm.Meta.INSTANCE);
  }

  @Override
  public UserForm.Meta metaBean() {
    return UserForm.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user name that uniquely identifies the user.
   * This is used with the password to authenticate.
   * @return the value of the property
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Sets the user name that uniquely identifies the user.
   * This is used with the password to authenticate.
   * @param userName  the new value of the property
   */
  public void setUserName(String userName) {
    this._userName = userName;
  }

  /**
   * Gets the the {@code userName} property.
   * This is used with the password to authenticate.
   * @return the property, not null
   */
  public final Property<String> userName() {
    return metaBean().userName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the plain text version of the user password.
   * @return the value of the property
   */
  public String getPasswordRaw() {
    return _passwordRaw;
  }

  /**
   * Sets the plain text version of the user password.
   * @param passwordRaw  the new value of the property
   */
  public void setPasswordRaw(String passwordRaw) {
    this._passwordRaw = passwordRaw;
  }

  /**
   * Gets the the {@code passwordRaw} property.
   * @return the property, not null
   */
  public final Property<String> passwordRaw() {
    return metaBean().passwordRaw().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the primary email address associated with this user.
   * @return the value of the property
   */
  public String getEmailAddress() {
    return _emailAddress;
  }

  /**
   * Sets the primary email address associated with this user.
   * @param emailAddress  the new value of the property
   */
  public void setEmailAddress(String emailAddress) {
    this._emailAddress = emailAddress;
  }

  /**
   * Gets the the {@code emailAddress} property.
   * @return the property, not null
   */
  public final Property<String> emailAddress() {
    return metaBean().emailAddress().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * @return the value of the property
   */
  public String getDisplayName() {
    return _displayName;
  }

  /**
   * Sets the display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * @param displayName  the new value of the property
   */
  public void setDisplayName(String displayName) {
    this._displayName = displayName;
  }

  /**
   * Gets the the {@code displayName} property.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * @return the property, not null
   */
  public final Property<String> displayName() {
    return metaBean().displayName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the locale that the user prefers.
   * @return the value of the property
   */
  public String getLocale() {
    return _locale;
  }

  /**
   * Sets the locale that the user prefers.
   * @param locale  the new value of the property
   */
  public void setLocale(String locale) {
    this._locale = locale;
  }

  /**
   * Gets the the {@code locale} property.
   * @return the property, not null
   */
  public final Property<String> locale() {
    return metaBean().locale().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-zone used to display local times.
   * @return the value of the property
   */
  public String getZone() {
    return _zone;
  }

  /**
   * Sets the time-zone used to display local times.
   * @param zone  the new value of the property
   */
  public void setZone(String zone) {
    this._zone = zone;
  }

  /**
   * Gets the the {@code zone} property.
   * @return the property, not null
   */
  public final Property<String> zone() {
    return metaBean().zone().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date style that the user prefers.
   * @return the value of the property
   */
  public String getDateStyle() {
    return _dateStyle;
  }

  /**
   * Sets the date style that the user prefers.
   * @param dateStyle  the new value of the property
   */
  public void setDateStyle(String dateStyle) {
    this._dateStyle = dateStyle;
  }

  /**
   * Gets the the {@code dateStyle} property.
   * @return the property, not null
   */
  public final Property<String> dateStyle() {
    return metaBean().dateStyle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time style that the user prefers.
   * @return the value of the property
   */
  public String getTimeStyle() {
    return _timeStyle;
  }

  /**
   * Sets the time style that the user prefers.
   * @param timeStyle  the new value of the property
   */
  public void setTimeStyle(String timeStyle) {
    this._timeStyle = timeStyle;
  }

  /**
   * Gets the the {@code timeStyle} property.
   * @return the property, not null
   */
  public final Property<String> timeStyle() {
    return metaBean().timeStyle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base user, allowing other properties to be set.
   * @return the value of the property
   */
  public ManageableUser getBaseUser() {
    return _baseUser;
  }

  /**
   * Sets the base user, allowing other properties to be set.
   * @param baseUser  the new value of the property
   */
  public void setBaseUser(ManageableUser baseUser) {
    this._baseUser = baseUser;
  }

  /**
   * Gets the the {@code baseUser} property.
   * @return the property, not null
   */
  public final Property<ManageableUser> baseUser() {
    return metaBean().baseUser().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserForm clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserForm other = (UserForm) obj;
      return JodaBeanUtils.equal(getUserName(), other.getUserName()) &&
          JodaBeanUtils.equal(getPasswordRaw(), other.getPasswordRaw()) &&
          JodaBeanUtils.equal(getEmailAddress(), other.getEmailAddress()) &&
          JodaBeanUtils.equal(getDisplayName(), other.getDisplayName()) &&
          JodaBeanUtils.equal(getLocale(), other.getLocale()) &&
          JodaBeanUtils.equal(getZone(), other.getZone()) &&
          JodaBeanUtils.equal(getDateStyle(), other.getDateStyle()) &&
          JodaBeanUtils.equal(getTimeStyle(), other.getTimeStyle()) &&
          JodaBeanUtils.equal(getBaseUser(), other.getBaseUser());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPasswordRaw());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEmailAddress());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDisplayName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLocale());
    hash += hash * 31 + JodaBeanUtils.hashCode(getZone());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDateStyle());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTimeStyle());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBaseUser());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("UserForm{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("userName").append('=').append(JodaBeanUtils.toString(getUserName())).append(',').append(' ');
    buf.append("passwordRaw").append('=').append(JodaBeanUtils.toString(getPasswordRaw())).append(',').append(' ');
    buf.append("emailAddress").append('=').append(JodaBeanUtils.toString(getEmailAddress())).append(',').append(' ');
    buf.append("displayName").append('=').append(JodaBeanUtils.toString(getDisplayName())).append(',').append(' ');
    buf.append("locale").append('=').append(JodaBeanUtils.toString(getLocale())).append(',').append(' ');
    buf.append("zone").append('=').append(JodaBeanUtils.toString(getZone())).append(',').append(' ');
    buf.append("dateStyle").append('=').append(JodaBeanUtils.toString(getDateStyle())).append(',').append(' ');
    buf.append("timeStyle").append('=').append(JodaBeanUtils.toString(getTimeStyle())).append(',').append(' ');
    buf.append("baseUser").append('=').append(JodaBeanUtils.toString(getBaseUser())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserForm}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code userName} property.
     */
    private final MetaProperty<String> _userName = DirectMetaProperty.ofReadWrite(
        this, "userName", UserForm.class, String.class);
    /**
     * The meta-property for the {@code passwordRaw} property.
     */
    private final MetaProperty<String> _passwordRaw = DirectMetaProperty.ofReadWrite(
        this, "passwordRaw", UserForm.class, String.class);
    /**
     * The meta-property for the {@code emailAddress} property.
     */
    private final MetaProperty<String> _emailAddress = DirectMetaProperty.ofReadWrite(
        this, "emailAddress", UserForm.class, String.class);
    /**
     * The meta-property for the {@code displayName} property.
     */
    private final MetaProperty<String> _displayName = DirectMetaProperty.ofReadWrite(
        this, "displayName", UserForm.class, String.class);
    /**
     * The meta-property for the {@code locale} property.
     */
    private final MetaProperty<String> _locale = DirectMetaProperty.ofReadWrite(
        this, "locale", UserForm.class, String.class);
    /**
     * The meta-property for the {@code zone} property.
     */
    private final MetaProperty<String> _zone = DirectMetaProperty.ofReadWrite(
        this, "zone", UserForm.class, String.class);
    /**
     * The meta-property for the {@code dateStyle} property.
     */
    private final MetaProperty<String> _dateStyle = DirectMetaProperty.ofReadWrite(
        this, "dateStyle", UserForm.class, String.class);
    /**
     * The meta-property for the {@code timeStyle} property.
     */
    private final MetaProperty<String> _timeStyle = DirectMetaProperty.ofReadWrite(
        this, "timeStyle", UserForm.class, String.class);
    /**
     * The meta-property for the {@code baseUser} property.
     */
    private final MetaProperty<ManageableUser> _baseUser = DirectMetaProperty.ofReadWrite(
        this, "baseUser", UserForm.class, ManageableUser.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "userName",
        "passwordRaw",
        "emailAddress",
        "displayName",
        "locale",
        "zone",
        "dateStyle",
        "timeStyle",
        "baseUser");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -266666762:  // userName
          return _userName;
        case 1403763597:  // passwordRaw
          return _passwordRaw;
        case -1070931784:  // emailAddress
          return _emailAddress;
        case 1714148973:  // displayName
          return _displayName;
        case -1097462182:  // locale
          return _locale;
        case 3744684:  // zone
          return _zone;
        case -259925341:  // dateStyle
          return _dateStyle;
        case 25596644:  // timeStyle
          return _timeStyle;
        case -1721461188:  // baseUser
          return _baseUser;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserForm> builder() {
      return new DirectBeanBuilder<UserForm>(new UserForm());
    }

    @Override
    public Class<? extends UserForm> beanType() {
      return UserForm.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code userName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userName() {
      return _userName;
    }

    /**
     * The meta-property for the {@code passwordRaw} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> passwordRaw() {
      return _passwordRaw;
    }

    /**
     * The meta-property for the {@code emailAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> emailAddress() {
      return _emailAddress;
    }

    /**
     * The meta-property for the {@code displayName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> displayName() {
      return _displayName;
    }

    /**
     * The meta-property for the {@code locale} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> locale() {
      return _locale;
    }

    /**
     * The meta-property for the {@code zone} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> zone() {
      return _zone;
    }

    /**
     * The meta-property for the {@code dateStyle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dateStyle() {
      return _dateStyle;
    }

    /**
     * The meta-property for the {@code timeStyle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> timeStyle() {
      return _timeStyle;
    }

    /**
     * The meta-property for the {@code baseUser} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageableUser> baseUser() {
      return _baseUser;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -266666762:  // userName
          return ((UserForm) bean).getUserName();
        case 1403763597:  // passwordRaw
          return ((UserForm) bean).getPasswordRaw();
        case -1070931784:  // emailAddress
          return ((UserForm) bean).getEmailAddress();
        case 1714148973:  // displayName
          return ((UserForm) bean).getDisplayName();
        case -1097462182:  // locale
          return ((UserForm) bean).getLocale();
        case 3744684:  // zone
          return ((UserForm) bean).getZone();
        case -259925341:  // dateStyle
          return ((UserForm) bean).getDateStyle();
        case 25596644:  // timeStyle
          return ((UserForm) bean).getTimeStyle();
        case -1721461188:  // baseUser
          return ((UserForm) bean).getBaseUser();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -266666762:  // userName
          ((UserForm) bean).setUserName((String) newValue);
          return;
        case 1403763597:  // passwordRaw
          ((UserForm) bean).setPasswordRaw((String) newValue);
          return;
        case -1070931784:  // emailAddress
          ((UserForm) bean).setEmailAddress((String) newValue);
          return;
        case 1714148973:  // displayName
          ((UserForm) bean).setDisplayName((String) newValue);
          return;
        case -1097462182:  // locale
          ((UserForm) bean).setLocale((String) newValue);
          return;
        case 3744684:  // zone
          ((UserForm) bean).setZone((String) newValue);
          return;
        case -259925341:  // dateStyle
          ((UserForm) bean).setDateStyle((String) newValue);
          return;
        case 25596644:  // timeStyle
          ((UserForm) bean).setTimeStyle((String) newValue);
          return;
        case -1721461188:  // baseUser
          ((UserForm) bean).setBaseUser((ManageableUser) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
