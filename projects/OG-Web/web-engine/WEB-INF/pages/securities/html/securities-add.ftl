<#escape x as x?html>
<@page title="Securities">


<#-- SECTION Add security -->
<@section title="Add security">
  <@form method="POST" action="${uris.securities()}" id="addSecurityForm">
  <p>
    <#if err_idschemeMissing??>
      <div class="err">The scheme type must be entered</div>
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
    </#if>
    
    <#if err_idvalueMissing??>
      <div class="err">The scheme identifiers must be entered</div>
      <@rowin label="Identifiers">
        <textarea name="idvalue" cols="35" rows="10"></textarea>
      </@rowin>
    </#if>
    
    <#if err_securityXml??>
      <div class="err">${err_securityXmlMsg}</div>
      <@subsection title="Security Bean XML">
        <@rowin label="">
          <div id="security-xml-editor">${securityXml}</div>
        </@rowin>
      </@subsection>
      <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
      <script type="text/javascript">
        var editor = ace.edit("security-xml-editor")
        editor.getSession().setMode('ace/mode/xml')
        $("#security-xml-editor").show()

        $("#addSecurityForm").submit( function(eventObj) {
          $("#security-xml").val(editor.getSession().getValue())
          return true
        })
      </script>
    </#if>
    
    <@rowin><input type="submit" value="Add" /></@rowin>
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
