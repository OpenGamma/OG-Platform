<#escape x as x?html>
<@page title="Market Data Snapshot" jquery=true jqueryDate=true>


<#-- SECTION Market Data Snapshot search -->
<@section title="Market Data Snapshot search" if=searchRequest??>
  <@form method="GET" action="${uris.snapshots()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Market Data Snapshot results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No market data snapshots" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.snapshot(item.uniqueId)}">${item.snapshot.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.snapshot(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add market data snapshot -->
<@section title="Add market data snapshot">
  <@form method="POST" action="${uris.snapshots()}">
    <script src="/green/js/marketDataSnapshot.js"></script>
  <p>
    <@rowin label="Snapshot Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin label="View Name">
      <select name="view">
        <option value="" selected></option>
        <#list views as view> 
        <option value="${view}">${view}</option>
        </#list>
      </select>
    </@rowin>    
    <@rowin label="Valuation Time (HH:mm:ss)"><input type="text" size="30" maxlength="80" name="valuationTime" value="" /></@rowin>
    <br/>
    <@subsection title="DataSources">
      
      <@rowin label=""><b>Live</b><img src="/images/down.png/" class="hide" name="live"></@rowin>
      <@rowin label="" id="live"><#list liveDataSources as liveDataSource><input type="checkbox" name="liveDataSources" value="${liveDataSource}">${liveDataSource}<br/></#list></@rowin>
      <@rowin label=""><b>Historical</b><img src="/images/down.png/" class="hide" name="historical"></@rowin>
      <@rowin label="" id="historical">
        <#list timeseriesresolverkeys as tsResolverKey>
          <div class="tsResolver">       
            <div><input type="checkbox" class="tsResolverkey" id="${tsResolverKey}_key" name="tsResolverKeys" value="${tsResolverKey}">${tsResolverKey}</div>
            <div><input type="text" class="datepicker" name="${tsResolverKey}_CustomDate"/> </div>
          </div>
          <br/>
        </#list>
      </@rowin>
      <#if (snapshots?size > 0)>
        <@rowin label=""><b>Snapshot</b><img src="/images/down.png/" class="hide" name="userSnapshot"></@rowin>
        <@rowin label="" id="userSnapshot"><#list snapshots as snapshot><input type="checkbox" name="userSnapshotIds" value="${snapshot.uniqueId?string}">${snapshot.uniqueId?string} ${snapshot.name}<br/></#list></@rowin>
      </#if>
    </@subsection>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
