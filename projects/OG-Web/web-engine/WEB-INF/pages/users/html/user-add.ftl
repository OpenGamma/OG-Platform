<#escape x as x?html>
<@page title="Add user">


<#-- SECTION Add user -->
<@section title="Add user">
  <#if err_unexpected??><div class="err">An unexpected error occurred</div></#if>
  <@form method="POST" action="${uris.users()}">
  <p>
    <#if err_usernameMissing??><div class="err">The user name must be entered</div></#if>
    <#if err_usernameTooShort??><div class="err">The user name is too short, it must be between 5 and 20 characters</div></#if>
    <#if err_usernameTooLong??><div class="err">The user name is too long, it must be between 5 and 20 characters</div></#if>
    <#if err_usernameInvalid??><div class="err">The user name is invalid, it must consist of letters, numbers, dash and underscore, starting with a letter</div></#if>
    <#if err_usernameAlreadyInUse??><div class="err">The user name has already been used</div></#if>
    <@rowin label="User name"><input type="text" size="40" maxlength="20" name="username" value="${username}" id="username" autofocus /></@rowin>

    <#if err_passwordMissing??><div class="err">The password must be entered</div></#if>
    <#if err_passwordTooShort??><div class="err">The password is too short, minimum of 6 characters</div></#if>
    <#if err_passwordTooLong??><div class="err">The password is too long, maximum of 100 characters</div></#if>
    <#if err_passwordWeak??><div class="err">The password is too weak</div></#if>
    <@rowin label="Password"><input type="password" size="40" maxlength="100" name="password" value="" id="password" /></@rowin>

    <#if err_emailMissing??><div class="err">The email address must be entered</div></#if>
    <#if err_emailToolLong??><div class="err">The email address is too long</div></#if>
    <#if err_emailInvalid??><div class="err">The email address is invalid</div></#if>
    <@rowin label="Email address"><input type="email" size="40" maxlength="200" name="email" value="${email}" id="email" /></@rowin>

    <#if err_displaynameMissing??><div class="err">The display name must be entered</div></#if>
    <#if err_displaynameTooLong??><div class="err">The display name is too long</div></#if>
    <#if err_displaynameInvalid??><div class="err">The display name is invalid</div></#if>
    <@rowin label="Display name"><input type="text" size="40" maxlength="200" name="displayname" value="${displayname}" id="displayname" /></@rowin>

    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.users()}">User search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
