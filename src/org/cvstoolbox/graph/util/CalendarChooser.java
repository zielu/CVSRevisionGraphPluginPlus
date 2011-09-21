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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A composite component used to date information.  There are spinners for
 * the month and year.  The day of the month is presented in a calendar page
 * style with toggle buttons for each day.
 * @author Mik Burgeson
 */
public class CalendarChooser extends JComponent {
  /** Indicates a small layout */
  public static final int XSMALL = 0;

  /** Indicates a small layout */
  public static final int SMALL = 1;

  /** Indicates a medium layout */
  public static final int MEDIUM = 2;

  /** Indicates a large layout */
  public static final int LARGE = 3;

  /** Indicates a extra large layout */
  public static final int XLARGE = 4;

  protected static final float[] DOM_FONT_SIZE = new float[] {7.0f,9.0f,10.0f,12.0f,16.0f};
  protected static final float[] DOW_FONT_SIZE = new float[] {9.0f,11.0f,12.0f,15.0f,18.0f};
  protected static final float[] MY_FONT_SIZE = new float[] {8.0f,11.0f,12.0f,13.0f,15.0f};

  protected JComponent _month;
  protected JSpinner _year;
  protected CalendarGrid _grid;
  protected Calendar _prev;
  protected JButton _today = null;

  protected SpinnerDateModel _model;
  protected SpinnerDateModel _monthModel;
  protected SpinnerDateModel _yearModel;
  protected boolean _monthAsSpinner = true;

  /**
   * Default constructor.
   */
  public CalendarChooser()
  {
    this(new SpinnerDateModel());
  }

  public CalendarChooser(SpinnerDateModel model)
  {
    this(model,true,false);
  }

  public CalendarChooser(boolean monthAsSpinner,boolean showNow)
  {
    this(new SpinnerDateModel(),monthAsSpinner,showNow);
  }

