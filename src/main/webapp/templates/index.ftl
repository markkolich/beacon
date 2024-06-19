<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><#if ip?has_content>${ip}<#else>null</#if></title>
<style>
    body {font-family: sans-serif}
    h1,h2 {vertical-align: top; text-align: center; margin:0; padding:0;}
</style>
<script src="https://code.jquery.com/jquery-3.6.0.min.js" integrity="sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4=" crossorigin="anonymous"></script>
<script>
    const fontsize = function () {
      var fontSize = $("body").width() * 0.1 + "px";
      $("h1").css('font-size', fontSize);
    };
    $(window).resize(fontsize);
    $(document).ready(fontsize);
</script>
</head>
<body>
    <h1><#if ip?has_content>${ip}<#else>null</#if></h1>

    <#if devMode>
        <h2>Dev Mode</h2>
    <#else>
        <h2>${buildVersion.getBuildNumber()} &ndash; ${buildVersion.getTimestamp()}</h2>
    </#if>

</body>
</html>
