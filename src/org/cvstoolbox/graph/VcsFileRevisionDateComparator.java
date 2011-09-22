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
import java.util.Date;

public class VcsFileRevisionDateComparator implements Comparator<VcsFileRevision> {
  public int compare(VcsFileRevision r1,VcsFileRevision r2)
  {
    int result;
    result = compareDates(r1.getRevisionDate(),r2.getRevisionDate());
    return(result);
  }

  public int compareDates(Date d1,Date d2)
  {
    if((d1 == null) && (d2 == null))
      return(0);
    if((d1 == null))
      return(-1);
    if((d2 == null))
      return(1);
    Long d1L = d1.getTime();
    Long d2L = d2.getTime();
    return(d1L.compareTo(d2L));
  }
}
