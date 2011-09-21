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

import java.util.List;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jdom.Element;

public class CVSRevisionGraphProjectComponent implements ProjectComponent,Configurable,JDOMExternalizable {
  public static final boolean DEFAULT_USE_TWO_TAG_CONVENTION = true;
  public static final boolean DEFAULT_SHOW_TAGS = true;
  public static final boolean DEFAULT_SHOW_TAG_FILTER = false;
  public static final boolean DEFAULT_SHOW_BRANCH_FILTER = false;
  public static final boolean DEFAULT_SHOW_REVISION_FILTER = false;
  public static final boolean DEFAULT_AFTER_DATE_TIME_FILTER = false;
  public static final boolean DEFAULT_BEFORE_DATE_TIME_FILTER = false;
  public static final String DEFAULT_AFTER_DATE_TIME = "";
  public static final String DEFAULT_BEFORE_DATE_TIME = "";
  public static final String DEFAULT_TAG_FILTER = "";
  public static final String DEFAULT_BRANCH_FILTER = null;
  public static final String DEFAULT_TWO_TAG_NAMING = "TAG_$T_MERGE_$S_TO_$D";
  public static final String DEFAULT_ONE_TAG_NAMING = "TAG_MERGE_$S_TO_$D";

  protected CVSRevisionGraphConfiguration _form = null;
  public boolean _useTwoTagConvention = DEFAULT_USE_TWO_TAG_CONVENTION;
  public boolean _showTags = DEFAULT_SHOW_TAGS;
  public String _tagNaming = DEFAULT_TWO_TAG_NAMING;
  public String _tagFilter = DEFAULT_TAG_FILTER;
  public String _branchFilter = DEFAULT_BRANCH_FILTER;
  public boolean _showRevisionFilter = DEFAULT_SHOW_REVISION_FILTER;
  public boolean _afterDateTimeFilter = DEFAULT_AFTER_DATE_TIME_FILTER;
  public boolean _beforeDateTimeFilter = DEFAULT_BEFORE_DATE_TIME_FILTER;
  public String _afterDateTime = DEFAULT_AFTER_DATE_TIME;
  public String _beforeDateTime = DEFAULT_BEFORE_DATE_TIME;
  public boolean _showTagFilter = DEFAULT_SHOW_TAG_FILTER;
  public boolean _showBranchFilter = DEFAULT_SHOW_BRANCH_FILTER;

  protected Icon _graphIcon = IconLoader.getIcon("/org/cvstoolbox/graph/images/graph_24.png");

  public CVSRevisionGraphProjectComponent(Project project)
  {
  }

  public String getBeforeTagName(String sourceBranchName,String destBranchName)
  {
    String retVal = _tagNaming.replace("$T","BEFORE");
    retVal = retVal.replace("$S",sourceBranchName);
    retVal = retVal.replace("$D",destBranchName);
    return(retVal);
  }

  public String getAfterTagName(String sourceBranchName,String destBranchName)
  {
    String retVal = _tagNaming.replace("$T","AFTER");
    retVal = retVal.replace("$S",sourceBranchName);
    retVal = retVal.replace("$D",destBranchName);
    return(retVal);
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return("CVSRevisionGraphProjectComponent");
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  public boolean is_useTwoTagConvention()
  {
    return _useTwoTagConvention;
  }

  public void set_useTwoTagConvention(final boolean useTwoTagConvention)
  {
    _useTwoTagConvention = useTwoTagConvention;
  }

  public boolean is_showTags()
  {
    return _showTags;
  }

  public void set_showTags(final boolean showTags)
  {
    _showTags = showTags;
  }

  public String get_tagFilter()
  {
    return _tagFilter;
  }

  public void set_tagFilter(final String tagFilter)
  {
    _tagFilter = tagFilter;
  }

  public List<String> getBranchFilter()
  {
    List<String> retVal = new ArrayList<String>();
    if(_branchFilter == null)
      return(retVal);
    String bFilters[] = _branchFilter.split(",");
    for(String bFilter : bFilters)
      retVal.add(bFilter);
    return(retVal);
  }

  public void setBranchFilter(List<String> branchFilter)
  {
    if((branchFilter == null) || (branchFilter.size() == 0)) {
      _branchFilter = null;
      return;
    }
    StringBuffer bFilters = new StringBuffer();
    for(String bFilter : branchFilter) {
      if(bFilters.length() != 0)
        bFilters.append(",");
      bFilters.append(bFilter);
    }
    _branchFilter = bFilters.toString();
  }

  public String get_branchFilter()
  {
    return _branchFilter;
  }

  public void set_branchFilter(String branchFilter)
  {
    _branchFilter = branchFilter;
  }

  public boolean is_showTagFilter()
  {
    return _showTagFilter;
  }

  public void set_showTagFilter(boolean showTagFilter)
  {
    _showTagFilter = showTagFilter;
  }

  public boolean is_showBranchFilter()
  {
    return _showBranchFilter;
  }

  public void set_showBranchFilter(boolean showBranchFilter)
  {
    _showBranchFilter = showBranchFilter;
  }

  public String get_tagNaming()
  {
    return _tagNaming;
  }

  public void set_tagNaming(final String tagNaming)
  {
    _tagNaming = tagNaming;
  }

  public String get_afterDateTime()
  {
    return _afterDateTime;
  }

  public void set_afterDateTime(String afterDateTime)
  {
    _afterDateTime = afterDateTime;
  }

  public boolean is_afterDateTimeFilter()
  {
    return _afterDateTimeFilter;
  }

  public void set_afterDateTimeFilter(boolean afterDateTimeFilter)
  {
    _afterDateTimeFilter = afterDateTimeFilter;
  }

  public String get_beforeDateTime()
  {
    return _beforeDateTime;
  }

  public void set_beforeDateTime(String beforeDateTime)
  {
    _beforeDateTime = beforeDateTime;
  }

  public boolean is_beforeDateTimeFilter()
  {
    return _beforeDateTimeFilter;
  }

  public void set_beforeDateTimeFilter(boolean beforeDateTimeFilter)
  {
    _beforeDateTimeFilter = beforeDateTimeFilter;
  }

  public boolean is_showRevisionFilter()
  {
    return _showRevisionFilter;
  }

  public void set_showRevisionFilter(boolean showRevisionFilter)
  {
    _showRevisionFilter = showRevisionFilter;
  }

  @Nls
  public String getDisplayName()
  {
    return("CVS Revision Graph");
  }

  public Icon getIcon()
  {
    return(_graphIcon);
  }

  @Nullable
  @NonNls
  public String getHelpTopic()
  {
    return("doc-cvsRG");
  }

  public JComponent createComponent()
  {
    if(_form == null)
      _form = new CVSRevisionGraphConfiguration();
    return(_form.get_mainPanel());
  }

  public boolean isModified()
  {
    if(_form == null)
      return(false);
    return(_form.isModified(this));
  }

  public void apply() throws ConfigurationException
  {
    if(_form == null)
      return;
    _form.getData(this);
  }

  public void reset()
  {
    if(_form == null)
      return;
    _form.setData(this);
  }

  public void disposeUIResources()
  {
    _form = null;
  }

  public void readExternal(Element element) throws InvalidDataException
  {
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(Element element) throws WriteExternalException
  {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }
}
