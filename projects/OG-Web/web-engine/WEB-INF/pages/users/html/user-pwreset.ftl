<#escape x as x?html>
<@page title="Reset password - ${user.userName}">


<#-- SECTION Reset password -->
<@section title="Reset password">
  <#if err_unexpected??><div class="err">An unexpected error occurred</div></#if>
  <@form method="POST" action="${uris.user()}">
  <p>
    <@rowout label="User name">${user.userName}</@rowout>

    <#if err_passwordMissing??><div class="err">The password must be entered</div></#if>
    <#if err_passwordTooShort??><div class="err">The password is too short, minimum of 6 characters</div></#if>
    <#if err_passwordTooLong??><div class="err">The password is too long, maximum of 100 characters</div></#if>
    <#if err_passwordWeak??><div class="err">The password is too weak</div></#if>
    <@rowin label="Password"><input type="password" size="40" maxlength="255" name="password" value="" id="password" /></@rowin>

    <@rowin><input type="submit" value="Reset" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.user()}">User - ${user.userName}</a><br />
    <a href="${uris.users()}">User search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
