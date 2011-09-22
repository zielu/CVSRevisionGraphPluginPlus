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

package org.cvstoolbox.graph;

import com.intellij.openapi.vcs.history.VcsFileRevision;

import java.util.Comparator;

public class RevisionStringComparator implements Comparator<Object> {
  public int compare(Object o1,Object o2)
  {
    int result;
    if((o1 instanceof String) && (o2 instanceof String)) {
      String s1 = (String)o1;
      String s2 = (String)o2;
      result = compareRevisionStrings(s1,s2);
    } else {
      VcsFileRevision r1 = (VcsFileRevision)o1;
      VcsFileRevision r2 = (VcsFileRevision)o2;
      result = compareRevisionStrings(r1.getRevisionNumber().asString(),r2.getRevisionNumber().asString());
    }
    return(result);
  }

  public int compareRevisionStrings(String rev1,String rev2)
  {
    if((rev1 == null) && (rev2 == null))
      return(0);
    if((rev1 == null))
      return(-1);
    if((rev2 == null))
      return(1);
    String rev1Parts[] = rev1.split("\\.");
    String rev2Parts[] = rev2.split("\\.");
    int i = 0;
    while((i < rev1Parts.length) && (i < rev2Parts.length)) {
      Integer rev1Part = new Integer(rev1Parts[i]);
      Integer rev2Part = new Integer(rev2Parts[i]);
      int result = rev1Part.compareTo(rev2Part);
      if(result != 0)
        return(result);
      i++;
    }
    if((i >= rev1Parts.length) && (i >= rev2Parts.length))
      return(0);
    else if((i < rev1Parts.length) && (i >= rev2Parts.length))
      return(1);
    else
      return(-1);
  }
}
