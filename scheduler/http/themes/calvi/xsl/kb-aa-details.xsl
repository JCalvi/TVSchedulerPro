<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">

      <html>
      <head>
        <title>Timer Control</title>
        <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
        <script>
          var index =   "<xsl:value-of select="//@index" />";
          var start =   "<xsl:value-of select="//start" />";
          var show =    "<xsl:value-of select="//show" />";
          var referrer = "<xsl:value-of select="//referrer" />";
        </script>
        <script src="/themes/calvi/javascript/kbaaform.js"></script>
      </head>
      <body onload="Initialize();">

      <table class='KBButtonTable'>
        <tr><td align="center" valign="middle">
          <table>
            <tr>
              <td></td>
              <td><div class="PageTitle">Auto-Add Options Menu</div></td>
            </tr>
            <tr>
              <td class="TextR">Start Buffer</td>
              <xsl:apply-templates select="//startBuffer" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">End Buffer</td>
              <xsl:apply-templates select="//endBuffer" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">CaptureType</td>
              <xsl:apply-templates select="//captureType" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">AutoDelete</td>
              <xsl:apply-templates select="//autoDel" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">Keep For</td>
              <xsl:apply-templates select="//keepfor" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">Post Task</td>
              <xsl:apply-templates select="//posttask" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">Filename Pattern</td>
              <xsl:apply-templates select="//filenamePatterns" mode="cell" />
            </tr>
            <tr>
              <td class="TextR">Capture Path</td>
              <xsl:apply-templates select="//capturePaths" mode="cell" />
            </tr>
          </table>
        </td></tr>
      </table>

      <form name="data" method="POST" action="/servlet/KBAutoAddRes" accept-charset="UTF-8">
        <input type="hidden" name="action" value="06"/>
        <input type="hidden" name="startbuffer" value=""/>
        <input type="hidden" name="endbuffer" value=""/>
        <input type="hidden" name="captype" value=""/>
        <input type="hidden" name="capPath" value=""/>
        <input type="hidden" name="autoDel" value=""/>
        <input type="hidden" name="keepFor" value=""/>
        <input type="hidden" name="task" value=""/>
        <input type="hidden" name="filenamePatterns" value=""/>
        <input type="hidden" name="index" value=""/>
        <input type="hidden" name="start" value=""/>
        <input type="hidden" name="show" value=""/>
        <input type="hidden" name="url" value=""/>
      </form>

      </body>
      </html>

	</xsl:template>

  <xsl:template match="startBuffer" mode="cell" >
    <td id="startBuffer" width="400" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">SB</span>
    </td>
  </xsl:template>

	<xsl:template match="endBuffer" mode="cell">
  <td id="endBuffer" class="FormInput" >
    <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
    <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
    <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
    <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
    <span class="solidText">EB</span>
  </td>
	</xsl:template>

	<xsl:template match="captureType" mode="cell">
    <td id="captype" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option_value" />
      <span class="solidText">TYPE</span>
    </td>
	</xsl:template>

	<xsl:template match="keepfor" mode="cell">
    <td id="keepfor" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">30 days</span>
    </td>
	</xsl:template>

	<xsl:template match="autoDel" mode="cell">
    <td id="autoDel" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <span class="solidText"><xsl:value-of select="@value" /></span>
    </td>
	</xsl:template>

	<xsl:template match="posttask" mode="cell">
    <td id="postTask" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option" />
      <span class="solidText">none</span>
    </td>
	</xsl:template>

	<xsl:template match="filenamePatterns" mode="cell">
    <td id="filenamePatterns" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option" />
      <span class="solidText">Null</span>
    </td>
	</xsl:template>

	<xsl:template match="capturePaths" mode="cell">
    <td id="capturePaths" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option_value" />
      <span class="solidText">PATH</span>
    </td>
	</xsl:template>

	<xsl:template match="option" mode="option">
	   <xsl:attribute name="option_{position()-1}"><xsl:value-of select="." /></xsl:attribute>
	</xsl:template>

	<xsl:template match="option" mode="option_value">
	   <xsl:attribute name="option_{position()-1}"><xsl:value-of select="." /></xsl:attribute>
	   <xsl:attribute name="value_{position()-1}"><xsl:value-of select="@value" /></xsl:attribute>
	</xsl:template>

</xsl:stylesheet>