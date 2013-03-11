<#escape x as x?html>
<@page title="Update - ${exchange.name}">


<#-- SECTION Update exchange -->
<@section title="Update exchange">
  <@form method="PUT" action="${uris.exchange()}">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <#if err_idschemeMissing??><div class="err">The scheme type must be entered</div></#if>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <#if err_idvalueMissing??><div class="err">The scheme id must be entered</div></#if>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <#if err_regionschemeMissing??><div class="err">The region type must be entered</div></#if>
    <@rowin label="Region type"><input type="text" size="30" maxlength="80" name="regionscheme" value="" /></@rowin>
    <#if err_regionvalueMissing??><div class="err">The region id must be entered</div></#if>
    <@rowin label="Region id"><input type="text" size="30" maxlength="80" name="regionvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.exchange()}">Exchange - ${exchange.name}</a><br />
    <a href="${uris.exchanges()}">Exchange search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
