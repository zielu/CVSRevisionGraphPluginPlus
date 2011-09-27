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

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import org.cvstoolbox.graph.util.CalendarClockDialog;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CVSRevisionGraphConfiguration {
    private JCheckBox _useTwoTagConventionCB;
    private JTextField _tagNamingTF;
    private JButton _tagNamingDefaultB;
    private JLabel _tagNamingExampleL;
    private JPanel _mainPanel;
    private JTextField _tagFilterTF;
    private JCheckBox _showTagsCB;
    private JBList _branchFilterL;
    private JButton _addBranchB;
    private JButton _removeBranchB;
    private JRadioButton _showBranchR;
    private JRadioButton _hideBranchR;
    private JRadioButton _showTagR;
    private JRadioButton _hideTagR;
    private JLabel _tagFilterL;
    private JRadioButton _showRevisionR;
    private JRadioButton _hideRevisionR;
    private JCheckBox _afterDateTimeCB;
    private JCheckBox _beforeDateTimeCB;
    private JTextField _afterDateTimeTF;
    private JButton _afterDateTimeB;
    private JTextField _beforeDateTimeTF;
    private JButton _beforeDateTimeB;

    public CVSRevisionGraphConfiguration() {
        _afterDateTimeTF.setToolTipText("Format: " + CVSRevisionGraph.DATE_TIME_FORMAT);
        _beforeDateTimeTF.setToolTipText("Format: " + CVSRevisionGraph.DATE_TIME_FORMAT);
        ButtonGroup bg = new ButtonGroup();
        bg.add(_showTagR);
        bg.add(_hideTagR);
        bg = new ButtonGroup();
        bg.add(_showBranchR);
        bg.add(_hideBranchR);
        bg = new ButtonGroup();
        bg.add(_showRevisionR);
        bg.add(_hideRevisionR);
        _useTwoTagConventionCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateTagNamingExample();
            }
        });
        _tagNamingDefaultB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateTagNamingDefault();
            }
        });
        _showTagsCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateTagFilter();
            }
        });
        _afterDateTimeCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateAfterDateTimeFilter();
            }
        });
        _beforeDateTimeCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateBeforeDateTimeFilter();
            }
        });
        _addBranchB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String branchName = Messages.showInputDialog(_mainPanel, "Branch name:", "Add Branch", Messages.getQuestionIcon());
                if ((branchName == null) || (branchName.length() == 0)) {
                    return;
                }
                DefaultListModel lm = (DefaultListModel) _branchFilterL.getModel();
                lm.addElement(branchName);
            }
        });
        _removeBranchB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int selIndices[] = _branchFilterL.getSelectedIndices();
                if ((selIndices == null) || (selIndices.length == 0)) {
                    return;
                }
                DefaultListModel lm = (DefaultListModel) _branchFilterL.getModel();
                for (int i = (selIndices.length - 1); i >= 0; i--) {
                    lm.removeElementAt(selIndices[i]);
                }
            }
        });
        _afterDateTimeB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                CalendarClockDialog ccDialog = new CalendarClockDialog(_mainPanel);
                ccDialog.show(_afterDateTimeTF.getText());
                String selDate = ccDialog.getReturnValue();
                if (selDate == null) {
                    return;
                }
                _afterDateTimeTF.setText(selDate);
            }
        });
        _beforeDateTimeB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                CalendarClockDialog ccDialog = new CalendarClockDialog(_mainPanel);
                ccDialog.show(_beforeDateTimeTF.getText());
                String selDate = ccDialog.getReturnValue();
                if (selDate == null) {
                    return;
                }
                _beforeDateTimeTF.setText(selDate);
            }
        });
    }

    protected void updateTagNamingExample() {
        if (_useTwoTagConventionCB.isSelected()) {
            _tagNamingExampleL.setText("Example:  " + CVSRevisionGraphProjectConfig.DEFAULT_TWO_TAG_NAMING);
        } else {
            _tagNamingExampleL.setText("Example:  " + CVSRevisionGraphProjectConfig.DEFAULT_ONE_TAG_NAMING);
        }
    }

    protected void updateTagNamingDefault() {
        if (_useTwoTagConventionCB.isSelected()) {
            _tagNamingTF.setText(CVSRevisionGraphProjectConfig.DEFAULT_TWO_TAG_NAMING);
        } else {
            _tagNamingTF.setText(CVSRevisionGraphProjectConfig.DEFAULT_ONE_TAG_NAMING);
        }
    }

    protected void updateTagFilter() {
        _showTagR.setEnabled(_showTagsCB.isSelected());
        _hideTagR.setEnabled(_showTagsCB.isSelected());
        _tagFilterL.setEnabled(_showTagsCB.isSelected());
        _tagFilterTF.setEnabled(_showTagsCB.isSelected());
    }

    protected void updateAfterDateTimeFilter() {
        _afterDateTimeTF.setEnabled(_afterDateTimeCB.isSelected());
        _afterDateTimeB.setEnabled(_afterDateTimeCB.isSelected());
    }

    protected void updateBeforeDateTimeFilter() {
        _beforeDateTimeTF.setEnabled(_beforeDateTimeCB.isSelected());
        _beforeDateTimeB.setEnabled(_beforeDateTimeCB.isSelected());
    }

    public JPanel get_mainPanel() {
        return _mainPanel;
    }

    public void setData(CVSRevisionGraphProjectComponent data) {
        CVSRevisionGraphProjectConfig config = data.getConfig();
        _useTwoTagConventionCB.setSelected(config.is_useTwoTagConvention());
        _tagNamingTF.setText(config.get_tagNaming());
        _showTagsCB.setSelected(config.is_showTags());
        if (config.is_showTagFilter()) {
            _showTagR.setSelected(true);
        } else {
            _hideTagR.setSelected(true);
        }
        _tagFilterTF.setText(config.get_tagFilter());
        if (config.is_showBranchFilter()) {
            _showBranchR.setSelected(true);
        } else {
            _hideBranchR.setSelected(true);
        }
        List<String> hiddenBranches = data.getBranchFilter();
        DefaultListModel lm = (DefaultListModel) _branchFilterL.getModel();
        lm.removeAllElements();
        for (String hiddenBranch : hiddenBranches) {
            lm.addElement(hiddenBranch);
        }
        if (config.is_showRevisionFilter()) {
            _showRevisionR.setSelected(true);
        } else {
            _hideRevisionR.setSelected(true);
        }
        _afterDateTimeCB.setSelected(config.is_afterDateTimeFilter());
        _beforeDateTimeCB.setSelected(config.is_beforeDateTimeFilter());
        _afterDateTimeTF.setText(config.get_afterDateTime());
        _beforeDateTimeTF.setText(config.get_beforeDateTime());
        updateTagNamingExample();
        updateTagFilter();
        updateAfterDateTimeFilter();
        updateBeforeDateTimeFilter();
    }

    public void getData(CVSRevisionGraphProjectComponent data) {
        CVSRevisionGraphProjectConfig config = data.getConfig();
        config.set_useTwoTagConvention(_useTwoTagConventionCB.isSelected());
        config.set_tagNaming(_tagNamingTF.getText());
        config.set_showTags(_showTagsCB.isSelected());
        config.set_showTagFilter(_showTagR.isSelected());
        config.set_tagFilter(_tagFilterTF.getText());
        config.set_showBranchFilter(_showBranchR.isSelected());
        List<String> hiddenBranches = new ArrayList<String>();
        DefaultListModel lm = (DefaultListModel) _branchFilterL.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            String hiddenBranch = (String) lm.getElementAt(i);
            hiddenBranches.add(hiddenBranch);
        }
        data.setBranchFilter(hiddenBranches);
        config.set_showRevisionFilter(_showRevisionR.isSelected());
        config.set_afterDateTimeFilter(_afterDateTimeCB.isSelected());
        config.set_beforeDateTimeFilter(_beforeDateTimeCB.isSelected());
        config.set_afterDateTime(_afterDateTimeTF.getText());
        config.set_beforeDateTime(_beforeDateTimeTF.getText());
    }

    public boolean isModified(CVSRevisionGraphProjectComponent data) {
        CVSRevisionGraphProjectConfig config = data.getConfig();
        if (_useTwoTagConventionCB.isSelected() != config.is_useTwoTagConvention()) {
            return true;
        }
        if (_tagNamingTF.getText() != null ? !_tagNamingTF.getText().equals(config.get_tagNaming()) : config.get_tagNaming() != null) {
            return true;
        }
        if (_showRevisionR.isSelected() != config.is_showRevisionFilter()) {
            return true;
        }
        if (_afterDateTimeCB.isSelected() != config.is_afterDateTimeFilter()) {
            return true;
        }
        if (_beforeDateTimeCB.isSelected() != config.is_beforeDateTimeFilter()) {
            return true;
        }
        if (_afterDateTimeTF.getText() != null ? !_afterDateTimeTF.getText().equals(config.get_afterDateTime()) : config.get_afterDateTime() != null) {
            return true;
        }
        if (_beforeDateTimeTF.getText() != null ? !_beforeDateTimeTF.getText().equals(config.get_beforeDateTime()) : config.get_beforeDateTime() != null) {
            return true;
        }
        if (_showTagsCB.isSelected() != config.is_showTags()) {
            return true;
        }
        if (_showTagR.isSelected() != config.is_showTagFilter()) {
            return true;
        }
        if (_tagFilterTF.getText() != null ? !_tagFilterTF.getText().equals(config.get_tagFilter()) : config.get_tagFilter() != null) {
            return true;
        }
        if (_showBranchR.isSelected() != config.is_showBranchFilter()) {
            return true;
        }
        DefaultListModel lm = (DefaultListModel) _branchFilterL.getModel();
        List<String> hiddenBranches = data.getBranchFilter();
        if ((lm == null) && (hiddenBranches == null)) {
            return false;
        }
        if ((lm == null)) {
            return true;
        }
        if ((hiddenBranches == null)) {
            return true;
        }
        if (lm.getSize() != hiddenBranches.size()) {
            return true;
        }
        for (int i = 0; i < hiddenBranches.size(); i++) {
            String hiddenBranch = hiddenBranches.get(i);
            String hBranch = (String) lm.getElementAt(i);
            if ((hiddenBranch == null) && (hBranch == null)) {
                continue;
            }
            if (hiddenBranch == null) {
                return true;
            }
            if (hBranch == null) {
                return true;
            }
            if (!hiddenBranch.equals(hBranch)) {
                return true;
            }
        }
        return false;
    }

    private void createUIComponents() {
        _branchFilterL = new JBList(new DefaultListModel());
    }

}
