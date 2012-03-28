<!doctype html>
<!--[if IE 8 ]><html lang="en" class="no-js ie8"><![endif]-->
<!--[if IE 9 ]><html lang="en" class="no-js ie9"><![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"> <!--<![endif]-->
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="SKYPE_TOOLBAR" content="SKYPE_TOOLBAR_PARSER_COMPATIBLE" />
<meta name="google" value="notranslate">
<title>Import</title>
${ogStyle.print('og_all.css', 'all',false)}
<style type="text/css">
    body {background: #fff;}
    div {margin-bottom: 20px}
    small {font-size: 10px;}
    input[type=text] {width: 200px}
</style>
<script type="text/javascript">
  window.onload = function () {document.getElementsByTagName('input')[0].focus();}
</script>
</head>
<body>
  <form action="http://localhost:8080/jax/portfolioupload/10/1000" enctype="multipart/form-data" method="post">
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
        CSV Upload:<br />
        <input type="file" name="file"><br />
      </label>
      <small><a href="/prototype/data/example-portfolio.csv">Example CSV Format</a></small>
    </div>
  </form>
</body>
</html>