  public CalendarChooser(SpinnerDateModel model,boolean monthAsSpinner,boolean showNow)
  {
    _model = model;
    _monthAsSpinner = monthAsSpinner;
    _model.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e)
      {
        Date d = _model.getDate();
        if(!d.equals(_yearModel.getDate()))
          _yearModel.setValue(d);
        if(!d.equals(_monthModel.getDate()))
          _monthModel.setValue(d);
        if(_month instanceof JComboBox) {
          JComboBox monthCombo = (JComboBox)_month;
          SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMMM",Locale.US);
          Calendar cal = Calendar.getInstance();
          cal.setTime(d);
          int monthNum = cal.get(Calendar.MONTH);
          String monthName = sdf.format(d);
          MonthNumber monthNumber = new MonthNumber(monthNum,monthName);
          monthCombo.setSelectedItem(monthNumber);
        }
      }
    });

    _monthModel = new SpinnerDateModel(new Date(),null,null,Calendar.MONTH);
    if(_monthAsSpinner) {
      // Set up the month spinner
      JSpinner monthSpinner = new JSpinner(_monthModel);
      JSpinner.DateEditor editor = new JSpinner.DateEditor(monthSpinner,"MMMMMMMMM");
      //Since can't supply my own dateformat, I must change the symbols after the fact
      editor.getFormat().setDateFormatSymbols(new SimpleDateFormat("MMMMMMMMM",Locale.US).getDateFormatSymbols());
      monthSpinner.setEditor(editor);
      _prev = Calendar.getInstance();
      _prev.setTime(_monthModel.getDate());
      monthSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e)
        {
          Date d = _monthModel.getDate();
          Calendar cal = Calendar.getInstance();
          cal.setTime(d);
          Calendar curCal = Calendar.getInstance();
          curCal.setTime(_model.getDate());
          curCal.set(_monthModel.getCalendarField(),cal.get(_monthModel.getCalendarField()));
          _model.setValue(curCal.getTime());
        }
      });
      _month = monthSpinner;
    } else {
      Vector<MonthNumber> monthItems = new Vector<MonthNumber>();
      Calendar cal = Calendar.getInstance();
      //Set day to the first since every month has a first
      cal.set(Calendar.DATE,1);
      SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMMM",Locale.US);
      for(int monthNum = cal.getActualMinimum(Calendar.MONTH); monthNum <= cal.getActualMaximum(Calendar.MONTH); monthNum++) {
        cal.set(Calendar.MONTH,monthNum);
        String monthName = sdf.format(cal.getTime());
        MonthNumber item = new MonthNumber(monthNum,monthName);
        monthItems.add(item);
      }
      JComboBox monthCombo = new JComboBox(monthItems);
      //Increase width of monthCombo
      Dimension prefSize = monthCombo.getPreferredSize();
      Dimension newPrefSize = new Dimension((int)(1.30 * prefSize.width),prefSize.height);
      monthCombo.setPreferredSize(newPrefSize);
      //Set initial value
      Date initialDate = _monthModel.getDate();
      cal = Calendar.getInstance();
      cal.setTime(initialDate);
      int monthNum = cal.get(Calendar.MONTH);
      String monthName = sdf.format(initialDate);
      MonthNumber monthNumber = new MonthNumber(monthNum,monthName);
      monthCombo.setSelectedItem(monthNumber);
      monthCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae)
          {
            JComboBox monthCombo = (JComboBox)_month;
            MonthNumber monthNumber = (MonthNumber)monthCombo.getSelectedItem();
            if(monthNumber == null)
              return;
            int monthNum = monthNumber.getNum();
            Date d = _model.getDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.set(Calendar.MONTH,monthNum);
            _model.setValue(cal.getTime());
          }
      });
      _month = monthCombo;
    }

    // Set up the year spinner
    _yearModel = new SpinnerDateModel(new Date(),null,null,Calendar.YEAR);
    _year = new JSpinner(_yearModel);
    _year.setEditor(new JSpinner.DateEditor(_year,"yyyy"));
    _year.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e)
      {
        Date d = _yearModel.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        Calendar curCal = Calendar.getInstance();
        curCal.setTime(_model.getDate());
        curCal.set(_yearModel.getCalendarField(),cal.get(_yearModel.getCalendarField()));
        _model.setValue(curCal.getTime());
      }
    });

    // Set up the calendar grid
    _grid = new CalendarGrid();
    _grid.setDate(_model.getDate());

    // Add the components
    setLayout(new BorderLayout());
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(_month,BorderLayout.WEST);
    topPanel.add(_year,BorderLayout.EAST);
    add(topPanel,BorderLayout.NORTH);
    add(_grid,BorderLayout.CENTER);

    if(showNow) {
      _today = new JButton("Today");
      _today.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae)
        {
          _model.setValue(Calendar.getInstance().getTime());
        }
      });
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(_today);
      add(buttonPanel,BorderLayout.SOUTH);
    }
  }

  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    _month.setEnabled(enabled);
    _year.setEnabled(enabled);
    _grid.setEnabled(enabled);
    if(_today != null)
      _today.setEnabled(enabled);
  }

  public void addChangeListener(ChangeListener l)
  {
    if(_model != null)
      _model.addChangeListener(l);
  }

  public void removeChangeListener(ChangeListener l)
  {
    if(_model != null)
      _model.removeChangeListener(l);
  }

  /**
   * Return the current date/time represented by this component.
   *
   * @return Date that is the current date/time represented by this component
   */
  public Date getDate()
  {
    //Only affect the date portion of a Date, not the time
    Date d = _model.getDate();
    Calendar dCal = Calendar.getInstance();
    dCal.setTime(d);
    Calendar retVal = Calendar.getInstance();
    retVal.set(Calendar.MONTH,dCal.get(Calendar.MONTH));
    retVal.set(Calendar.DAY_OF_MONTH,dCal.get(Calendar.DAY_OF_MONTH));
    retVal.set(Calendar.YEAR,dCal.get(Calendar.YEAR));
    retVal.set(Calendar.HOUR,0);
    retVal.set(Calendar.MINUTE,0);
    retVal.set(Calendar.SECOND,0);
    retVal.set(Calendar.AM_PM,Calendar.AM);
    return(retVal.getTime());
  }

  /**
   * Presents the specified date/time.
   *
   * @param date Date that is to be the current date/time
   */
  public void setDate(Date date)
  {
    _year.setValue(date);
    if(_monthAsSpinner) {
      JSpinner monthSpinner = (JSpinner)_month;
      monthSpinner.setValue(date);
    } else {
      JComboBox monthCombo = (JComboBox)_month;
      SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMMM",Locale.US);
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int monthNum = cal.get(Calendar.MONTH);
      String monthName = sdf.format(date);
      MonthNumber monthNumber = new MonthNumber(monthNum,monthName);
      monthCombo.setSelectedItem(monthNumber);
    }
    _grid.setDate(date);
  }

  /**
   * Modifies the amount of space the UI will occupy.
   * @param size Must be one of the fields {@link #SMALL}, {@link #MEDIUM},
   *     {@link #LARGE} or {@link #XLARGE}.
   */
  public void setSize(int size)
  {
    if((size >= 0) && (size <= XLARGE)) {
      // Resize the day labels in the grid
      for(JLabel dayOfWeek : _grid._dayOfWeek) {
        Font f = dayOfWeek.getFont();
        if(f.getSize2D() != DOW_FONT_SIZE[size]) {
          dayOfWeek.setFont(f.deriveFont(DOW_FONT_SIZE[size]));
        }
      }

      // Resize the buttons in the grid
      for(JToggleButton day : _grid._day) {
        Font f = day.getFont();
        if(f.getSize2D() != DOM_FONT_SIZE[size]) {
          day.setFont(f.deriveFont(DOM_FONT_SIZE[size]));
        }
      }

      // Resize the month and year
      Component monthComp;
      if(_month instanceof JSpinner) {
        JSpinner monthSpinner = (JSpinner)_month;
        monthComp = monthSpinner.getEditor();
      } else
        monthComp = _month;
      Component[] c = new Component[] {monthComp,_year.getEditor()};
      for(Component aC : c) {
        if(aC instanceof JSpinner.DefaultEditor) {
          Component ed = ((JSpinner.DefaultEditor)aC).getTextField();
          Font f = ed.getFont();
          if(f.getSize2D() != MY_FONT_SIZE[size])
            ed.setFont(f.deriveFont(MY_FONT_SIZE[size]));
        } else if(aC instanceof JComboBox) {
          JComboBox monthCombo = (JComboBox)aC;
          Font f = monthCombo.getFont();
          if(f.getSize2D() != MY_FONT_SIZE[size])
            monthCombo.setFont(f.deriveFont(MY_FONT_SIZE[size]));
        }
      }

      // Resize the now button
      if(_today != null) {
        Font f = _today.getFont();
        if(f.getSize2D() != MY_FONT_SIZE[size])
          _today.setFont(f.deriveFont(MY_FONT_SIZE[size]));
      }
    }
  }

  protected class CalendarGrid extends JComponent {
    protected JLabel[] _dayOfWeek = null;
    protected JToggleButton[] _day = null;
    protected ButtonGroup _group = null;
    protected int _firstDay = -1;
    protected Color _selectionForeground = null;
    protected Color _selectionBackground = null;
    protected Color _foreground = null;
    protected Color _background = null;

    public CalendarGrid()
    {
      _foreground = (Color)UIManager.getDefaults().get("Button.foreground");
      _background = (Color)UIManager.getDefaults().get("Button.background");
      _selectionForeground = (Color)UIManager.getDefaults().get("Table.selectionForeground");
      _selectionBackground = (Color)UIManager.getDefaults().get("Table.selectionBackground");
      setLayout(new GridLayout(7,7));
      _dayOfWeek = new JLabel[7];
      Calendar cal = Calendar.getInstance();

      // Depending on locale, the first day may not be Sunday...
      _firstDay = cal.getFirstDayOfWeek();
      int day = _firstDay;
      cal.set(Calendar.DAY_OF_WEEK,day);

      // Make day of week labels abbreviated to 3 characters
      SimpleDateFormat fmt = new SimpleDateFormat("EEE",Locale.US);
      for(int i = 0; i < 7; i++) {
        cal.set(Calendar.DAY_OF_WEEK,day);
        _dayOfWeek[i] = new JLabel(fmt.format(cal.getTime()).substring(0,1));
        _dayOfWeek[i].setHorizontalAlignment(SwingConstants.CENTER);
        if((day == Calendar.SATURDAY) || (day == Calendar.SUNDAY))
          _dayOfWeek[i].setForeground(Color.red);
        add(_dayOfWeek[i]);
        day = (day % 7) + 1;
      }

      // Listen for changes to the model
      _model.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e)
        {
          setDate(_model.getDate());
        }
      });

      // Create all toggle buttons for the grid
      _group = new ButtonGroup();
      _day = new JToggleButton[37];
      for(int i = 0; i < _day.length; i++) {
        _day[i] = new JToggleButton("1");
        Font f = _day[i].getFont();
        _day[i].setFont(f.deriveFont(9.0f));
        _day[i].setMargin(new Insets(0,0,0,0));
        _group.add(_day[i]);
        add(_day[i]);
      }

      // Make the initial selection
      setDate(_model.getDate());

      // Configure the button group
      _group.getSelection().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e)
        {
          ButtonModel m = (ButtonModel)e.getSource();
          if(!_group.isSelected(m)) {
            m.removeChangeListener(this);
            m = _group.getSelection();
            m.addChangeListener(this);
          }
          _model.setValue(getDate());
        }
      });
    }

    public void setEnabled(boolean enabled)
    {
      super.setEnabled(enabled);
      for(JLabel dayOfWeek : _dayOfWeek)
        dayOfWeek.setEnabled(enabled);
      for(JToggleButton day : _day)
        day.setEnabled(enabled);
    }

    public void setDate(Date time)
    {
      Calendar tmp = Calendar.getInstance();

      // Get the day of week for the first of the month
      tmp.setTime(time);
      int date = tmp.get(Calendar.DAY_OF_MONTH);
      tmp.set(Calendar.DAY_OF_MONTH,1);
      final int day = tmp.get(Calendar.DAY_OF_WEEK);

      // Get number of days in the month
      final int maxDay = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);

      // Re-label the buttons
      int i;
      for(i = 0; i < ((day + 7) - _firstDay) % 7; i++)
        disableButton(_day[i]);

      for(int j = 1; j <= maxDay; j++,i++) {
        String str = Integer.toString(j);
        _day[i].setText(str);
        _day[i].setActionCommand(str);
        _day[i].setEnabled(true);
        if(j == date) {
          _day[i].setSelected(true);
          _day[i].setForeground(_selectionForeground);
          _day[i].setBackground(_selectionBackground);
        } else {
          _day[i].setSelected(false);
          _day[i].setForeground(_foreground);
          _day[i].setBackground(_background);
        }
        _day[i].setVisible(true);
      }

      for(; i < _day.length; i++)
        disableButton(_day[i]);

      if(!_model.getDate().equals(time))
        _model.setValue(time);
    }

    protected void disableButton(JToggleButton button)
    {
      button.setText("");
      button.setActionCommand("0");
      button.setEnabled(false);
      button.setSelected(false);
      button.setVisible(false);
    }

    public Date getDate()
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(_model.getDate());
      cal.set(Calendar.DAY_OF_MONTH,getDayOfMonth());
      return cal.getTime();
    }

    public int getDayOfMonth()
    {
      ButtonModel button = _group.getSelection();
      if(button != null) {
        return Integer.parseInt(button.getActionCommand());
      }
      return -1;
    }
  }

  protected class MonthNumber {
    protected int _num = -1;
    protected String _name = null;

    public MonthNumber(int num,String name)
    {
      _num = num;
      _name = name;
    }

    public int getNum()
    {
      return(_num);
    }

    public String toString()
    {
      return(_name);
    }

    public boolean equals(Object o)
    {
      MonthNumber mn = (MonthNumber)o;
      if((_num == mn._num) && (_name.equals(mn._name)))
        return(true);
      return(false);
    }
  }

  public static void main(String[] argv)
  {
    JFrame f = new JFrame("Choose date & time");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().setLayout(new BorderLayout());

    final CalendarChooser cal = new CalendarChooser(false,true);
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR,1995);
    cal.setDate(calendar.getTime());
    cal.setSize(4);
    f.getContentPane().add(cal,BorderLayout.CENTER);
    f.pack();
    f.setVisible(true);
  }
}
