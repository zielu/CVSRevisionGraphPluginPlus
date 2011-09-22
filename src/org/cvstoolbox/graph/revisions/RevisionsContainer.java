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

/*
 * @(#) $Id:  $
 */
package org.cvstoolbox.graph.revisions;

import com.intellij.cvsSupport2.history.CvsFileRevision;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import org.cvstoolbox.graph.CVSRevisionGraph;
import org.cvstoolbox.graph.RevisionStringComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RevisionsContainer {
    private final RevisionStringComparator _revCompare;
    private final Map<BranchRevision,List<VcsFileRevision>> _revMap =
            new LinkedHashMap<BranchRevision, List<VcsFileRevision>>();

    private boolean _showRevisionFilter = false;

    private boolean _showBranchFilter = false;
    private List<String> _branchFilter = null;

    private String _afterDateTime = null;
    private String _beforeDateTime = null;

    private boolean _afterDateTimeFilter = false;
    private boolean _beforeDateTimeFilter = false;

    public RevisionsContainer(RevisionStringComparator revCompare) {
        _revCompare = revCompare;
    }

    public void set_showRevisionFilter(boolean _showRevisionFilter) {
        this._showRevisionFilter = _showRevisionFilter;
    }

    public void set_showBranchFilter(boolean _showBranchFilter) {
        this._showBranchFilter = _showBranchFilter;
    }

    public void set_branchFilter(List<String> _branchFilter) {
        this._branchFilter = _branchFilter;
    }

    public void set_afterDateTime(String _afterDateTime) {
        this._afterDateTime = _afterDateTime;
    }

    public void set_beforeDateTime(String _beforeDateTime) {
        this._beforeDateTime = _beforeDateTime;
    }

    public void set_afterDateTimeFilter(boolean _afterDateTimeFilter) {
        this._afterDateTimeFilter = _afterDateTimeFilter;
    }

    public void set_beforeDateTimeFilter(boolean _beforeDateTimeFilter) {
        this._beforeDateTimeFilter = _beforeDateTimeFilter;
    }

    public String getParentRevision(String revision) {
        if (revision == null) {
            return (null);
        }
        int index = revision.lastIndexOf('.');
        if (index == -1) {
            return (null);
        }
        //Strip off last dot and number
        return (revision.substring(0, index));
    }

    public String getGrandParentRevision(String revision) {
        return getParentRevision(getParentRevision(revision));
    }

    public boolean isRevisionBranch(String revision) {
        String revParts[] = revision.split("\\.");
        return ((revParts.length % 2) != 0);
    }

    public List<VcsFileRevision> getRevisionsOnBranch(BranchRevision branch) {
        return _revMap.get(branch);
    }

    public Collection<BranchRevision> getBranches() {
        return _revMap.keySet();
    }

    public Map<BranchRevision,List<VcsFileRevision>> getRevisions() {
        return _revMap;
    }

    public void convertToRevisionMap(List<VcsFileRevision> revisions) {
        _revMap.clear();
        HashMap<String, List<VcsFileRevision>> revMap = new HashMap<String, List<VcsFileRevision>>();
        List<String> branchRevisionList = new ArrayList<String>();
        HashMap<String, String> branchNameMap = new HashMap<String, String>();
        branchNameMap.put("1", "HEAD");
        for (VcsFileRevision rev : revisions) {
            String branchRevision = getParentRevision(rev.getRevisionNumber().asString());
            if (!branchRevisionList.contains(branchRevision)) {
                branchRevisionList.add(branchRevision);
            }
            if (rev instanceof CvsFileRevision) {
                CvsFileRevision cvsRev = (CvsFileRevision) rev;
                Collection<String> branches = cvsRev.getBranches();
                for (String branch : branches) {
                    String branchParts[] = branch.split(" *[()]");
                    branchNameMap.put(branchParts[1], branchParts[0]);
                    if (!branchRevisionList.contains(branchParts[1])) {
                        branchRevisionList.add(branchParts[1]);
                    }
                }
            }
            List<VcsFileRevision> revsOnBranch = revMap.get(branchRevision);
            if (revsOnBranch == null) {
                revsOnBranch = new ArrayList<VcsFileRevision>();
                revMap.put(branchRevision, revsOnBranch);
            }
            boolean passesFilter = true;
            if (_afterDateTimeFilter) {
                Date afterDateTime = null;
                try {
                    afterDateTime = CVSRevisionGraph._dateTimeFormat.parse(_afterDateTime);
                } catch (Exception e) {
                    //Ignore
                }
                if ((afterDateTime != null) && (afterDateTime.compareTo(rev.getRevisionDate()) > 0)) {
                    passesFilter = false;
                }
            }
            if (_beforeDateTimeFilter) {
                Date beforeDateTime = null;
                try {
                    beforeDateTime = CVSRevisionGraph._dateTimeFormat.parse(_beforeDateTime);
                } catch (Exception e) {
                    //Ignore
                }
                if ((beforeDateTime != null) && (beforeDateTime.compareTo(rev.getRevisionDate()) < 0)) {
                    passesFilter = false;
                }
            }
            if ((_afterDateTimeFilter) || (_beforeDateTimeFilter)) {
                if (_showRevisionFilter ? passesFilter : !passesFilter) {
                    revsOnBranch.add(rev);
                }
            } else {
                revsOnBranch.add(rev);
            }
        }
        //Sort everything
        Collections.sort(branchRevisionList, _revCompare);
        for (List<VcsFileRevision> revsOnBranch : revMap.values()) {
            Collections.sort(revsOnBranch, _revCompare);
        }
        //Fill up return value in correct order
        for (String branchRevision : branchRevisionList) {
            String branchName = branchNameMap.get(branchRevision);
            if (branchName == null) {
                branchName = "<unnamed>";
            }
            if (!_branchFilter.isEmpty()) {
                boolean contains = _branchFilter.contains(branchName);
                if (_showBranchFilter ? !contains : contains) {
                    continue;
                }
            }
            BranchRevision branch = new BranchRevision(branchName, branchRevision);
            List<VcsFileRevision> revsOnBranch = revMap.remove(branchRevision);
            if (revsOnBranch == null) {
                revsOnBranch = Collections.emptyList();
            }
            _revMap.put(branch, revsOnBranch);
        }
    }

    public void dispose() {
        _revMap.clear();
    }
}
