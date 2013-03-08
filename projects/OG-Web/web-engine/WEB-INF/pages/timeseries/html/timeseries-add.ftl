<#escape x as x?html>
<@page title="Time Series">


<#-- SECTION Add timeseries -->
<@section title="Add timeseries">
  <@form method="POST" action="${uris.allTimeSeries()}">
  <p>
    <@rowin label="Scheme type">
      <select name="idscheme">
        <option value="ACTIVFEED_TICKER" <#if scheme = 'ACTIVFEED_TICKER'>selected</#if>>ActivFeed Ticker</option>
      	<option value="" <#if scheme = ''>selected</#if>></option>
        <option value="BLOOMBERG_TICKER" <#if scheme = 'BLOOMBERG_TICKER'>selected</#if>>Bloomberg Ticker</option>
        <option value="BLOOMBERG_TCM" <#if scheme = 'BLOOMBERG_TCM'>selected</#if>>Bloomberg Ticker/Coupon/Maturity</option>
        <option value="BLOOMBERG_BUID" <#if scheme = 'BLOOMBERG_BUID'>selected</#if>>Bloomberg Buid</option>
        <option value="CUSIP" <#if scheme = 'CUSIP'>selected</#if>>Cusip</option>
        <option value="ISIN" <#if scheme = 'ISIN'>selected</#if>>Isin</option>
        <option value="RIC" <#if scheme = 'RIC'>selected</#if>>Ric</option>
        <option value="SEDOL1" <#if scheme = 'SEDOL1'>selected</#if>>Sedol1</option>
      </select>
    </@rowin>
    <@rowin label="Data provider"><input type="text" size="30" maxlength="80" name="dataProvider" value="${dataProvider}" /></@rowin>
    <#if err_iddatafieldMissing??><div class="err">The data field must be entered</div></#if>
	<@rowin label="Data field"><input type="text" size="30" maxlength="80" name="dataField" value="${dataField}" /></@rowin>
	<#if err_startInvalid??><div class="err">The start date is invalid</div></#if>
	<@rowin label="Start date(yyyy-mm-dd)"><input type="text" size="30" maxlength="80" name="startDate" value="${start}" /></@rowin>
	<#if err_endInvalid??><div class="err">The end date is invalid</div></#if>
	<@rowin label="End date(yyyy-mm-dd)"><input type="text" size="30" maxlength="80" name="endDate" value="${end}" /></@rowin>
	<#if err_idvalueMissing??><div class="err">Identifiers must be entered</div></#if>
    <@rowin label="Identifiers"></@rowin>
    <@rowin><textarea name="idvalue" cols="35" rows="10">${idValue}</textarea></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.allTimeSeries()}">Timeseries search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
