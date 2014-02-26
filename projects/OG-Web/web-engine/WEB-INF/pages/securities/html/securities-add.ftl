<#escape x as x?html>
<@page title="Securities" jquery=true aceXmlEditor=true>

<#-- SECTION Add security -->
<@section title="Add security by XML">
  <@form method="POST" action="${uris.securities()}" id="addSecurityForm">
  <p>
    <#if err_securityXml??>
      <div class="err">${err_securityXmlMsg}</div>
    </#if>
    <#if uniqueIdSchemes?exists>
    <@rowin label="UniqueId Scheme">
      <select name="uniqueIdScheme">
        <option value="" <#if scheme = ''>selected</#if>></option>
        <#list uniqueIdSchemes as uniqueIdScheme>
        <option value="${uniqueIdScheme}" <#if scheme = '${uniqueIdScheme}'>selected</#if>>${uniqueIdScheme}</option>
        </#list>
      </select>
    </@rowin>
    </#if>  
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
    <input type="hidden" name="type" value="xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addSecurityForm" inputId="security-xml" xmlValue="${securityXml}"></@xmlEditorScript></#noescape>
    
  </p>
  </@form>
</@section>

<#-- SECTION Load and Add security -->
<@section title="Load securities by ID">
  <@form method="POST" action="${uris.securities()}">
  <p>
    <#if err_idschemeMissing??>
      <div class="err">The scheme type must be entered</div>
    </#if>
    <@rowin label="Scheme type">
      <select name="idscheme">
        <option value="BLOOMBERG_TICKER">Bloomberg Ticker</option>
        <option value="BLOOMBERG_TCM">Bloomberg Ticker/Coupon/Maturity</option>
        <option value="BLOOMBERG_BUID">Bloomberg BUID</option>
        <option value="CUSIP">CUSIP</option>
        <option value="ISIN">ISIN</option>
        <option value="RIC">RIC</option>
        <option value="SEDOL1">SEDOL</option>
      </select>
    </@rowin>
    <#if err_idvalueMissing??>
      <div class="err">The scheme identifiers must be entered</div>
    </#if>
      <@rowin label="Identifiers">
        <textarea name="idvalue" cols="35" rows="10"></textarea>
      </@rowin>
    <input type="hidden" name="type" value="id"/>
    <@rowin><input type="submit" value="Add"/></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
