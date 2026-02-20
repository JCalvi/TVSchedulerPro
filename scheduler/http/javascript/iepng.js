

function correctPNG() // correctly handle PNG transparency in Win IE 5.5 or higher.
{
   
   for(var i=0; i < document.images.length; i++)
   {      
	   var img = document.images[i];

   	var imgName = img.src.toUpperCase();
   	if (imgName.substring(imgName.length-3, imgName.length) == "PNG")
   	{
   	   img.style.cssText = img.style.cssText + 
   	      ";filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\'" + 
   	      img.src + "\', sizingMethod='scale');";

         img.src = "/images/blank.gif";
   	}
   	else
   	{
   	   img.style.cssText = "filter: alpha(opacity=100);";
   	}
   }

}

document.write("<style type='text/css'>img {filter: alpha(opacity=0);}</style>");
window.attachEvent("onload", correctPNG);
