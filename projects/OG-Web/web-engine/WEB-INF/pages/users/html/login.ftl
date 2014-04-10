<#-- Green version of login -->
<#escape x as x?html>
<@page title="Login">
<#-- SECTION Main -->
<@section title="Login">
  <p>
    Please login to OpenGamma risk analytics.
  </p>
  <@form method="POST" action="${security.loginUri}">
  <p>
    <#if err_invalidLogin??>
     <div class="err">
      <#if err_invalidLogin = 'UserNameMissing'>The user name must be entered
      <#elseif err_invalidLogin = 'PasswordMissing'>The password must be entered
      <#elseif err_invalidLogin = 'UnknownAccount'>The user name does not exist
      <#elseif err_invalidLogin = 'IncorrectCredentials'>The password is incorrect
      <#elseif err_invalidLogin = 'ExpiredCredentials'>The password has expired
      <#elseif err_invalidLogin = 'DisabledAccount'>The account has been disabled
      <#else><div class="err">Unable to login
      </#if>
     </div>
    </#if>
    <@rowin label="User name"><input type="text" size="40" maxlength="20" name="username" value="${username}" id="username" autofocus /></@rowin>
    <@rowin label="Password"><input type="password" size="40" maxlength="100" name="password" value="" id="password" /></@rowin>
    <@rowin><input type="submit" value="Login" /></@rowin>
  </p>
  </@form>
</@section>
<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${security.registerUri}">Register</a><br />
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
