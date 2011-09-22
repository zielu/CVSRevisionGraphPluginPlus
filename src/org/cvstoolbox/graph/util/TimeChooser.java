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

import org.cvstoolbox.graph.CVSRevisionGraph;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class TimeChooser extends JComponent {
  public static final int MARGIN = 5;
  public static final int THIN_STROKE_WIDTH = 2;
  public static final int THICK_STROKE_WIDTH = 4;
  public static final double TICK_PERCENT = 0.10;
  public static final double HOUR_PERCENT = 0.50;
  public static final double MINUTE_PERCENT = 0.75;
  public static final double SECOND_PERCENT = 0.80;
  public static final int FONT_SIZE = 14;
  public static final int SHADOW_OFFSET = 4;

  protected Calendar _cal = null;
  protected ClockFace _face = null;
  protected ArrayList<JComponent> _spinnerComps = null;
  protected ArrayList<JLabel> _labelComps = null;
  protected JComboBox _zoneCombo = null;
  protected boolean _ignoreAMPM = false;
  protected JButton _now = null;

  public TimeChooser()
  {
    this(Calendar.getInstance().getTime(),false);
  }

  public TimeChooser(boolean showNow)
  {
    this(Calendar.getInstance().getTime(),showNow);
  }

  public TimeChooser(Date date,boolean showNow)
  {
    _cal = Calendar.getInstance();
    _cal.setTime(date);
    setLayout(new BorderLayout());
    _face = new ClockFace();
    add(_face,BorderLayout.CENTER);
    JPanel controlPanel = new JPanel();
    _spinnerComps = new ArrayList<JComponent>();
    _labelComps = new ArrayList<JLabel>();
    DateTimeFormatTokenizer tokens = new DateTimeFormatTokenizer(CVSRevisionGraph._timeFormat,true);
    while(tokens.hasMoreTokens()) {
      DateTimeFormatToken token = tokens.nextToken();
      if(token.isSpecial()) {
        if(token.getCalendarField() == Calendar.AM_PM) {
          Vector<AMPMNumber> ampmItems = new Vector<AMPMNumber>();
          String ampmStrings[] = CVSRevisionGraph._timeFormat.getDateFormatSymbols().getAmPmStrings();
          ampmItems.add(new AMPMNumber(Calendar.AM,ampmStrings[0]));
          ampmItems.add(new AMPMNumber(Calendar.PM,ampmStrings[1]));
          JComboBox tempCombo = new JComboBox(ampmItems);
          tempCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
              if(_ignoreAMPM)
                return;
              JComboBox thisCombo = (JComboBox)ae.getSource();
              AMPMNumber ampmNum = (AMPMNumber)thisCombo.getSelectedItem();
              //Use add to workaround JDK bug of setting ampm
              if((_cal.get(Calendar.AM_PM) == Calendar.AM) && (ampmNum.getNum() == Calendar.PM))
                _cal.add(Calendar.AM_PM,1);
              else if((_cal.get(Calendar.AM_PM) == Calendar.PM) && (ampmNum.getNum() == Calendar.AM))
                _cal.add(Calendar.AM_PM,-1);
              //No need to repaint
            }
          });
          _spinnerComps.add(tempCombo);
          controlPanel.add(tempCombo);
        } else {
          JSpinner tempSpinner = new JSpinner(new SpinnerDateModel(_cal.getTime(),null,null,token.getCalendarField()));
          //Force a double digit field to solve initial size of spinner
          String fieldPattern = token.toString();
          if(fieldPattern.length() == 1)
            fieldPattern = fieldPattern + fieldPattern;
          JSpinner.DateEditor editor = new JSpinner.DateEditor(tempSpinner,fieldPattern);
          tempSpinner.setEditor(editor);
          tempSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce)
            {
              JSpinner thisSpinner = (JSpinner)ce.getSource();
              SpinnerDateModel model = (SpinnerDateModel)thisSpinner.getModel();
              Date d = model.getDate();
              Calendar cal = Calendar.getInstance();
              cal.setTime(d);
              _cal.set(model.getCalendarField(),cal.get(model.getCalendarField()));
              repaint();
            }
          });
          _spinnerComps.add(tempSpinner);
          controlPanel.add(tempSpinner);
        }
      } else {
        JLabel tempLabel = new JLabel(token.toString());
        _labelComps.add(tempLabel);
        controlPanel.add(tempLabel);
      }
    }
    if(showNow) {
      _now = new JButton("Now");
      _now.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae)
        {
          setDate(Calendar.getInstance().getTime());
        }
      });
      controlPanel.add(_now);
    }
    JPanel tempPanel = new JPanel(new FlowLayout());
    tempPanel.add(controlPanel);
    add(tempPanel,BorderLayout.SOUTH);
    setDate(date);
  }

  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    _face.setEnabled(enabled);
    for(JComponent spinnerComp : _spinnerComps)
      spinnerComp.setEnabled(enabled);
    if(_now != null)
      _now.setEnabled(enabled);
  }

  public void setDate(Date date)
  {
    _cal = Calendar.getInstance();
    _cal.setTime(date);
    //Update controls
    for(JComponent spinnerComp : _spinnerComps) {
      if(spinnerComp instanceof JSpinner) {
        ((JSpinner)spinnerComp).setValue(_cal.getTime());
      } else {
        String ampmStrings[] = CVSRevisionGraph._timeFormat.getDateFormatSymbols().getAmPmStrings();
        int ampm = _cal.get(Calendar.AM_PM);
        AMPMNumber ampmNum;
        if(ampm == Calendar.AM)
          ampmNum = new AMPMNumber(ampm, ampmStrings[0]);
        else
          ampmNum = new AMPMNumber(ampm, ampmStrings[1]);
        _ignoreAMPM = true;
        ((JComboBox)spinnerComp).setSelectedItem(ampmNum);
        _ignoreAMPM = false;
      }
    }
    repaint();
  }

  public Date getDate()
  {
    //Only affect the time portion of a Date, not the date
    Calendar retVal = Calendar.getInstance();
    retVal.set(Calendar.MONTH,Calendar.JANUARY);
    retVal.set(Calendar.DAY_OF_MONTH,1);
    retVal.set(Calendar.YEAR,1970);
    retVal.set(Calendar.HOUR,_cal.get(Calendar.HOUR));
    retVal.set(Calendar.MINUTE,_cal.get(Calendar.MINUTE));
    retVal.set(Calendar.SECOND,_cal.get(Calendar.SECOND));
    retVal.set(Calendar.AM_PM,_cal.get(Calendar.AM_PM));
    return(retVal.getTime());
  }

  public static void main(String[] args)
  {
    JFrame frame = new JFrame("TimeChooser Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    TimeChooser tc = new TimeChooser(true);
    frame.getContentPane().add(tc);
    frame.pack();
    frame.setVisible(true);
    Calendar cal = Calendar.getInstance();
    tc.setDate(cal.getTime());
  }

  protected class AMPMNumber {
    protected int _num = -1;
    protected String _name = null;

    public AMPMNumber(int num,String name)
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
      AMPMNumber mn = (AMPMNumber)o;
      if((_num == mn._num) && (_name.equals(mn._name)))
        return(true);
      return(false);
    }
  }

  protected class ClockFace extends JComponent {
    public ClockFace()
    {
      setPreferredSize(new Dimension(200,200));
    }

    protected void paintComponent(Graphics g)
    {
      //Setup graphics
      Graphics2D g2d = (Graphics2D)g;
      g2d.setStroke(new BasicStroke(THICK_STROKE_WIDTH,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
      Font f = new Font("Serif",Font.PLAIN,FONT_SIZE);
      g2d.setFont(f);
      if(isEnabled())
        g2d.setColor(Color.black);
      else
        g2d.setColor(Color.gray);
      FontRenderContext frc = g2d.getFontRenderContext();
      AffineTransform origTransform = g2d.getTransform();

      //Draw circle
      Rectangle b = getBounds();
      Insets ins = TimeChooser.this.getInsets();
      b = new Rectangle(b.x,b.y,b.width - ins.left - ins.right,b.height - ins.top - ins.bottom);
      double ex;
      double ey;
      double ew;
      if(b.getWidth() < b.getHeight()) {
        ew = b.getWidth() - (2.0 * MARGIN);
        ex = b.getX() + MARGIN;
        ey = b.getY() + ((b.getHeight() - ew) / 2.0);
      } else {
        ew = b.getHeight() - (2.0 * MARGIN);
        ex = b.getX() + ((b.getWidth() - ew) / 2.0);
        ey = b.getY() + MARGIN;
      }
      double r = ew / 2.0;
      double cx = ex + r;
      double cy = ey + r;
      Ellipse2D.Double e = new Ellipse2D.Double(ex,ey,ew,ew);
      g2d.draw(e);
      //Draw tick marks and text
      Line2D.Double l = new Line2D.Double(cx,cy - r,cx,cy - r + (TICK_PERCENT * r));
      AffineTransform t = AffineTransform.getRotateInstance(Math.PI / 6.0,cx,cy);
      for(int i = 0; i < 12; i++) {
        g2d.draw(l);
        String numString;
        if(i == 0)
          numString = "12";
        else
          numString = "" + i;
        Rectangle2D sb = f.getStringBounds(numString,frc);
        g2d.drawString(numString,(float)(cx - (sb.getWidth() / 2.0)),(float)(cy - r + (TICK_PERCENT * r) + sb.getHeight()));
        g2d.transform(t);
      }
      //Draw hands
      int h = _cal.get(Calendar.HOUR);
      int m = _cal.get(Calendar.MINUTE);
      int s = _cal.get(Calendar.SECOND);
      //Reset transformation
      g2d.setTransform(origTransform);
      //Draw hour
      g2d.setColor(Color.blue);
      t = AffineTransform.getRotateInstance((Math.PI / 360.0) * ((h * 60.0) + m),cx,cy);
      g2d.transform(t);
      Line2D.Double hl = new Line2D.Double(cx,cy,cx,cy - (HOUR_PERCENT * r));
      g2d.draw(hl);
      //Reset transformation
      g2d.setTransform(origTransform);
      //Draw minute
      t = AffineTransform.getRotateInstance((Math.PI / 30.0) * m,cx,cy);
      g2d.transform(t);
      Line2D.Double ml = new Line2D.Double(cx,cy,cx,cy - (MINUTE_PERCENT * r));
      g2d.draw(ml);
      //Reset transformation
      g2d.setTransform(origTransform);
      g2d.setStroke(new BasicStroke(THIN_STROKE_WIDTH,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
      //Draw second
      g2d.setColor(Color.red);
      t = AffineTransform.getRotateInstance((Math.PI / 30.0) * s,cx,cy);
      g2d.transform(t);
      Line2D.Double sl = new Line2D.Double(cx,cy,cx,cy - (SECOND_PERCENT * r));
      g2d.draw(sl);
    }
  }
}
