/*
 * CVS Revision Graph Plus IntelliJ IDEA Plugin
 *
 * Copyright (C) 2011, Łukasz Zieliński
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.cvstoolbox.graph.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class DateTimeFormatTokenMap {
  protected static HashMap<Character,Integer> _specialCharMap = null;
  protected static HashMap<Character,String> _specialCharDescriptionMap = null;
  protected static char _specialChars[] = null;

  protected static void init()
  {
    if(_specialCharMap == null) {
      _specialCharMap = new HashMap<Character,Integer>();
      _specialCharMap.put('G',Calendar.ERA);
      _specialCharMap.put('y',Calendar.YEAR);
      _specialCharMap.put('M',Calendar.MONTH);
      _specialCharMap.put('w',Calendar.WEEK_OF_YEAR);
      _specialCharMap.put('W',Calendar.WEEK_OF_MONTH);
      _specialCharMap.put('D',Calendar.DAY_OF_YEAR);
      _specialCharMap.put('d',Calendar.DAY_OF_MONTH);
      _specialCharMap.put('F',Calendar.DAY_OF_WEEK_IN_MONTH);
      _specialCharMap.put('E',Calendar.DAY_OF_WEEK);
      _specialCharMap.put('a',Calendar.AM_PM);
      _specialCharMap.put('H',Calendar.HOUR_OF_DAY);
      _specialCharMap.put('k',Calendar.HOUR_OF_DAY);
      _specialCharMap.put('K',Calendar.HOUR);
      _specialCharMap.put('h',Calendar.HOUR);
      _specialCharMap.put('m',Calendar.MINUTE);
      _specialCharMap.put('s',Calendar.SECOND);
      _specialCharMap.put('S',Calendar.MILLISECOND);
      _specialCharMap.put('z',Calendar.ZONE_OFFSET);
      _specialCharMap.put('Z',Calendar.ZONE_OFFSET);
    }
    if(_specialCharDescriptionMap == null) {
      _specialCharDescriptionMap = new HashMap<Character,String>();
      _specialCharDescriptionMap.put('G',"Era designator");
      _specialCharDescriptionMap.put('y',"Year");
      _specialCharDescriptionMap.put('M',"Month in year");
      _specialCharDescriptionMap.put('w',"Week in year");
      _specialCharDescriptionMap.put('W',"Week in month");
      _specialCharDescriptionMap.put('D',"Day in year");
      _specialCharDescriptionMap.put('d',"Day in month");
      _specialCharDescriptionMap.put('F',"Day of week in month");
      _specialCharDescriptionMap.put('E',"Day in week");
      _specialCharDescriptionMap.put('a',"Am/pm marker");
      _specialCharDescriptionMap.put('H',"Hour in day (0-23)");
      _specialCharDescriptionMap.put('k',"Hour in day (1-24)");
      _specialCharDescriptionMap.put('K',"Hour in am/pm (0-11)");
      _specialCharDescriptionMap.put('h',"Hour in am/pm (1-12)");
      _specialCharDescriptionMap.put('m',"Minute in hour");
      _specialCharDescriptionMap.put('s',"Second in minute");
      _specialCharDescriptionMap.put('S',"Millisecond");
      _specialCharDescriptionMap.put('z',"General time zone");
      _specialCharDescriptionMap.put('Z',"RFC time zone");
    }
    if(_specialChars == null) {
      _specialChars = new char[19];
      _specialChars[0] = 'G';
      _specialChars[1] = 'y';
      _specialChars[2] = 'M';
      _specialChars[3] = 'w';
      _specialChars[4] = 'W';
      _specialChars[5] = 'D';
      _specialChars[6] = 'd';
      _specialChars[7] = 'F';
      _specialChars[8] = 'E';
      _specialChars[9] = 'a';
      _specialChars[10] = 'H';
      _specialChars[11] = 'k';
      _specialChars[12] = 'K';
      _specialChars[13] = 'h';
      _specialChars[14] = 'm';
      _specialChars[15] = 's';
      _specialChars[16] = 'S';
      _specialChars[17] = 'z';
      _specialChars[18] = 'Z';
      Arrays.sort(_specialChars);
    }
  }

  public static char[] getSpecialChars()
  {
    init();
    return(_specialChars);
  }

  public static String getSpecialCharDescription(Character specialCharacter)
  {
    init();
    String descrip = _specialCharDescriptionMap.get(specialCharacter);
    return(descrip);
  }

  public static int getCalendarField(char special)
  {
    init();
    Integer field = _specialCharMap.get(new Character(special));
    if(field == null)
      return(-1);
    else
      return(field);
  }
}