<#escape x as x?html>
<@page title="Add position" jquery=true aceXmlEditor=true>


<#-- SECTION Add position -->
<@section title="Add position">
  <@form method="POST" action="${uris.positions()}">
  <p>
    <#if uniqueIdSchemes?exists>
    <@rowin label="UniqueId Scheme">
      <select name="uniqueIdScheme">
        <#assign selectedScheme = "${selectedUniqueIdScheme!''}">
        <option value="" <#if selectedScheme = ''>selected</#if>></option>
        <#list uniqueIdSchemes as uniqueIdScheme>
        <option value="${uniqueIdScheme}" <#if  uniqueIdScheme == selectedScheme>selected</#if>>${uniqueIdScheme}</option>
        </#list>
      </select>
    </@rowin>
    </#if> 
    <#if err_quantityMissing??><div class="err">The quantity must be entered</div></#if>
    <#if err_quantityNotNumeric??><div class="err">The quantity must be numeric</div></#if>
    <@rowin label="Quantity"><input type="text" size="10" maxlength="12" name="quantity" value="${quantity!''}" /></@rowin>
    <#if err_idschemeMissing??><div class="err">The scheme must be entered</div></#if>
    <@rowin label="Scheme type">
      <select name="idscheme">
        <#list externalSchemes?keys as key> 
          <option value="${key}">${externalSchemes[key]}</option>
       </#list>
      </select>
    </@rowin>
    <#if err_idvalueMissing??><div class="err">The identifier must be entered</div></#if>
    <#if err_idvalueNotFound??><div class="err">The identifier was not found</div></#if>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="idvalue" value="${idvalue!''}" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Add position by XML-->
<@section title="Add position by XML">
  <@form method="POST" action="${uris.positions()}" id="addPositionForm">
  <p>
    <#if err_xmlMissing??>
      <div class="err">The position XML must be entered</div>
    </#if>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <@rowin><input type="hidden" name="positionXml" id="position-xml"/></@rowin>
    <input type="hidden" name="type" value="xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  <#noescape><@xmlEditorScript formId="addPositionForm" inputId="position-xml" xmlValue="${positionXml!''}"></@xmlEditorScript></#noescape> 
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.positions()}">Position search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
