<#escape x as x?html>
<@page title="Update position - ${position.name}">


<#-- SECTION Position output -->
<@section title="Portfolio node">
  <p>
    <@rowout label="Name">${position.name}</@rowout>
    <@rowout label="Reference">${position.uniqueId.value}, version ${position.uniqueId.version}</@rowout>
  </p>
</@section>
<#-- SECTION Update position -->
<@section title="Update position">
  <@form method="PUT" action="${uris.position()}">
  <p>
    <#if err_quantityMissing??><div class="err">The quantity must be entered</div></#if>
    <@rowin label="Quantity"><input type="text" size="30" maxlength="12" name="quantity" value="" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.position()}">Position - ${position.name}</a><br />
    <a href="${uris.positions()}">Position search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
