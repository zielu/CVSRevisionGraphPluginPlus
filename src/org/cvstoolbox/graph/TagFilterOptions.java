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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import info.clearthought.layout.TableLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class TagFilterOptions extends DialogWrapper {
  protected JRadioButton _showTagR = null;
  protected JRadioButton _hideTagR = null;
  protected JTextField _tagFilterTF = null;
  protected TagFilterOptions.TagFilteringOptions _retVal = null;
  protected Project _project = null;

  public TagFilterOptions(Project project)
  {
    super(project,false);
    setTitle("Tag Filter Options");
    setModal(true);
    _project = project;
    init();
  }

  @Nullable
  protected JComponent createCenterPanel()
  {
    double tableSizes2[][] = {{TableLayout.PREFERRED,TableLayout.PREFERRED},{TableLayout.PREFERRED,TableLayout.PREFERRED}};
    TableLayout tl2 = new TableLayout(tableSizes2);
    tl2.setHGap(5);
    tl2.setVGap(5);
    JPanel radioP = new JPanel(tl2);
    radioP.setBorder(BorderFactory.createEtchedBorder());
    _showTagR = new JRadioButton("Show");
    radioP.add(_showTagR,"0,0,l,b");
    _hideTagR = new JRadioButton("Hide");
    radioP.add(_hideTagR,"0,1,l,t");
    radioP.add(new JLabel("tags of the form:"),"1,0,1,1,c,c");
    ButtonGroup bg = new ButtonGroup();
    bg.add(_showTagR);
    bg.add(_hideTagR);
    double tableSizes[][] = {{TableLayout.PREFERRED,TableLayout.FILL},{TableLayout.PREFERRED}};
    TableLayout tl = new TableLayout(tableSizes);
    tl.setHGap(5);
    tl.setVGap(5);
    JPanel retVal = new JPanel(tl);
    retVal.add(radioP,"0,0");
    _tagFilterTF = new JTextField(25);
    _tagFilterTF.setToolTipText("Use regular expression syntax");
    retVal.add(_tagFilterTF,"1,0,f,c");
    return(retVal);
  }

  protected void dispose()
  {
    _project = null;
    super.dispose();
  }

  protected void doOKAction()
  {
    _retVal = new TagFilterOptions.TagFilteringOptions();
    _retVal._showTagFilter = _showTagR.isSelected();
    _retVal._tagFilter = _tagFilterTF.getText();
    super.doOKAction();
  }

  public void show(boolean showTagFilter,String tagFilter)
  {
    if(showTagFilter)
      _showTagR.setSelected(true);
    else
      _hideTagR.setSelected(true);
    _tagFilterTF.setText(tagFilter);
    show();
  }

  public TagFilterOptions.TagFilteringOptions getReturnValue()
  {
    return(_retVal);
  }

  public void doCancelAction()
  {
    _retVal = null;
    super.doCancelAction();
  }

  public class TagFilteringOptions {
    public String _tagFilter = null;
    public boolean _showTagFilter;
  }
}
