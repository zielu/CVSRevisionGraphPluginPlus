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

import java.util.StringTokenizer;
import java.util.Arrays;
import java.text.SimpleDateFormat;

public class DateTimeFormatTokenizer {
  protected String _pattern = null;
  protected int _index = -1;
  protected boolean _stripQuotes = true;

  public DateTimeFormatTokenizer(SimpleDateFormat sdf,boolean stripQuotes)
  {
    _pattern = sdf.toPattern();
    _stripQuotes = stripQuotes;
  }

  public DateTimeFormatTokenizer(String pattern,boolean stripQuotes)
  {
    _pattern = pattern;
    _stripQuotes = stripQuotes;
  }

  public boolean hasMoreTokens()
  {
    if(_index < (_pattern.length() - 1))
      return(true);
    else
      return(false);
  }

  public DateTimeFormatToken nextToken()
  {
    StringBuffer buf = new StringBuffer();
    String retVal;

    _index++;
    char ch = _pattern.charAt(_index);
    buf.append(ch);
    if(Arrays.binarySearch(DateTimeFormatTokenMap.getSpecialChars(),ch) >= 0) {
      //Keep looking for the same special character repeating
      while((_index < (_pattern.length() - 1)) && (_pattern.charAt(_index + 1) == ch)) {
        buf.append(ch);
        _index++;
      }
    } else {
      //See if a quoted string
      if(ch == '\'') {
        //Keep looking for anything until hit matching quote
        while(_index < (_pattern.length() - 1)) {
          char nch = _pattern.charAt(_index + 1);
          buf.append(nch);
          _index++;
          //Check for matching quote
          if(nch == '\'') {
            //Check if another quote is immediately following this one to signal a quote literal
            if((_index < (_pattern.length() - 1)) && (_pattern.charAt(_index + 1) == '\'')) {
              buf.append(nch);
              _index++;
            } else
              break;
          }
        }
      } else {
        //Keep looking for non-special non-quoted characters
        if(_index < (_pattern.length() - 1)) {
          char nch = _pattern.charAt(_index + 1);
          while((Arrays.binarySearch(DateTimeFormatTokenMap.getSpecialChars(),nch) < 0) && (nch != '\'')) {
            buf.append(nch);
            _index++;
            if(_index < (_pattern.length() - 1))
              nch = _pattern.charAt(_index + 1);
            else
              break;
          }
        }
      }
    }
    retVal = buf.toString();
    if(_stripQuotes) {
      //Strip out quotes and replace double quotes with single quote literal
      StringTokenizer stokens = new StringTokenizer(buf.toString(),"'",true);
      StringBuffer buf2 = new StringBuffer();
      boolean lastTokenWasApost = false;
      while(stokens.hasMoreTokens()) {
        String stoken = stokens.nextToken();
        if(stoken.equals("'")) {
          if(lastTokenWasApost) {
            buf2.append("'");
            lastTokenWasApost = false;
          } else
            lastTokenWasApost = true;
        } else {
          buf2.append(stoken);
          lastTokenWasApost = false;
        }
      }
      retVal = buf2.toString();
    }
    DateTimeFormatToken token = new DateTimeFormatToken(retVal);
    return(token);
  }

  public static void main(String args[])
  {
//    SimpleDateFormat sdf = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance();
    DateTimeFormatTokenizer tokens = new DateTimeFormatTokenizer("HHmmss'nowHHMM''':;.><'''men'''",true);
    while(tokens.hasMoreTokens()) {
      DateTimeFormatToken token = tokens.nextToken();
      System.out.println("'" + token + "' (" + token.isSpecial() + ")");
    }
  }
}