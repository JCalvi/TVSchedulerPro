<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
   
  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
    <script>
      var id = "<xsl:value-of select="//@id" />";
      var index = "<xsl:value-of select="//@index" />";
      var month = <xsl:value-of select="//@month" />;
      var year = <xsl:value-of select="//@year" />;
      var day = <xsl:value-of select="//@date" />;
      var name = "<xsl:value-of select="//name" />";
    </script>
    <script src="/themes/calvi/javascript/kbform.js"></script>
    <script src="/javascript/jumpBack.js"></script>
  </head>
  
  <body onload="Initialize();">
  
  <table class='KBButtonTable'>
    <tr><td align="center" valign="middle">
      <table>
        <tr> 
          <td class="TextR">Name</td>
          <xsl:apply-templates select="//name" mode="cell" />
        </tr>
        <tr> 
          <td class="TextR">Time</td>
            <xsl:apply-templates select="//startTimeHour" mode="cell" />
          <td class="TextC">:</td>
          <xsl:apply-templates select="//startTimeMin" mode="cell" />
            <td class="TextR">Length</td>
          <xsl:apply-templates select="//duration" mode="cell" />
        </tr>
        <tr> 
          <td class="TextR">Channel</td>
          <xsl:apply-templates select="//channel" mode="cell" />
        </tr>
        <tr> 
          <td class="TextR">Type</td>
          <xsl:apply-templates select="//type" mode="cell" />
        </tr>
        <tr> 
          <td class="TextR">Capture Type</td>
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
          <td class="TextR">Name Pattern</td>
          <xsl:apply-templates select="//pattern" mode="cell" />
        </tr> 
        <tr> 
          <td class="TextR">Capture Path</td>
          <xsl:apply-templates select="//capturePath" mode="cell" />
        </tr>         
        <tr> 
          <td class="TextR">Post Task</td>
          <xsl:apply-templates select="//posttask" mode="cell" />
        </tr> 
      </table>
    </td></tr>
  </table>
  
  <div id="keyboard" style="position:absolute; left:0; top:0; visibility: hidden; border: 3px solid #FFFFFF;">
    <table border="0" cellspacing="15" bgcolor="#386EB8">
      <tr>
        <td id="keyboardtext" colspan="10" height="40" class="FormInputSelected"></td>
      </tr>         
      <tr>
        <td width="25" align="center" id="0-0" value="1" class="FormInput">1</td>
        <td width="25" align="center" id="1-0" value="2" class="FormInput">2</td>
        <td width="25" align="center" id="2-0" value="3" class="FormInput">3</td>
        <td width="25" align="center" id="3-0" value="4" class="FormInput">4</td>
        <td width="25" align="center" id="4-0" value="5" class="FormInput">5</td>
        <td width="25" align="center" id="5-0" value="6" class="FormInput">6</td>
        <td width="25" align="center" id="6-0" value="7" class="FormInput">7</td>
        <td width="25" align="center" id="7-0" value="8" class="FormInput">8</td>
        <td width="25" align="center" id="8-0" value="9" class="FormInput">9</td>
        <td width="25" align="center" id="9-0" value="0" class="FormInput">0</td>
      </tr>
      <tr>
        <td width="25" align="center" id="0-1" value="a" class="FormInput">a</td>
        <td width="25" align="center" id="1-1" value="b" class="FormInput">b</td>
        <td width="25" align="center" id="2-1" value="c" class="FormInput">c</td>
        <td width="25" align="center" id="3-1" value="d" class="FormInput">d</td>
        <td width="25" align="center" id="4-1" value="e" class="FormInput">e</td>
        <td width="25" align="center" id="5-1" value="f" class="FormInput">f</td>
        <td width="25" align="center" id="6-1" value="g" class="FormInput">g</td>
        <td width="25" align="center" id="7-1" value="h" class="FormInput">h</td>
        <td width="25" align="center" id="8-1" value="i" class="FormInput">i</td>
        <td width="25" align="center" id="9-1" value="j" class="FormInput">j</td>
      </tr>
      <tr>
        <td width="25" align="center" id="0-2" value="k" class="FormInput">k</td>
        <td width="25" align="center" id="1-2" value="l" class="FormInput">l</td>
        <td width="25" align="center" id="2-2" value="m" class="FormInput">m</td>
        <td width="25" align="center" id="3-2" value="n" class="FormInput">n</td>
        <td width="25" align="center" id="4-2" value="o" class="FormInput">o</td>
        <td width="25" align="center" id="5-2" value="p" class="FormInput">p</td>
        <td width="25" align="center" id="6-2" value="q" class="FormInput">q</td>
        <td width="25" align="center" id="7-2" value="r" class="FormInput">r</td>
        <td width="25" align="center" id="8-2" value="s" class="FormInput">s</td>
        <td width="25" align="center" id="9-2" value="t" class="FormInput">t</td>
      </tr>         
      <tr>
        <td width="25" align="center" id="0-3" value="u" class="FormInput">u</td>
        <td width="25" align="center" id="1-3" value="v" class="FormInput">v</td>
        <td width="25" align="center" id="2-3" value="w" class="FormInput">w</td>
        <td width="25" align="center" id="3-3" value="x" class="FormInput">x</td>
        <td width="25" align="center" id="4-3" value="y" class="FormInput">y</td>
        <td width="25" align="center" id="5-3" value="z" class="FormInput">z</td>
        <td width="25" align="center" id="6-3" value="z" class="FormInput">[</td>
        <td width="25" align="center" id="7-3" value="z" class="FormInput">]</td>
        <td width="25" align="center" id="8-3" value=" " class="FormInput">_</td>
        <td width="25" align="center" id="9-3" value="&lt;" class="FormInput">&lt;</td>
      </tr>
      <tr>
        <td width="25" align="center" id="0-4" colspan="10" value="ok" class="FormInput">OK</td>
      </tr>
    </table>
  </div>      
  
  <form action="/servlet/KBScheduleDataRes" method="POST" name="data" accept-charset="UTF-8">
    <input type="hidden" name="action" value="03"/>
    <input type="hidden" name="name" value=""/>
    <input type="hidden" name="hour" value=""/>
    <input type="hidden" name="min" value=""/>
    <input type="hidden" name="duration" value=""/>
    <input type="hidden" name="channel" value=""/>
    <input type="hidden" name="type" value=""/>
    <input type="hidden" name="captype" value=""/>
    <input type="hidden" name="capPath" value=""/>
    <input type="hidden" name="month" value=""/>
    <input type="hidden" name="day" value=""/>
    <input type="hidden" name="year" value=""/>
    <input type="hidden" name="autoDel" value=""/>
    <input type="hidden" name="keepfor" value=""/>
    <input type="hidden" name="namePattern" value=""/>
    <input type="hidden" name="task" value=""/>
    <input type="hidden" name="id" value=""/>
    <input type="hidden" name="index" value=""/>
  </form>
  
  </body>
  </html>
   
	</xsl:template>

	<xsl:template match="name" mode="cell">
    <td id="name" colspan="5" class="FormInput" width="400" >
      <xsl:value-of select="//name" />
    </td>
	</xsl:template>

	<xsl:template match="startTimeHour" mode="cell">
    <td id="hour" class="FormInput" width="40">
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">HH</span>
    </td>
	</xsl:template>

	<xsl:template match="startTimeMin" mode="cell">
    <td id="min" class="FormInput" width="40">
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">MM</span>
    </td>
	</xsl:template>
	
	<xsl:template match="duration" mode="cell">	
    <td id="duration" class="FormInput" width="40">
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>	   
      <span class="solidText">DUR</span>
    </td>
	</xsl:template>	

	<xsl:template match="channel" mode="cell">	
    <td id="channel" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option" />
      <span class="solidText">CHANNEL</span>
    </td>
	</xsl:template>
	
	<xsl:template match="type" mode="cell">	
    <td id="type" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">TYPE</span>
    </td>
	</xsl:template>	

	<xsl:template match="captureType" mode="cell">	
    <td id="captype" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option_value" />
      <span class="solidText">TYPE</span>
    </td>
	</xsl:template>  

	<xsl:template match="pattern" mode="cell">	
    <td id="namePattern" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option" />
      <span class="solidText">%n</span>
    </td>
	</xsl:template>

	<xsl:template match="keepfor" mode="cell">	
    <td id="keepfor" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">30 days</span>
    </td>
	</xsl:template>	
	
	<xsl:template match="autoDel" mode="cell">	
    <td id="autoDel" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:attribute name="max"><xsl:value-of select="@max" /></xsl:attribute>
      <xsl:attribute name="min"><xsl:value-of select="@min" /></xsl:attribute>
      <xsl:attribute name="amount"><xsl:value-of select="@amount" /></xsl:attribute>
      <span class="solidText">true</span>
    </td>
	</xsl:template>		

	<xsl:template match="posttask" mode="cell">	
    <td id="postTask" colspan="5" class="FormInput" >
      <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
      <xsl:apply-templates select="option" mode="option" />
      <span class="solidText">none</span>
    </td>
	</xsl:template>   
	
	<xsl:template match="capturePath" mode="cell">	
    <td id="capturePath" colspan="5" class="FormInput" >
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