<#-- Register a new user -->
<#escape x as x?html>
<@page title="Your profile">
<#-- SECTION Main -->
<@section title="Change your profile">
  <p>
    Use this form to change your preferences.
  </p>
  <#if err_unexpected??><div class="err">An unexpected error occurred</div></#if>
  <@form method="POST" action="${userSecurity.profileUri}">
  <p>
    <#if err_emailMissing??><div class="err">The email address must be entered</div></#if>
    <#if err_emailToolLong??><div class="err">The email address is too long</div></#if>
    <#if err_emailInvalid??><div class="err">The email address is invalid</div></#if>
    <@rowin label="Email address"><input type="email" size="40" maxlength="200" name="email" value="${email}" id="email" /></@rowin>

    <#if err_displaynameMissing??><div class="err">The display name must be entered</div></#if>
    <#if err_displaynameTooLong??><div class="err">The display name is too long</div></#if>
    <#if err_displaynameInvalid??><div class="err">The display name is invalid</div></#if>
    <@rowin label="Display name"><input type="text" size="40" maxlength="200" name="displayname" value="${displayname}" id="displayname" /></@rowin>

    <#if err_localeInvalid??><div class="err">The locale is invalid</div></#if>
    <@rowin label="Locale">
      <select name="locale" id="locale">
        <option value="en" <#if timezone = 'en'>selected</#if>>English</option>
        <option value="fr" <#if timezone = 'fr'>selected</#if>>French</option>
        <option value="es" <#if timezone = 'es'>selected</#if>>Spanish</option>
      </select>
    </@rowin>

    <#if err_timezoneInvalid??><div class="err">The time zone is invalid</div></#if>
    <@rowin label="Time zone">
      <select name="timezone" id="timezone">
        <option value="Europe/London" <#if timezone = 'Europe/London'>selected</#if>>London, West Europe</option>
        <option value="Europe/Paris" <#if timezone = 'Europe/Paris'>selected</#if>>Paris, Central Europe</option>
        <option value="Europe/Berlin" <#if timezone = 'Europe/Berlin'>selected</#if>>Berlin, Central Europe</option>
        <option value="Europe/Moscow" <#if timezone = 'Europe/Moscow'>selected</#if>>Moscow, Russian Federation</option>
        <option value="America/New_York" <#if timezone = 'America/New_York'>selected</#if>>New York, Eastern</option>
        <option value="America/Chicago" <#if timezone = 'America/Chicago'>selected</#if>>Chicago, Central</option>
        <option value="America/Denver" <#if timezone = 'America/Denver'>selected</#if>>Denver, Mountain</option>
        <option value="America/Los_Angeles" <#if timezone = 'America/Los_Angeles'>selected</#if>>Los Angeles, Pacific</option>
        <option value="Asia/Hong_Kong" <#if timezone = 'Asia/Hong_Kong'>selected</#if>>Hong Kong</option>
        <option value="Asia/Seoul" <#if timezone = 'Asia/Seoul'>selected</#if>>Seoul, South Korea</option>
        <option value="Asia/Singapore" <#if timezone = 'Asia/Singapore'>selected</#if>>Singapore</option>
        <option value="Asia/Tokyo" <#if timezone = 'Asia/Tokyo'>selected</#if>>Tokyo, Japan</option>
        <option value="Australia/Sydney" <#if timezone = 'Australia/Sydney'>selected</#if>>Sydney, Australia</option>
      </select>
    </@rowin>

    <#if err_datestyleInvalid??><div class="err">The date style is invalid</div></#if>
    <@rowin label="Date style">
      <select name="datestyle" id="datestyle">
        <option value="ISO" <#if datestyle = 'ISO'>selected</#if>>ISO format - yyyy-MM-dd</option>
        <option value="STANDARD_US" <#if datestyle = 'STANDARD_US'>selected</#if>>Standard US format - MM/dd/yyyy</option>
        <option value="STANDARD_EU" <#if datestyle = 'STANDARD_EU'>selected</#if>>Standard EU format - dd/MM/yyyy</option>
        <option value="TEXTUAL_MONTH" <#if datestyle = 'TEXTUAL_MONTH'>selected</#if>>Textual month format - d MMM yyyy</option>
        <option value="LOCALIZED_SHORT" <#if datestyle = 'LOCALIZED_SHORT'>selected</#if>>Localized format (short)</option>
        <option value="LOCALIZED_MEDIUM" <#if datestyle = 'LOCALIZED_MEDIUM'>selected</#if>>Localized format (medium)</option>
        <option value="LOCALIZED_LONG" <#if datestyle = 'LOCALIZED_LONG'>selected</#if>>Localized format (long)</option>
        <option value="LOCALIZED_FULL" <#if datestyle = 'LOCALIZED_FULL'>selected</#if>>Localized format (full)</option>
      </select>
    </@rowin>

    <#if err_timestyleInvalid??><div class="err">The time style is invalid</div></#if>
    <@rowin label="Time style">
      <select name="timestyle" id="timestyle">
        <option value="ISO" <#if timestyle = 'ISO'>selected</#if>>ISO format - HH:mm:ss</option>
        <option value="LOCALIZED_SHORT" <#if timestyle = 'LOCALIZED_SHORT'>selected</#if>>Localized format (short)</option>
        <option value="LOCALIZED_MEDIUM" <#if timestyle = 'LOCALIZED_MEDIUM'>selected</#if>>Localized format (medium)</option>
        <option value="LOCALIZED_LONG" <#if timestyle = 'LOCALIZED_LONG'>selected</#if>>Localized format (long)</option>
        <option value="LOCALIZED_FULL" <#if timestyle = 'LOCALIZED_FULL'>selected</#if>>Localized format (full)</option>
      </select>
    </@rowin>

    <@rowin><input type="submit" value="Save profile" /></@rowin>
  </p>
  </@form>
</@section>

<@section title="Change your password">
  <p>
    Use this form to change your password.
  </p>
  <#if err_unexpected??><div class="err">An unexpected error occurred</div></#if>
  <@form method="POST" action="${userSecurity.profileUri}">
  <p>
    <#if err_passwordMissing??><div class="err">The password must be entered</div></#if>
    <#if err_passwordTooShort??><div class="err">The password is too short, minimum of 6 characters</div></#if>
    <#if err_passwordTooLong??><div class="err">The password is too long, maximum of 100 characters</div></#if>
    <#if err_passwordWeak??><div class="err">The password is too weak</div></#if>
    <@rowin label="Password"><input type="password" size="40" maxlength="255" name="password" value="" id="password" /></@rowin>

    <@rowin><input type="submit" value="Change password" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Return home</a><br />
  </p>
</@section>
<p>
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
</p>
</@page>
</#escape>
