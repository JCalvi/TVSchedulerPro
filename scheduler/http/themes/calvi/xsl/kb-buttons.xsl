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
        <tr><td align="center" valign="middle">
          <xsl:if test="string-length(//@title) > 0">
             <div class="PageTitle"><xsl:value-of select="//@title" /></div>
          </xsl:if>
          <table class='KBButtonListTable' id="itemtable" multiarrow="true">
            <xsl:apply-templates select="//button" mode="cell" />      
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
  
	<xsl:template match="button" mode="cell">
      <tr>
        <td activeClass="ActiveButton" defaultClass="Button" class="Button">
          <xsl:attribute name="value"><xsl:value-of select="url" /></xsl:attribute>  
          <xsl:attribute name="id">item_<xsl:value-of select="position()-1" /></xsl:attribute>
          <xsl:attribute name="confirm"><xsl:value-of select="confirm" /></xsl:attribute>
          <xsl:attribute name="width">500</xsl:attribute>
          <span class="solidText">
            <xsl:value-of select="@name"/>
            <xsl:if test="@name='Sun'">day</xsl:if>
            <xsl:if test="@name='Mon'">day</xsl:if>
            <xsl:if test="@name='Tue'">sday</xsl:if>
            <xsl:if test="@name='Wed'">nesday</xsl:if>
            <xsl:if test="@name='Thu'">rsday</xsl:if>
            <xsl:if test="@name='Fri'">day</xsl:if>
            <xsl:if test="@name='Sat'">urday</xsl:if>
          </span>
        </td>
        <td></td>
      </tr>

   
	</xsl:template>
   
</xsl:stylesheet>

