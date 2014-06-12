<#include "modules/common/og.common.header.ftl">
<title>Import</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css">
    body {background: #fff;}
    div {margin-bottom: 9px}
    small {font-size: 10px;}
    input[type=text] {width: 200px}
    td {vertical-align: top;}
    table {width: 100%;}
</style>
<script type="text/javascript">
  window.onload = function () {document.getElementsByTagName('input')[0].focus();}
</script>
</head>
<body>
  <form action="/jax/portfolioupload" enctype="multipart/form-data" method="post">
    <table>
      <tr>
        <td>
          <div>
            <label>
              CSV/XLS Upload:<br />
              <input type="file" name="file"><br />
            </label>
            <small><a href="/prototype/data/example-portfolio.csv">Example CSV Format</a></small>
          </div>
          <div>
            <label>
              Portfolio Name: <br /><input type="text" name="portfolioName"><br />
            </label>
          </div>
          <div>
            <label>
              Timeseries DataField: <br /><input type="text" name="dataField"><br />
              <small>(comma delimited, e.g. PX_LAST, YLD_YTM_MID)</small>
            </label>
          </div>
          <div>
            <label>
              Data Provider: <br /><input type="text" name="dataProvider"><br />
            </label>
          </div>
          <div>
            <label>
              Date Format: <br />
              <select name="dateFormat">
                <option value="ISO">ISO Format (yyyy-MM-dd or yyyyMMdd)</option>
                <option value="US">US Format (MM-dd-yyyy or MM/dd/yyyy)</option>
                <option value="UK">UK Format (dd-MM-yyyy or dd/MM/yyyy)</option>
              </select>
            </label>
          </div>
        </td>
        <td width="10px" style="border-left: 1px solid #CCCCCC;"></td>
        <td>
          <div>
            <label>
              XML Upload:<br />
              <input type="file" name="filexml"><br />
            </label>
          </div>
        </td>
      </tr>
    </table>
  </form>
</body>
</html>
