<#escape x as x?html>
<@page title="Update role">


<#-- SECTION Update role -->
<@section title="Update role">
  <#if err_unexpected??><div class="err">An unexpected error occurred</div></#if>
  <@form method="POST" action="${uris.roles()}">
  <p>
    <#if err_descriptionMissing??><div class="err">The description must be entered</div></#if>
    <#if err_descriptionTooLong??><div class="err">The description is too long, maximum of 200 characters</div></#if>
    <#if err_descriptionInvalid??><div class="err">The description is invalid</div></#if>
    <@rowin label="Description"><input type="text" size="40" maxlength="200" name="description" value="${description}" id="description" autofocus /></@rowin>

    <@rowin label="Add roles"><input type="text" size="40" maxlength="400" name="addroles" value="" id="addroles" /></@rowin>
    <@rowin label="Remove roles"><input type="text" size="40" maxlength="400" name="removeroles" value="" id="removeroles" /></@rowin>
    <@rowin label="Add permissions"><input type="text" size="40" maxlength="400" name="addperms" value="" id="addperms" /></@rowin>
    <@rowin label="Remove permissions"><input type="text" size="40" maxlength="400" name="removeperms" value="" id="removeperms" /></@rowin>
    <@rowin label="Add users"><input type="text" size="40" maxlength="400" name="addusers" value="" id="addusers" /></@rowin>
    <@rowin label="Remove users"><input type="text" size="40" maxlength="400" name="removeusers" value="" id="removeusers" /></@rowin>

    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.role()}">Role - ${role.roleName}</a><br />
    <a href="${uris.roles()}">User search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
