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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jetbrains.annotations.Nullable;
import org.cvstoolbox.graph.CVSRevisionGraph;
import com.intellij.openapi.ui.DialogWrapper;

public class CalendarClockDialog extends DialogWrapper {
  protected CalendarChooser _calChooser = null;
  protected TimeChooser _timeChooser = null;
  protected JSplitPane _sp = null;
  protected String _retVal = null;

  public CalendarClockDialog(Component parent)
  {
    super(parent,false);
    setTitle("Choose Date/Time");
    setModal(true);
    init();
  }

  @Nullable
  protected JComponent createCenterPanel()
  {
    JPanel mainPanel = new JPanel(new BorderLayout());
    _calChooser = new CalendarChooser(false,true);
    _calChooser.setSize(CalendarChooser.LARGE);
    _calChooser.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5),BorderFactory.createTitledBorder("")));
    _timeChooser = new TimeChooser(true);
    _timeChooser.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5),BorderFactory.createTitledBorder("")));
    _sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,_calChooser,_timeChooser);
    mainPanel.add(_sp,BorderLayout.CENTER);
    return(mainPanel);
  }

  protected void doOKAction()
  {
    updateReturnValue();
    super.doOKAction();
  }

  public void doCancelAction()
  {
    _retVal = null;
    super.doCancelAction();
  }

  protected void dispose()
  {
    super.dispose();
  }

  protected Date getDate()
  {
    Date calD = _calChooser.getDate();
    Date clockD = _timeChooser.getDate();
    //Merge two dates
    Calendar retVal = Calendar.getInstance();
    Calendar calC = Calendar.getInstance();
    calC.setTime(calD);
    Calendar clockC = Calendar.getInstance();
    clockC.setTime(clockD);
    retVal.set(Calendar.MONTH,calC.get(Calendar.MONTH));
    retVal.set(Calendar.DAY_OF_MONTH,calC.get(Calendar.DAY_OF_MONTH));
    retVal.set(Calendar.YEAR,calC.get(Calendar.YEAR));
    retVal.set(Calendar.HOUR,clockC.get(Calendar.HOUR));
    retVal.set(Calendar.MINUTE,clockC.get(Calendar.MINUTE));
    retVal.set(Calendar.SECOND,clockC.get(Calendar.SECOND));
    retVal.set(Calendar.AM_PM,clockC.get(Calendar.AM_PM));
    //Return an sql.Timestamp to signal that both date and time was picked
    Timestamp sqlTS = new Timestamp(retVal.getTime().getTime());
    return(sqlTS);
  }

  protected void setDate(Date d)
  {
    _calChooser.setDate(d);
    _timeChooser.setDate(d);
  }

  protected void updateReturnValue()
  {
    Date d = getDate();
    if(d == null)
      _retVal = null;
    _retVal = CVSRevisionGraph._dateTimeFormat.format(d);
  }

  public String getReturnValue()
  {
    return(_retVal);
  }

  public void show(String initialValue)
  {
    _retVal = null;
    Date initialVal = null;
    try {
      initialVal = CVSRevisionGraph._dateTimeFormat.parse(initialValue);
    } catch(Exception e) {
      //Ignore
    }
    if(initialVal != null)
      setDate(initialVal);
    _sp.resetToPreferredSizes();
    _sp.setResizeWeight(0.5);
    show();
  }
}
