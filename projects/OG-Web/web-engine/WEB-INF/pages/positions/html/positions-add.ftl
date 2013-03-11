<#escape x as x?html>
<@page title="Add position">


<#-- SECTION Add position -->
<@section title="Add position">
  <@form method="POST" action="${uris.positions()}">
  <p>
    <#if err_quantityMissing??><div class="err">The quantity must be entered</div></#if>
    <#if err_quantityNotNumeric??><div class="err">The quantity must be numeric</div></#if>
    <@rowin label="Quantity"><input type="text" size="10" maxlength="12" name="quantity" value="" /></@rowin>
    <#if err_idschemeMissing??><div class="err">The scheme must be entered</div></#if>
    <@rowin label="Scheme type">
      <select name="idscheme">
        <option value="BLOOMBERG_TICKER">Bloomberg Ticker</option>
        <option value="BLOOMBERG_BUID">Bloomberg BUID</option>
        <option value="CUSIP">CUSIP</option>
        <option value="ISIN">ISIN</option>
        <option value="RIC">RIC</option>
        <option value="SEDOL1">SEDOL</option>
      </select>
    </@rowin>
    <#if err_idvalueMissing??><div class="err">The identifier must be entered</div></#if>
    <#if err_idvalueNotFound??><div class="err">The identifier was not found</div></#if>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
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
