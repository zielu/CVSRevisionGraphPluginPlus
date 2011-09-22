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
import com.intellij.openapi.util.IconLoader;
import info.clearthought.layout.TableLayout;
import org.cvstoolbox.graph.util.CalendarClockDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DateFilterOptions extends DialogWrapper {
  protected JRadioButton _showTagR = null;
  protected JRadioButton _hideTagR = null;
  protected JCheckBox _afterDateTimeFilterCB = null;
  protected JTextField _afterDateTimeTF = null;
  protected JButton _afterDateTimeB = null;
  protected JCheckBox _beforeDateTimeFilterCB = null;
  protected JTextField _beforeDateTimeTF = null;
  protected JButton _beforeDateTimeB = null;
  protected DateFilterOptions.DateFilteringOptions _retVal = null;
  protected Project _project = null;

  public DateFilterOptions(Project project)
  {
    super(project,false);
    setTitle("Date Filter Options");
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
    radioP.add(new JLabel("revisions:"),"1,0,1,1,c,c");
    ButtonGroup bg = new ButtonGroup();
    bg.add(_showTagR);
    bg.add(_hideTagR);
    double tableSizes[][] = {{TableLayout.PREFERRED,TableLayout.FILL,TableLayout.PREFERRED,TableLayout.FILL,TableLayout.PREFERRED},{TableLayout.PREFERRED,TableLayout.PREFERRED}};
    TableLayout tl = new TableLayout(tableSizes);
    tl.setHGap(5);
    tl.setVGap(5);
    JPanel retVal = new JPanel(tl);
    retVal.add(radioP,"0,0,0,1");
    _afterDateTimeFilterCB = new JCheckBox("After Date/Time");
    _afterDateTimeFilterCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae)
      {
        updateAfterDateTime();
      }
    });
    retVal.add(_afterDateTimeFilterCB,"1,0,2,0,l,c");
    _afterDateTimeTF = new JTextField(25);
    _afterDateTimeTF.setToolTipText("Format: " + CVSRevisionGraph.DATE_TIME_FORMAT);
    retVal.add(_afterDateTimeTF,"1,1");
    _afterDateTimeB = new JButton(IconLoader.getIcon("/org/cvstoolbox/graph/images/dateTime.png"));
    _afterDateTimeB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae)
      {
        CalendarClockDialog ccDialog = new CalendarClockDialog(_afterDateTimeTF);
        ccDialog.show(_afterDateTimeTF.getText());
        String selDate = ccDialog.getReturnValue();
        if(selDate == null)
          return;
        _afterDateTimeTF.setText(selDate);
      }
    });
    _afterDateTimeB.setMargin(new Insets(0,0,0,0));
    retVal.add(_afterDateTimeB,"2,1,c,c");
    _beforeDateTimeFilterCB = new JCheckBox("Before Date/Time");
    _beforeDateTimeFilterCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae)
      {
        updateBeforeDateTime();
      }
    });
    retVal.add(_beforeDateTimeFilterCB,"3,0,4,0,l,c");
    _beforeDateTimeTF = new JTextField(25);
    _beforeDateTimeTF.setToolTipText("Format: " + CVSRevisionGraph.DATE_TIME_FORMAT);
    retVal.add(_beforeDateTimeTF,"3,1");
    _beforeDateTimeB = new JButton(IconLoader.getIcon("/org/cvstoolbox/graph/images/dateTime.png"));
    _beforeDateTimeB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae)
      {
        CalendarClockDialog ccDialog = new CalendarClockDialog(_beforeDateTimeTF);
        ccDialog.show(_beforeDateTimeTF.getText());
        String selDate = ccDialog.getReturnValue();
        if(selDate == null)
          return;
        _beforeDateTimeTF.setText(selDate);
      }
    });
    _beforeDateTimeB.setMargin(new Insets(0,0,0,0));
    retVal.add(_beforeDateTimeB,"4,1,c,c");
    return(retVal);
  }

  protected void updateAfterDateTime()
  {
    _afterDateTimeTF.setEnabled(_afterDateTimeFilterCB.isSelected());
    _afterDateTimeB.setEnabled(_afterDateTimeFilterCB.isSelected());
  }

  protected void updateBeforeDateTime()
  {
    _beforeDateTimeTF.setEnabled(_beforeDateTimeFilterCB.isSelected());
    _beforeDateTimeB.setEnabled(_beforeDateTimeFilterCB.isSelected());
  }

  protected void dispose()
  {
    _project = null;
    super.dispose();
  }

  protected void doOKAction()
  {
    _retVal = new DateFilterOptions.DateFilteringOptions();
    _retVal._showRevisionFilter = _showTagR.isSelected();
    _retVal._afterDateTimeFilter = _afterDateTimeFilterCB.isSelected();
    _retVal._beforeDateTimeFilter = _beforeDateTimeFilterCB.isSelected();
    _retVal._afterDateTime = _afterDateTimeTF.getText();
    _retVal._beforeDateTime = _beforeDateTimeTF.getText();
    super.doOKAction();
  }

  public void show(boolean showRevisionFilter,boolean afterDateTimeFilter,boolean beforeDateTimeFilter,String afterDateTime,String beforeDateTime)
  {
    if(showRevisionFilter)
      _showTagR.setSelected(true);
    else
      _hideTagR.setSelected(true);
    _afterDateTimeFilterCB.setSelected(afterDateTimeFilter);
    _beforeDateTimeFilterCB.setSelected(beforeDateTimeFilter);
    _afterDateTimeTF.setText(afterDateTime);
    _beforeDateTimeTF.setText(beforeDateTime);
    updateAfterDateTime();
    updateBeforeDateTime();
    show();
  }

  public DateFilterOptions.DateFilteringOptions getReturnValue()
  {
    return(_retVal);
  }

  public void doCancelAction()
  {
    _retVal = null;
    super.doCancelAction();
  }

  public class DateFilteringOptions {
    public boolean _showRevisionFilter;
    public boolean _afterDateTimeFilter;
    public String _afterDateTime = null;
    public boolean _beforeDateTimeFilter;
    public String _beforeDateTime = null;
  }
}
