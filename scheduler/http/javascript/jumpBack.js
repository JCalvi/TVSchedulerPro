

function jumpBack()
{
   var defaultBack = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
   var cookieName = "backURL";
   
   var theCookie = "" + document.cookie;
   var ind = theCookie.indexOf(cookieName);
   
   if (ind == -1)
   {
      document.location.href = defaultBack;
      return; 
   }
      
   var ind1 = theCookie.indexOf(';', ind);
   
   if (ind1 == -1)
   {
      document.location.href = defaultBack;
      ind1 = theCookie.length; 
   }
            
   var backURL = unescape(theCookie.substring(ind + cookieName.length + 1, ind1));
   
   if(backURL.length > 0)
   {
      document.location.href = backURL;
   }
   else
   {
      document.location.href = defaultBack;
   }
}