<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

   <xsl:template match="/">
     
      <html>
      <head>
      
      <style>
      body
      {
         background-image: url(/themes/calvi/images/background.jpg);
         background-attachment:fixed;
         background-repeat: no-repeat;
         background-size: cover;
      }
      </style>
      
      </head>
      
      <body>
      
      <IFRAME SRC="/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons"
      WIDTH="100%" HEIGHT="100%" 
      SCROLLING="no" 
      FRAMEBORDER="0" 
      allowtransparency="true" 
      background-color="transparent">      
      </IFRAME>
      
      </body>
      </html>

   </xsl:template>
   
</xsl:stylesheet>

