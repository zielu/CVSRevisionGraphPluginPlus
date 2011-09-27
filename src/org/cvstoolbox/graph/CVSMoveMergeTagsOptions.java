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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import info.clearthought.layout.TableLayout;
import org.cvstoolbox.graph.revisions.BranchRevision;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

public class CVSMoveMergeTagsOptions extends DialogWrapper implements ActionListener {
  public static final String MOVE_BEFORE_PREFIX = "Move BEFORE tag";
  public static final String MOVE_AFTER_PREFIX = "Move AFTER tag";
  public static final String MOVE_PREFIX = "Move tag";

  protected JComboBox _sourceBranchNameCB = null;
  protected JComboBox _destBranchNameCB = null;
  protected JCheckBox _moveBeforeCB = null;
  protected JCheckBox _moveAfterCB = null;
  protected Icon _swapIcon = IconLoader.getIcon("/org/cvstoolbox/graph/images/swap.png");
  protected MoveMergeTagsOptions _retVal = null;
  protected Project _project = null;
  protected Vector<String> _branches = null;

  public CVSMoveMergeTagsOptions(Project project, Collection<BranchRevision> branchSet)
  {
    super(project,false);
    setTitle("Move Merge Tags Options");
    setModal(true);
    _project = project;
    _branches = new Vector<String>(branchSet.size());
    for(BranchRevision brev : branchSet)
      _branches.add(brev.getName());
    Collections.sort(_branches);
    init();
  }

  @Nullable
  protected JComponent createCenterPanel()
  {
    double tableSizes[][] = {{TableLayout.PREFERRED,TableLayout.FILL,TableLayout.PREFERRED},{TableLayout.PREFERRED,TableLayout.PREFERRED,TableLayout.PREFERRED,TableLayout.PREFERRED}};
    TableLayout tl = new TableLayout(tableSizes);
    tl.setHGap(5);
    tl.setVGap(5);
    JPanel retVal = new JPanel(tl);
    retVal.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    retVal.add(new JLabel("Source Branch Name:"),"0,0");
    _sourceBranchNameCB = new JComboBox(_branches);
    _sourceBranchNameCB.setSelectedItem(null);
    _sourceBranchNameCB.addActionListener(this);
    retVal.add(_sourceBranchNameCB,"1,0,f,c");
    retVal.add(new JLabel("Destination Branch Name:"),"0,1");
    _destBranchNameCB = new JComboBox(_branches);
    _destBranchNameCB.setSelectedItem(null);
    _destBranchNameCB.addActionListener(this);
    retVal.add(_destBranchNameCB,"1,1,f,c");
    SwapAction swapAction = new SwapAction();
    JButton swapButton = new JButton(swapAction);
    swapButton.setText(null);
    retVal.add(swapButton,"2,0,2,1,c,c");
    //Check for single or dual tag mode
    CVSRevisionGraphProjectComponent rgpc = _project.getComponent(CVSRevisionGraphProjectComponent.class);
    _moveBeforeCB = new JCheckBox(rgpc.getConfig().is_useTwoTagConvention() ? MOVE_BEFORE_PREFIX : MOVE_PREFIX,true);
    retVal.add(_moveBeforeCB,"0,2,2,2");
    _moveAfterCB = new JCheckBox(MOVE_AFTER_PREFIX,true);
    retVal.add(_moveAfterCB,"0,3,2,3");
    _moveAfterCB.setVisible(rgpc.getConfig().is_useTwoTagConvention());
    return(retVal);
  }

  protected void dispose()
  {
    _sourceBranchNameCB.removeActionListener(this);
    _sourceBranchNameCB = null;
    _destBranchNameCB.removeActionListener(this);
    _destBranchNameCB = null;
    _moveBeforeCB = null;
    _moveAfterCB = null;
    _swapIcon = null;
    _project = null;
    _branches = null;
    super.dispose();
  }

  protected void doOKAction()
  {
    _retVal = new MoveMergeTagsOptions();
    String sourceBranchName = (String)_sourceBranchNameCB.getSelectedItem();
    if((sourceBranchName == null) || (sourceBranchName.length() == 0)) {
      Messages.showMessageDialog(_project,"Please enter a source branch name","Bad Branch Name",Messages.getErrorIcon());
      return;
    }
    String destBranchName = (String)_destBranchNameCB.getSelectedItem();
    if((destBranchName == null) || (destBranchName.length() == 0)) {
      Messages.showMessageDialog(_project,"Please enter a destination branch name","Bad Branch Name",Messages.getErrorIcon());
      return;
    }
    _retVal._sourceBranchName = sourceBranchName;
    _retVal._destBranchName = destBranchName;
    _retVal._moveBefore = _moveBeforeCB.isSelected();
    _retVal._moveAfter = _moveAfterCB.isSelected();
    super.doOKAction();
  }

  public MoveMergeTagsOptions getReturnValue()
  {
    return(_retVal);  
  }

  public void doCancelAction()
  {
    _retVal = null;
    super.doCancelAction();
  }

  protected void swapBranchNames()
  {
    String target = (String)_destBranchNameCB.getSelectedItem();
    _destBranchNameCB.setSelectedItem(_sourceBranchNameCB.getSelectedItem());
    _sourceBranchNameCB.setSelectedItem(target);
  }

  public void actionPerformed(ActionEvent ae)
  {
    CVSRevisionGraphProjectComponent rgpc = _project.getComponent(CVSRevisionGraphProjectComponent.class);
    String sourceBranchName = (String)_sourceBranchNameCB.getSelectedItem();
    String destBranchName = (String)_destBranchNameCB.getSelectedItem();
    if((sourceBranchName == null) || (destBranchName == null)) {
      _moveBeforeCB.setText(rgpc.getConfig().is_useTwoTagConvention() ? MOVE_BEFORE_PREFIX : MOVE_PREFIX);
      _moveAfterCB.setText(MOVE_AFTER_PREFIX);
    } else {
      String beforeTagName = rgpc.getBeforeTagName(sourceBranchName,destBranchName);
      String afterTagName = rgpc.getAfterTagName(sourceBranchName,destBranchName);
      if(rgpc.getConfig().is_useTwoTagConvention())
        _moveBeforeCB.setText(MOVE_BEFORE_PREFIX + " (" + beforeTagName + ")");
      else
        _moveBeforeCB.setText(MOVE_PREFIX + " (" + beforeTagName + ")");
      _moveAfterCB.setText(MOVE_AFTER_PREFIX + " (" + afterTagName + ")");
    }
  }

  protected class SwapAction extends AbstractAction {
    public SwapAction()
    {
      super("Swap Branch Names",_swapIcon);
      putValue(Action.SHORT_DESCRIPTION,"Swap Branch Names");
    }

    public void actionPerformed(ActionEvent ae)
    {
      swapBranchNames();
    }
  }

  public class MoveMergeTagsOptions {
    public String _sourceBranchName = null;
    public String _destBranchName = null;
    public boolean _moveBefore;
    public boolean _moveAfter;
  }
}
