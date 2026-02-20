<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">

  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
    <script>
      var back = "<xsl:value-of select="//@back" />";
    </script>
    <script src="/themes/calvi/javascript/kbbuttons.js"></script>
    <script src="/javascript/jumpBack.js"></script>
  </head>
  <body onLoad="Initialize();">

  <table class='KBButtonTable'>
    <tr>
      <td align="center" valign="middle">
        <table width="95%" border="0" cellspacing="5" id="itemTable">
          <tr>
            <td class="name">Title</td>
            <td class="value"><xsl:value-of select="//title" /></td>
          </tr>
          <xsl:if test="string-length(//sub-title) > 1">
            <tr>
              <td class="name">Subtitle</td>
              <td class="value"><xsl:value-of select="//sub-title" /></td>
            </tr>
          </xsl:if>
          <tr>
            <td class="name">Time</td>
            <td class="value"><xsl:value-of select="//start" />
			 (<xsl:value-of select="//duration" /> min)
			</td>
          </tr>
          <tr>
            <td class="name">Channel</td>
            <td class="value"><xsl:value-of select="//channel" /></td>
          </tr>
          <xsl:if test="string-length(//flags) > 1">
            <tr>
              <td class="name">Flags</td>
              <td class="value"><xsl:value-of select="//flags" /></td>
            </tr>
          </xsl:if>
         <xsl:variable name="brReplaced">
           <xsl:call-template name="replace-string">
             <xsl:with-param name="text" select="//description"/>
             <xsl:with-param name="replace" select="'&#60;br&#62;'"/>
             <xsl:with-param name="with" select="'&#13;'"/>
           </xsl:call-template>
         </xsl:variable>

         <!--<xsl:value-of select="string-length($brReplaced)"/>-->
         <xsl:choose>
           <xsl:when test="string-length($brReplaced) > 300">
             <tr>
              <td class="description" colspan="2" style="font-size:24px;">
                <xsl:value-of select="$brReplaced"/>
              </td>
             </tr>
           </xsl:when>
           <xsl:otherwise>
             <tr>
               <td class="description" colspan="2" style="font-size:30px;">
                <xsl:value-of select="$brReplaced"/>
               </td>
             </tr>
           </xsl:otherwise>
         </xsl:choose>
        </table>

        <table width="95%" border="0" cellspacing="5" id="itemtable" multiarrow="true">
        <tr>
          <td activeClass="ActiveButton" defaultClass="Button" class="Button">
            <xsl:attribute name="value">/servlet/KBEpgDataRes?action=01&amp;year=<xsl:value-of select="//@year" />&amp;month=<xsl:value-of select="//@month" />&amp;day=<xsl:value-of select="//@day" />&amp;start=<xsl:value-of select="//@hour" />&amp;selected=<xsl:value-of select="//title" />-<xsl:value-of select="//channel" /></xsl:attribute>
            <xsl:attribute name="id">item_<xsl:value-of select="0" /></xsl:attribute>
            <xsl:attribute name="confirm">false</xsl:attribute>
            <xsl:attribute name="width">33%</xsl:attribute>
            EPG
          </td>
          <td activeClass="ActiveButton" defaultClass="Button" class="Button">
            <xsl:attribute name="value"><xsl:value-of select="//addURL" /></xsl:attribute>
            <xsl:attribute name="id">item_<xsl:value-of select="1" /></xsl:attribute>
            <xsl:attribute name="confirm">false</xsl:attribute>
            <xsl:attribute name="width">33%</xsl:attribute>
            Schedule
          </td>
          <td activeClass="ActiveButton" defaultClass="Button" class="Button">
            <xsl:attribute name="value"><xsl:value-of select="//autoAddURL" /></xsl:attribute>
            <xsl:attribute name="id">item_<xsl:value-of select="2" /></xsl:attribute>
            <xsl:attribute name="confirm">true</xsl:attribute>
            <xsl:attribute name="width">33%</xsl:attribute>
            Auto-Add
          </td>
        </tr>
      </table>
    </td></tr>
  </table>

  <div id="confirm" style="position:absolute; left:0; top:0; visibility: hidden; ">
    <table border="0" cellspacing="15" bgcolor="#386EB8">
      <tr>
        <td colspan="2" class="questionText">
        <div id="confirmText">Please Confirm The Action!</div>
        </td>
      </tr>
      <tr>
        <td id="item2_0" value="yes" activeClass="ActiveButton" defaultClass="Button" class="Button" width="200px">Yes</td>
        <td id="item2_1" value="no" activeClass="ActiveButton" defaultClass="Button" class="Button" width="200px">No</td>
      </tr>
    </table>
  </div>

  </body>
  </html>
	</xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
