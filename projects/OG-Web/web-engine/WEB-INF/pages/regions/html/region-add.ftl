<#escape x as x?html>
<@page title="Add - ${region.name}">


<#-- SECTION Add region -->
<@section title="Add region">
  <@form method="POST" action="${uris.region()}">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${region.name}" /></@rowin>
    <@rowin label="Full name"><input type="text" size="30" maxlength="80" name="fullname" value="${region.fullName}" /></@rowin>
    <#if err_classificationMissing??><div class="err">The classification must be entered</div></#if>
    <@rowin label="Classification"><input type="text" size="30" maxlength="80" name="classification" value="${region.classification}" /></@rowin>
    <@rowin label="Country ISO"><input type="text" size="30" maxlength="80" name="country" value="" /></@rowin>
    <@rowin label="Currency ISO"><input type="text" size="30" maxlength="80" name="currency" value="" /></@rowin>
    <@rowin label="Time zone"><input type="text" size="30" maxlength="80" name="timezone" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.region()}">Region - ${region.name}</a><br />
    <a href="${uris.regions()}">Region search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
