<!DOCTYPE xsl:stylesheet [ 
<!ENTITY nbsp "&#160;">
<!ENTITY space "<xsl:text> </xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
   
      <html>
      <head>
      <title>TV Scheduler Pro System Status</title>
      <link rel="stylesheet" href="/css/main.css" type="text/css"/>
      <xsl:comment>[if lt IE 7.0]&gt;
      &lt;script src="/javascript/iepng.js"&gt;&lt;/script&gt;
      &lt;![endif]</xsl:comment>
      <script src="/javascript/main.js"></script>
      <script src="/javascript/refresh.js"></script>
      
<style>
td.rowcol0
{
    background-color: #6F92FA;
}

td.rowcol1
{
    background-color: #5F86FA;
}
</style>      
      
      </head>
      <body>
      <center>

      <br/><br/>
      <img border="0" src="/images/logo.png" align="absmiddle" width="64" height="64"/> 
      <span class="pageTitle"> TV Scheduler Pro</span>
      <br/>
      <br/>
      
      
      <table cellpadding="5" width="90%" border="0">
      
      <tr>
      
      <td valign="top" nowrap="1" rowspan="1" width="10px">
      
      <table border="0">

      <tr>
      <td nowrap="1">
      <a href="/servlet/ScheduleDataRes" class="noUnder">
      <img border="0" src="/images/schedule.png" align="absmiddle" width="48" height="48"/> 
      <span class="areaTitle">Schedules</span>
      </a>
      <br/>
      <br/>
      </td>
      </tr>

      <tr>
      <td nowrap="1">
      <a class="noUnder" href="/servlet/EpgDataRes?action=12&amp;scrollto=-1"> 
      <img border="0" src="/images/epg.png" align="absmiddle" width="48" height="48"/> 
      <span class="areaTitle">Show EPG</span>
      </a>
      <br/>
      <br/>
      </td>
      </tr>
      
      <tr>
      <td nowrap="1">
      <a class="noUnder" href="/servlet/EpgDataRes?action=22">
      <img border="0" src="/images/search.png" align="absmiddle" width="48" height="48"/> 
      <span class="areaTitle">Search EPG</span>
      </a>
      <br/>
      <br/>
      </td>
      </tr>
           
      <tr>
      <td nowrap="1">
      <a class="noUnder" href="/servlet/EpgAutoAddDataRes?action=36"> 
      <img border="0" src="/images/now_next.png" align="absmiddle" width="48" height="48"/> 
      <span class="areaTitle">Watch List</span>      
      </a>
      <br/>
      <br/>
      </td>
      </tr>
      
      <tr>
      <td nowrap="1">      
      <a href="/settings.html" class="noUnder">
      <img border="0" src="/images/settings.png" align="absmiddle" width="48" height="48"/> 
      <span class="areaTitle">System</span>
      </a>
      <br/>
      <br/>
      </td>
      </tr>
      
      </table>
      
      </td>
      <td valign="top">
      
      <table width="100%" border="0">
      <tr><td>
      
      <table width="100%" border="0">
	   <tr>
      <td colspan="3" style="border: solid #FFFFFF 1px;">
      <b>Now Running:</b>     
      </td>
      </tr>	
      
      <xsl:if test="count(//now_running/item) > 0">
         <xsl:apply-templates select="//now_running/item" />
      </xsl:if>
      <xsl:if test="count(//now_running/item) = 0">
	      <tr>
         <td colspan="3">
         No running captures
         </td>
         </tr>	
      </xsl:if>
      
      </table>
      <p/>
      
      <table width="100%" border="0">
	   <tr>
      <td colspan="3" style="border: solid #FFFFFF 1px;">
      <b>Next Schedule:</b>     
      </td>
      </tr>	
      
      <xsl:if test="count(//next/item) > 0">
         <xsl:apply-templates select="//next/item" />
      </xsl:if>
      <xsl:if test="count(//next/item) = 0">
	      <tr>
         <td colspan="3">
         No pending schedules
         </td>
         </tr>	
      </xsl:if>
      
      </table>
      <p/>      
      
      
      
      <table width="100%" border="0" class="">
      
	  <tr>
	  
      <td colspan="1" style="border: solid #FFFFFF 1px; width:1%; overflow:hidden;"><b>EPG:</b></td>	  
       
      <td colspan="1" style="border: solid #FFFFFF 1px; overflow:hidden;"><b>Now</b></td>
      <td colspan="1" style="border: solid #FFFFFF 1px; width:1%"><b>Time</b></td>     
       
      <td colspan="1" style="border: solid #FFFFFF 1px; overflow:hidden;"><b>Next</b></td>      
      <td colspan="1" style="border: solid #FFFFFF 1px; width:1%; overflow:hidden;"><b>Time</b></td>       
      
      </tr>
      
      <xsl:if test="count(//now_and_next/channel) > 0">
         <xsl:apply-templates select="//now_and_next/channel" />
      </xsl:if>
      <xsl:if test="count(//now_and_next/channel) = 0">
	      <tr>
         <td colspan="4">
         No EPG items
         </td>
         </tr>	
      </xsl:if>
      
      </table>
      
      <p/>   
      
      </td></tr>
      </table>
      
      
      <table width="100%" border="0">
	  <tr>
      <td colspan="2" style="border: solid #FFFFFF 1px;">
      <b>Free Space</b>     
      </td>
      </tr>	
      <xsl:apply-templates select="//freeSpace/drive" />	
      </table>
   

      </td></tr>
      <tr><td colspan="2" valign="bottom" align="right">
      <table>   
            
      
      
      <xsl:apply-templates select="//version" />
      <xsl:apply-templates select="//time" />
      
      </table>     
      
      </td>
      </tr>
      
      
      </table>
      </center>
      </body>
      </html>
   
	</xsl:template>
	
	<xsl:template match="freeSpace/drive">
      <tr>
      <td nowrap="1">
      
      <a>
         <xsl:attribute name="href">/servlet/FileManagementDataRes?path=<xsl:value-of select="@path"/></xsl:attribute>
         <img src="/images/managefiles.png" border="0" align="absmiddle" width="24" height="24" title="Browse Path"/>
      </a>&nbsp;<xsl:value-of select="@path" />
      </td>
      <td nowrap="1" align="right">
         <xsl:value-of select="@free" />
      </td>      
      </tr>	
	</xsl:template>		
	
	
	<xsl:template match="now_and_next/channel" >
	  <tr>
	  
      <td style="overflow:hidden; white-space:nowrap; padding-right:10px;">
      <xsl:attribute name="class">rowcol<xsl:value-of select="position() mod 2"/></xsl:attribute>
      <xsl:value-of select="@ws_channel" />
      </td>
      
      <td style="overflow:hidden; white-space:nowrap;">
      <xsl:attribute name="class">rowcol<xsl:value-of select="position() mod 2"/></xsl:attribute>
      <b>
      <span style="cursor: pointer; cursor: hand;">
      <xsl:attribute name="onClick">openDetails('/servlet/EpgDataRes?action=06&amp;id=<xsl:value-of select="now/id" />&amp;channel=<xsl:value-of select="@ws_channel" />');</xsl:attribute>
      <xsl:variable name="maxSize" select="75" />
      <xsl:choose>   
      <xsl:when test="string-length(now/name) &lt; $maxSize">
         <xsl:value-of select="now/name" />
      </xsl:when>
      <xsl:otherwise>
          <xsl:value-of select="substring(now/name, 0, number($maxSize)-2)" />...
      </xsl:otherwise>
      </xsl:choose> 
      </span>
      </b>      
      </td>
      
      <td style="overflow:hidden; white-space:nowrap; padding-right:10px;">
      <xsl:attribute name="class">rowcol<xsl:value-of select="position() mod 2"/></xsl:attribute>
      <xsl:if test="string-length(now/start/@hour_12) &gt; 0">    
	      <xsl:value-of select="now/start/@hour_12" />:<xsl:value-of select="now/start/@minute" /><xsl:value-of select="now/start/@am_pm" /> (<xsl:value-of select="now/duration" />)
      </xsl:if>
      </td>
      
      <td style="overflow:hidden; white-space:nowrap;">
      <xsl:attribute name="class">rowcol<xsl:value-of select="position() mod 2"/></xsl:attribute>
      <b>
      <span style="cursor: pointer; cursor: hand;">
      <xsl:attribute name="onClick">openDetails('/servlet/EpgDataRes?action=06&amp;id=<xsl:value-of select="next/id" />&amp;channel=<xsl:value-of select="@ws_channel" />');</xsl:attribute>
      <xsl:value-of select="next/name" />
      </span>
      </b>
      </td>
      
      <td nowrap="1" style="overflow:hidden; white-space:nowrap;">
      <xsl:attribute name="class">rowcol<xsl:value-of select="position() mod 2"/></xsl:attribute>
      <xsl:if test="string-length(next/start/@hour_12) &gt; 0">    
         <xsl:value-of select="next/start/@hour_12" />:<xsl:value-of select="next/start/@minute" /><xsl:value-of select="next/start/@am_pm" /> (<xsl:value-of select="next/duration" />)
      </xsl:if>
      </td>  
        
      </tr>	
	</xsl:template>		
	
	
	<xsl:template match="next/item" >
	  <tr>
      <td nowrap="1">

      <a>
         <xsl:attribute name="href">/servlet/EpgDataRes?action=25&amp;id=<xsl:value-of select="id"/></xsl:attribute>
         <img src="/images/epg.png" border="0" align="absmiddle" width="24" height="24" title="Show in EPG"/>
      </a>&nbsp;<a onClick="return confirm('Are you sure you want to delete this item?');">
         <xsl:attribute name="href">/servlet/ScheduleDataRes?action=04&amp;id=<xsl:value-of select="id"/></xsl:attribute>
         <img src="/images/delete.png" border="0" align="absmiddle" alt="Delete" width="24" height="24" title="Delete Schedule"/>
      </a>&nbsp;<xsl:value-of select="start/@hour_12" />:<xsl:value-of select="start/@minute" /><xsl:value-of select="start/@am_pm" /> <span title="Duration in minutes"> (<xsl:value-of select="duration" />) </span>
      </td>
      <td nowrap="1" width="100%">      
      <xsl:value-of select="name" /> 
      <span title="Channel">  (<xsl:value-of select="channel" />) </span>
      </td>
      <td nowrap="1"> 
      <!--<xsl:value-of select="time_to_action/@days" /> d, <xsl:value-of select="time_to_action/@hours" /> h, <xsl:value-of select="time_to_action/@minutes" /> m, <xsl:value-of select="time_to_action/@seconds" /> s-->
      <span title="Time to Schedule"><xsl:value-of select="(time_to_action/@days * 24) + time_to_action/@hours" /> h <xsl:value-of select="time_to_action/@minutes" /> m</span>
      </td>
      </tr>	
	</xsl:template>		

	<xsl:template match="now_running/item" >
	   <tr>
      <td nowrap="1">

      <a onClick="return confirm('Are you sure you want to stop this capture?');">
         <xsl:attribute name="href">/servlet/ScheduleDataRes?action=09&amp;id=<xsl:value-of select="id"/></xsl:attribute>
         <img src="/images/stop.png" align="absmiddle" border="0" width="24" height="24" title="Stop Capture" />
      </a>&nbsp;<xsl:value-of select="start/@hour_12" />:<xsl:value-of select="start/@minute" /><xsl:value-of select="start/@am_pm" /> (<xsl:value-of select="duration" />)
      </td>
      <td nowrap="1" width="100%">      
      <xsl:value-of select="name" />
      <span title="Channel">  (<xsl:value-of select="channel" />) </span>
      </td>
      <td nowrap="1"> 
      <xsl:value-of select="status" />
      </td>
      </tr>	
	</xsl:template>	

   
	<xsl:template match="version" >
   
 
      <tr><td>Scheduler Version</td><td>: <xsl:value-of select="@scheduler" /></td></tr>
      <tr><td>Capture Engine</td><td>: <xsl:value-of select="@capture_engine" /></td></tr>
   
	</xsl:template>
	
	<xsl:template match="time" >
   
 
      <tr><td>Server Time</td><td>: <xsl:value-of select="@hour_12" />:<xsl:value-of select="@minute" /><xsl:value-of select="@am_pm" /></td></tr>
   
	</xsl:template>
   
</xsl:stylesheet>

