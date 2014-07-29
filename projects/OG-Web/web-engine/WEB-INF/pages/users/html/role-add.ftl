<#escape x as x?html>
<@page title="Add role">


<#-- SECTION Add role -->
<@section title="Add role">
  <#if err_unexpected??><div class="err">An unexpected error occurred</div></#if>
  <@form method="POST" action="${uris.roles()}">
  <p>
    <#if err_rolenameMissing??><div class="err">The role name must be entered</div></#if>
    <#if err_rolenameTooShort??><div class="err">The role name is too short, it must be between 5 and 20 characters</div></#if>
    <#if err_rolenameTooLong??><div class="err">The role name is too long, it must be between 5 and 20 characters</div></#if>
    <#if err_rolenameInvalid??><div class="err">The user name is invalid, it must consist of letters, numbers, dash and underscore, starting with a letter</div></#if>
    <#if err_rolenameAlreadyInUse??><div class="err">The role name has already been used</div></#if>
    <@rowin label="Role name"><input type="text" size="40" maxlength="20" name="rolename" value="${rolename}" id="rolename" autofocus /></@rowin>

    <#if err_descriptionMissing??><div class="err">The description must be entered</div></#if>
    <#if err_descriptionTooLong??><div class="err">The description is too long, maximum of 200 characters</div></#if>
    <#if err_descriptionInvalid??><div class="err">The description is invalid</div></#if>
    <@rowin label="Description"><input type="text" size="40" maxlength="200" name="description" value="${description}" id="description" /></@rowin>

    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.roles()}">User search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
