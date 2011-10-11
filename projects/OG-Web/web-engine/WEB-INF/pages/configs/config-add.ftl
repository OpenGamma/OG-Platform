<#escape x as x?html>
<@page title="Add configuration">


<#-- SECTION Add configuration -->
<@section title="Add configuration">
  <@form method="POST" action="${uris.configs()}">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    
    <#if err_typeMissing??><div class="err">The type must be entered</div></#if>
    <@rowin label="Type"><input type="text" size="30" maxlength="80" name="type" value="" /></@rowin>
    
    <#if err_xmlMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Configuration (XML)">
      <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="80" name="configxml" id="xmltextarea"></textarea></div>
    </@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.configs()}">Configuration home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
