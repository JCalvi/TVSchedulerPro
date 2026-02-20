/*
* Copyright (c) 2009 Blue Bit Solutions
* Copyright (c) 2010-2024 John Calvi
*
* This file is part of TV Scheduler Pro
*
* TV Scheduler Pro is free software: you can redistribute it and/or
* modify it under the terms of the GNU General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* TV Scheduler Pro is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with TV Scheduler Pro.
* If not, see <http://www.gnu.org/licenses/>.
*/

public class Base64
{
   private static byte[] base64CharTable;

   private static final char[] S_BASE64CHAR =
   { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
         'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
         'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
         'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
         '5', '6', '7', '8', '9', '+', '/' };

   private static final char S_BASE64PAD = '=';

   private Base64(){}

   public static byte[] decodeBase64(String base64)
   {
      make64CharTable();

      // at most 3 bytes per 4 chars:
      byte[] temp = new byte[3 * (base64.length() / 4)], quad = new byte[4];

      int j = 0, // number of decoded bytes
      k = 0; // number of digits in current quad
      for (int i = 0; i < base64.length(); i++)
      {
         char c = base64.charAt(i);
         if (c >= 0 && c < 128 && base64CharTable[c] >= 0) // is it a base 64 char
         {
            quad[k++] = base64CharTable[c];
            if (k == 4)
            { // full quad?
               // decode the 3 bytes:
               temp[j++] = (byte) ((quad[0] << 2) + (quad[1] >> 4));
               temp[j++] = (byte) ((quad[1] << 4) + (quad[2] >> 2));
               temp[j++] = (byte) ((quad[2] << 6) + quad[3]);
               k = 0; // restart quad
            }
         }
      }

      // handle padding:
      if (quad[2] == base64CharTable['=']) {
		j -= 2;
	  } else if (quad[3] == base64CharTable['=']) {
		j--;
	  }

      // copy into smaller array:
      byte[] data = new byte[j];
      for (int i = 0; i < j; i++) {
		data[i] = temp[i];
	  }

      return data;
   }

   private static void make64CharTable()
   {
      if (base64CharTable != null) {
		return;
	  }

      base64CharTable = new byte[128];

      for (int i = 0; i < 128; i++) {
		base64CharTable[i] = -1; // invalid characters are negative
	  }

      byte j = 0;
      char c;

      for (c = 'A'; c <= 'Z'; c++) {
		base64CharTable[c] = j++;
	  }

      for (c = 'a'; c <= 'z'; c++) {
		base64CharTable[c] = j++;
	  }

      for (c = '0'; c <= '9'; c++) {
		base64CharTable[c] = j++;
	  }

      base64CharTable['+'] = 62;
      base64CharTable['/'] = 63;
      base64CharTable['='] = 64; // '=' must be valid and unique
   }

   public static String encode(byte[] data)
   {
      int off = 0;
      int len = data.length;

      if (len <= 0) {
		return "";
	  }

      char[] out = new char[len / 3 * 4 + 4];
      int rindex = off;
      int windex = 0;
      int rest = len - off;
      while (rest >= 3)
      {
         int i = ((data[rindex] & 0xff) << 16)
               + ((data[rindex + 1] & 0xff) << 8) + (data[rindex + 2] & 0xff);
         out[windex++] = S_BASE64CHAR[i >> 18];
         out[windex++] = S_BASE64CHAR[(i >> 12) & 0x3f];
         out[windex++] = S_BASE64CHAR[(i >> 6) & 0x3f];
         out[windex++] = S_BASE64CHAR[i & 0x3f];
         rindex += 3;
         rest -= 3;
      }
      if (rest == 1)
      {
         int i = data[rindex] & 0xff;
         out[windex++] = S_BASE64CHAR[i >> 2];
         out[windex++] = S_BASE64CHAR[(i << 4) & 0x3f];
         out[windex++] = S_BASE64PAD;
         out[windex++] = S_BASE64PAD;
      }
      else if (rest == 2)
      {
         int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
         out[windex++] = S_BASE64CHAR[i >> 10];
         out[windex++] = S_BASE64CHAR[(i >> 4) & 0x3f];
         out[windex++] = S_BASE64CHAR[(i << 2) & 0x3f];
         out[windex++] = S_BASE64PAD;
      }
      return new String(out, 0, windex);
   }


}