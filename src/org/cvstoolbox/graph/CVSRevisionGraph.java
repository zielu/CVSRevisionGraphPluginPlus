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

import com.intellij.cvsSupport2.cvsExecution.CvsOperationExecutor;
import com.intellij.cvsSupport2.cvsExecution.CvsOperationExecutorCallback;
import com.intellij.cvsSupport2.cvshandlers.CommandCvsHandler;
import com.intellij.cvsSupport2.cvshandlers.FileSetToBeUpdated;
import com.intellij.cvsSupport2.history.CvsFileRevision;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffTool;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsHistorySession;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.ui.ReplaceFileConfirmationDialog;
import com.intellij.openapi.vcs.vfs.VcsFileSystem;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import info.clearthought.layout.TableLayout;
import org.cvstoolbox.graph.revisions.BranchRevision;
import org.cvstoolbox.graph.revisions.RevisionsContainer;
import org.jetbrains.annotations.Nullable;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CVSRevisionGraph extends DialogWrapper implements GraphSelectionListener {
    public static final int CELL_SPACING = 15;
    public static final int MIN_DIALOG_WIDTH = 600;
    public static final int MIN_DIALOG_HEIGHT = 600;
    public static final String DATE_TIME_FORMAT = "MMM dd, yyyy hh:mm:ss a";
    public static final String TIME_FORMAT = "hh:mm:ss a";

    public static enum CONNECT_TYPE {NO, HALF, YES}

    public static final float DASH_PATTERN[] = new float[]{4, 4};
    public static final SimpleDateFormat _dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
    public static final SimpleDateFormat _timeFormat = new SimpleDateFormat(TIME_FORMAT);

    protected JGraph _g = null;
    protected DefaultGraphModel _gm = null;
    protected CVSRevisionGraphCellViewFactory _cellViewFactory = null;
    protected HashMap<String, DefaultGraphCell> _cellMap = null;
    protected VcsHistoryProvider _historyProvider = null;
    protected List<VcsFileRevision> _revisions = null;
    protected Project _project = null;
    protected JTextField _revisionTF = null;
    protected JTextField _authorTF = null;
    protected JTextField _dateTF = null;
    protected JTextArea _messageTA = null;
    protected DefaultListModel _tagsLM = null;
    protected FilePath _filePath = null;
    protected ActionPopupMenu _viewPopup = null;
    protected ActionPopupMenu _fileRevisionPopup = null;
    protected ActionPopupMenu _branchRevisionPopup = null;
    protected boolean _showBranchFilter = false;
    protected List<String> _branchFilter = null;
    protected boolean _showRevisionFilter = false;
    protected boolean _afterDateTimeFilter = false;
    protected boolean _beforeDateTimeFilter = false;
    protected String _afterDateTime = null;
    protected String _beforeDateTime = null;
    protected RevisionStringComparator _revCompare = null;
    protected VcsFileRevisionDateComparator _revDateCompare = null;

    private RevisionsContainer revisionsContainer;

    private final Logger LOG = Logger.getInstance("#org.cvstoolbox.graph.CVSRevisionGraph");

    public CVSRevisionGraph(Project project, FilePath filePath, VcsHistoryProvider historyProvider) {
        super(project, false);
        setTitle("CVS Revision Graph: " + filePath.getName());
        setModal(false);
        _filePath = filePath;
        _project = project;
        _historyProvider = historyProvider;

        init();
        try {
            getWindow().setIconImage(ImageIO.read(getClass().getResource("/org/cvstoolbox/graph/images/graph_16.png")));
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    protected void init() {
        _revCompare = new RevisionStringComparator();
        revisionsContainer = new RevisionsContainer(_revCompare);
        _revDateCompare = new VcsFileRevisionDateComparator();
        _gm = new DefaultGraphModel();
        _g = new JGraph(_gm);
        _cellViewFactory = new CVSRevisionGraphCellViewFactory(_project);
        CVSRevisionGraphProjectComponent rgpc = _project.getComponent(CVSRevisionGraphProjectComponent.class);
        CVSRevisionGraphProjectConfig config = rgpc.getConfig();
        _cellViewFactory.setShowTags(config.is_showTags());
        _cellViewFactory.setShowTagFilter(config.is_showTagFilter());
        _cellViewFactory.setTagFilter(config.get_tagFilter());
        _g.getGraphLayoutCache().setFactory(_cellViewFactory);
        _g.setHandleSize(0);
        _g.setMoveable(false);
        _cellMap = new HashMap<String, DefaultGraphCell>();
        VcsHistorySession historySession;
        try {
            historySession = _historyProvider.createSessionFor(_filePath);
        } catch (Throwable t) {
            LOG.error(t);
            throw new RuntimeException(t.getMessage());
        }
        _revisions = historySession.getRevisionList();
        _showBranchFilter = config.is_showBranchFilter();
        _branchFilter = rgpc.getBranchFilter();
        if (_branchFilter.isEmpty()) {
            _showBranchFilter = false;
        }
        _showRevisionFilter = config.is_showRevisionFilter();
        _afterDateTimeFilter = config.is_afterDateTimeFilter();
        _beforeDateTimeFilter = config.is_beforeDateTimeFilter();
        _afterDateTime = config.get_afterDateTime();
        _beforeDateTime = config.get_beforeDateTime();
        createGraph();
        layoutGraph();
        setCancelButtonText("Close");
        super.init();
        VcsRevisionNumber currentRevision = historySession.getCurrentRevisionNumber();
        if (currentRevision == null) {
            historySession.shouldBeRefreshed();
            currentRevision = historySession.getCurrentRevisionNumber();
        }
        setCurrentRevision(currentRevision.asString());
    }

    @Nullable
    protected JComponent createCenterPanel() {
        _g.addGraphSelectionListener(this);
        _g.addMouseListener(new PopupListener());
        JBScrollPane sp = new JBScrollPane(_g);
        return (sp);
    }

    protected Action[] createActions() {
        return (new Action[]{getCancelAction()});
    }

    protected JComponent createNorthPanel() {
        CompareAction ca = new CompareAction();
        RefreshAction ra = new RefreshAction();
        AddTagAction ata = new AddTagAction();
        RemoveTagAction rta = new RemoveTagAction();
        MoveMergeTagsAction mmta = new MoveMergeTagsAction();
        GetRevisionAction gra = new GetRevisionAction();
        ZoomInAction zia = new ZoomInAction();
        ZoomOutAction zoa = new ZoomOutAction();
        ZoomDefaultAction zda = new ZoomDefaultAction();
        ToggleShowTagsAction tsta = new ToggleShowTagsAction();
        TagFilterAction tfa = new TagFilterAction();
        DateFilterAction dfa = new DateFilterAction();
        HideSelectedBranchAction hsba = new HideSelectedBranchAction();
        ShowAllBranchesAction saba = new ShowAllBranchesAction();
        DefaultActionGroup toolbarActions = new DefaultActionGroup();
        toolbarActions.add(ra);
        toolbarActions.addSeparator();
        toolbarActions.add(zia);
        toolbarActions.add(zoa);
        toolbarActions.add(zda);
        toolbarActions.addSeparator();
        toolbarActions.add(ca);
        toolbarActions.addSeparator();
        toolbarActions.add(gra);
        toolbarActions.addSeparator();
        toolbarActions.add(ata);
        toolbarActions.add(rta);
        toolbarActions.add(mmta);
        toolbarActions.addSeparator();
        toolbarActions.add(tsta);
        toolbarActions.add(tfa);
        toolbarActions.addSeparator();
        toolbarActions.add(hsba);
        toolbarActions.add(saba);
        toolbarActions.addSeparator();
        toolbarActions.add(dfa);
        ActionManager am = ActionManager.getInstance();
        ActionToolbar actionToolbar = am.createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, true);
        DefaultActionGroup fileRevisionActions = new DefaultActionGroup();
        fileRevisionActions.add(ata);
        fileRevisionActions.add(rta);
        fileRevisionActions.add(mmta);
        fileRevisionActions.addSeparator();
        fileRevisionActions.add(gra);
        _fileRevisionPopup = am.createActionPopupMenu(ActionPlaces.UNKNOWN, fileRevisionActions);
        DefaultActionGroup branchRevisionActions = new DefaultActionGroup();
        branchRevisionActions.add(hsba);
        _branchRevisionPopup = am.createActionPopupMenu(ActionPlaces.UNKNOWN, branchRevisionActions);
        DefaultActionGroup viewActions = new DefaultActionGroup();
        viewActions.add(ra);
        viewActions.addSeparator();
        viewActions.add(zia);
        viewActions.add(zoa);
        viewActions.add(zda);
        viewActions.addSeparator();
        viewActions.add(tsta);
        viewActions.add(tfa);
        viewActions.addSeparator();
        viewActions.add(saba);
        viewActions.addSeparator();
        viewActions.add(dfa);
        _viewPopup = am.createActionPopupMenu(ActionPlaces.UNKNOWN, viewActions);
        return (actionToolbar.getComponent());
    }

    @Nullable
    protected JComponent createSouthPanel() {
        double tableSizes[][] = {{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}, {TableLayout.PREFERRED}};
        TableLayout tl = new TableLayout(tableSizes);
        tl.setHGap(5);
        tl.setVGap(5);
        JPanel retVal = new JPanel(tl);
        retVal.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        double tableSizes2[][] = {{TableLayout.PREFERRED, TableLayout.FILL}, {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}};
        TableLayout tl2 = new TableLayout(tableSizes2);
        tl2.setHGap(5);
        tl2.setVGap(5);
        JPanel innerPanel = new JPanel(tl2);
        innerPanel.add(new JLabel("Revision:"), "0,0,r,f");
        innerPanel.add(new JLabel("Author:"), "0,1,r,f");
        innerPanel.add(new JLabel("Date:"), "0,2,r,f");
        _revisionTF = new JTextField();
        _revisionTF.setEditable(false);
        innerPanel.add(_revisionTF, "1,0");
        _authorTF = new JTextField();
        _authorTF.setEditable(false);
        innerPanel.add(_authorTF, "1,1");
        _dateTF = new JTextField();
        _dateTF.setEditable(false);
        innerPanel.add(_dateTF, "1,2");
        retVal.add(innerPanel, "0,0");
        retVal.add(new JLabel("Message:"), "1,0,f,t");
        _messageTA = new JTextArea();
        _messageTA.setEditable(false);
        JBScrollPane sp = new JBScrollPane(_messageTA);
        retVal.add(sp, "2,0");
        retVal.add(new JLabel("Tags:"), "3,0,f,t");
        _tagsLM = new DefaultListModel();
        JBList tagsL = new JBList(_tagsLM);
        tagsL.setVisibleRowCount(4);
        sp = new JBScrollPane(tagsL);
        retVal.add(sp, "4,0");
        JComponent buttonPanel = super.createSouthPanel();
        retVal.add(buttonPanel, "5,0,r,b");
        return (retVal);
    }

    protected void createGraph() {
        convertToRevisionMap(_revisions);
        DefaultGraphCell lastCell = null;
        boolean notFirst = false;
        for (Map.Entry<BranchRevision, List<VcsFileRevision>> entry : revisionsContainer.getRevisions().entrySet()) {
            BranchRevision branch = entry.getKey();
            CONNECT_TYPE connectType = CONNECT_TYPE.YES;
            if (notFirst) {
                String parentRevision = revisionsContainer.getParentRevision(branch.getRevision());
                lastCell = _cellMap.get(parentRevision);
                //Check if no branch point rev due to hiding
                if (lastCell == null) {
                    connectType = CONNECT_TYPE.HALF;
                    while ((lastCell == null) && (parentRevision != null)) {
                        parentRevision = revisionsContainer.getParentRevision(parentRevision);
                        lastCell = _cellMap.get(parentRevision);
                    }
                }
            }
            lastCell = createCell(lastCell, branch, connectType);
            List<VcsFileRevision> revsOnBranch = entry.getValue();
            for (VcsFileRevision rev : revsOnBranch) {
                lastCell = createCell(lastCell, rev, CONNECT_TYPE.YES);
            }
            notFirst = true;
        }
        //Draw red merge arrows
        for (DefaultGraphCell cell : _cellMap.values()) {
            Object userObj = cell.getUserObject();
            if (userObj instanceof BranchRevision) {
                continue;
            }
            VcsFileRevision rev = (VcsFileRevision) userObj;
            String message = rev.getCommitMessage();
            if ((!message.contains("Merge From: ")) || (!message.contains("Revision: "))) {
                continue;
            }
            int index = message.indexOf("Revision: ");
            //Add length of Revision:
            index += 10;
            StringBuilder revision = new StringBuilder();
            char ch = message.charAt(index);
            while ((Character.isDigit(ch)) || (ch == '.')) {
                revision.append(ch);
                index++;
                if (index >= message.length()) {
                    break;
                }
                ch = message.charAt(index);
            }
            DefaultGraphCell mergeFromCell = _cellMap.get(revision.toString());
            if (mergeFromCell == null) {
                continue;
            }
            createEdge(mergeFromCell, cell, false, JBColor.red);
        }
    }

    protected DefaultGraphCell createCell(DefaultGraphCell neighborCell, Object userObject, CONNECT_TYPE connected) {
        DefaultGraphCell retVal = new DefaultGraphCell(userObject);
        if (userObject instanceof VcsFileRevision) {
            VcsFileRevision rev = (VcsFileRevision) userObject;
            _cellMap.put(rev.getRevisionNumber().asString(), retVal);
        } else if (userObject instanceof BranchRevision) {
            BranchRevision bRev = (BranchRevision) userObject;
            _cellMap.put(bRev.getRevision(), retVal);
        }
        GraphConstants.setOpaque(retVal.getAttributes(), true);
        _g.getGraphLayoutCache().insert(retVal);
        if ((neighborCell != null) && (connected != CONNECT_TYPE.NO)) {
            createEdge(neighborCell, retVal, connected == CONNECT_TYPE.HALF, JBColor.black);
        }
        return (retVal);
    }

    protected DefaultEdge createEdge(DefaultGraphCell source, DefaultGraphCell target, boolean dashed, Color color) {
        DefaultPort sourceP = new DefaultPort();
        source.add(sourceP);
        DefaultPort targetP = new DefaultPort();
        target.add(targetP);
        DefaultEdge retVal = new DefaultEdge();
        retVal.setSource(sourceP);
        retVal.setTarget(targetP);
        GraphConstants.setLineEnd(retVal.getAttributes(), GraphConstants.ARROW_CLASSIC);
        GraphConstants.setEndFill(retVal.getAttributes(), true);
        GraphConstants.setLineColor(retVal.getAttributes(), color);
        if (dashed) {
            GraphConstants.setDashPattern(retVal.getAttributes(), DASH_PATTERN);
        }
        _g.getGraphLayoutCache().insert(retVal);
        return (retVal);
    }

    protected void convertToRevisionMap(List<VcsFileRevision> revisions) {
        revisionsContainer.set_afterDateTime(_afterDateTime);
        revisionsContainer.set_afterDateTimeFilter(_afterDateTimeFilter);
        revisionsContainer.set_beforeDateTime(_beforeDateTime);
        revisionsContainer.set_beforeDateTimeFilter(_beforeDateTimeFilter);
        revisionsContainer.set_branchFilter(_branchFilter);
        revisionsContainer.set_showBranchFilter(_showBranchFilter);
        revisionsContainer.set_showRevisionFilter(_showRevisionFilter);
        revisionsContainer.convertToRevisionMap(revisions);
    }

    protected void layoutGraph() {
        DefaultGraphCell lastCell = null;
        Double nextX = null;

        boolean notFirst = false;
        for (Map.Entry<BranchRevision, List<VcsFileRevision>> entry : revisionsContainer.getRevisions().entrySet()) {
            BranchRevision branch = entry.getKey();
            if (notFirst) {
                String parentRevision = revisionsContainer.getParentRevision(branch.getRevision());
                lastCell = _cellMap.get(parentRevision);
                //Check if no branch point rev due to hiding
                if (lastCell == null) {
                    while ((lastCell == null) && (parentRevision != null)) {
                        parentRevision = revisionsContainer.getParentRevision(parentRevision);
                        lastCell = _cellMap.get(parentRevision);
                    }
                }
            }
            lastCell = positionCell(lastCell, SwingConstants.RIGHT, nextX, branch);
            Rectangle2D rect = GraphConstants.getBounds(lastCell.getAttributes());
            Double widestX = rect.getX() + rect.getWidth();
            List<VcsFileRevision> revsOnBranch = entry.getValue();
            for (VcsFileRevision rev : revsOnBranch) {
                lastCell = positionCell(lastCell, SwingConstants.BOTTOM, nextX, rev);
                rect = GraphConstants.getBounds(lastCell.getAttributes());
                if (widestX == null) {
                    widestX = rect.getX() + rect.getWidth();
                } else {
                    double wideX = rect.getX() + rect.getWidth();
                    if (widestX < wideX) {
                        widestX = wideX;
                    }
                }
            }
            nextX = widestX + CELL_SPACING;
            notFirst = true;
        }
        //Now adjust y position in order of date
        //Sort revision list by date for use later in layoutGraph
        Collections.sort(_revisions, _revDateCompare);
        lastCell = null;
        for (int i = 0, n = _revisions.size(); i < n; i++) {
            VcsFileRevision rev = _revisions.get(i);
            //Skip first revision since everything will be based on it
            if (lastCell == null) {
                lastCell = _cellMap.get(rev.getRevisionNumber().asString());
                continue;
            }
            DefaultGraphCell cell = _cellMap.get(rev.getRevisionNumber().asString());
            if (cell == null) {
                continue;
            }
            Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
            lastCell = positionCell(lastCell, SwingConstants.BOTTOM, rect.getX(), rev);
        }
        //Now fix branch header cells since branch point revisions may have been moved down
        boolean isFirst = true;
        for (Map.Entry<BranchRevision, List<VcsFileRevision>> entry : revisionsContainer.getRevisions().entrySet()) {
            if (isFirst) {
                isFirst = false;
                continue;
            }
            BranchRevision branch = entry.getKey();
            List<VcsFileRevision> revsOnBranch = entry.getValue();
            //Use branch point rev as placement guide
            String parentRevision = revisionsContainer.getParentRevision(branch.getRevision());
            DefaultGraphCell parentCell = _cellMap.get(parentRevision);
            //Check if no branch point rev due to hiding
            if (parentCell == null) {
                while ((parentCell == null) && (parentRevision != null)) {
                    parentRevision = revisionsContainer.getParentRevision(parentRevision);
                    parentCell = _cellMap.get(parentRevision);
                }
            }
            DefaultGraphCell branchCell = _cellMap.get(branch.getRevision());
            Rectangle2D branchRect = GraphConstants.getBounds(branchCell.getAttributes());
            positionCell(parentCell, SwingConstants.RIGHT, branchRect.getX(), branch);
            branchRect = GraphConstants.getBounds(branchCell.getAttributes());
            //Check for overlap with first rev in branch
            if (!revsOnBranch.isEmpty()) {
                VcsFileRevision firstRev = revsOnBranch.get(0);
                DefaultGraphCell firstCell = _cellMap.get(firstRev.getRevisionNumber().asString());
                Rectangle2D firstRect = GraphConstants.getBounds(firstCell.getAttributes());
                if ((firstRect.getY() - CELL_SPACING) < (branchRect.getY() + branchRect.getHeight())) {
                    positionCell(firstCell, SwingConstants.TOP, null, branch);
                }
            }
        }
        //Now calculate visual extents of each branch
        HashMap<String, List<DefaultGraphCell>> branchCells = new HashMap<String, List<DefaultGraphCell>>();
        HashMap<String, Rectangle2D> branchBounds = new HashMap<String, Rectangle2D>();
        for (Map.Entry<String, DefaultGraphCell> entry : _cellMap.entrySet()) {
            String branchRevision = entry.getKey();
            DefaultGraphCell cell = entry.getValue();
            Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
            if (!revisionsContainer.isRevisionBranch(branchRevision)) {
                branchRevision = revisionsContainer.getParentRevision(branchRevision);
            }
            List<DefaultGraphCell> cells = branchCells.get(branchRevision);
            if (cells == null) {
                cells = new ArrayList<DefaultGraphCell>();
                branchCells.put(branchRevision, cells);
            }
            cells.add(cell);
            Rectangle2D branchRect = branchBounds.get(branchRevision);
            if (branchRect == null) {
                branchRect = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                branchBounds.put(branchRevision, branchRect);
            } else {
                Rectangle2D.union(branchRect, rect, branchRect);
            }
        }
        //Sort branches
        List<String> branchRevisions = new ArrayList<String>(branchBounds.keySet());
        Collections.sort(branchRevisions, _revCompare);
        //Now optimize width of graph by moving non-overlapping branches closer together
        //Start at 3 branch since the first two will never need to be moved closer
        for (int i = 2, n = branchRevisions.size(); i < n; i++) {
            String branchRevision = branchRevisions.get(i);
            //Find closest ancestor
            String ancestorRevision = null;
            String candidateAncestor = revisionsContainer.getGrandParentRevision(branchRevision);
            while (candidateAncestor != null) {
                if (branchCells.containsKey(candidateAncestor)) {
                    ancestorRevision = candidateAncestor;
                    break;
                }
                candidateAncestor = revisionsContainer.getGrandParentRevision(candidateAncestor);
            }
            if (ancestorRevision == null) {
                continue;
            }
            Rectangle2D ancestorRect = branchBounds.get(ancestorRevision);
            Double suggestX = moveBranch(branchRevision, branchBounds, branchCells, ancestorRect.getX() + ancestorRect.getWidth() + CELL_SPACING);
            while (suggestX != null) {
                suggestX = moveBranch(branchRevision, branchBounds, branchCells, suggestX);
            }
        }
    }

    //Returns null if move successful, otherwise, returns suggested X location to try again
    protected Double moveBranch(String branchRevision, HashMap<String, Rectangle2D> branchBounds, HashMap<String, List<DefaultGraphCell>> branchCellMap, double x) {
        Rectangle2D oldBranchBounds = branchBounds.get(branchRevision);
        Rectangle2D.Double newBranchBounds = new Rectangle2D.Double(x, oldBranchBounds.getY(), oldBranchBounds.getWidth(), oldBranchBounds.getHeight());
        if (newBranchBounds.equals(oldBranchBounds)) {
            return (null);
        }
        //Check for collisions
        for (Map.Entry<String, Rectangle2D> entry : branchBounds.entrySet()) {
            String candidateBranchRevision = entry.getKey();
            Rectangle2D candidateRect = entry.getValue();
            //Skip myself
            if (candidateBranchRevision.equals(branchRevision)) {
                continue;
            }
            if (newBranchBounds.intersects(candidateRect)) {
                return (candidateRect.getX() + candidateRect.getWidth() + CELL_SPACING);
            }
        }
        branchBounds.put(branchRevision, newBranchBounds);
        List<DefaultGraphCell> branchCellList = branchCellMap.get(branchRevision);
        for (DefaultGraphCell cell : branchCellList) {
            Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
            rect = new Rectangle2D.Double(x, rect.getY(), rect.getWidth(), rect.getHeight());
            GraphConstants.setBounds(cell.getAttributes(), rect);
            _g.getGraphLayoutCache().editCell(cell, cell.getAttributes());
        }
        return (null);
    }

    protected DefaultGraphCell positionCell(DefaultGraphCell neighborCell, int relativePosition, Double preferredX, Object userObject) {
        DefaultGraphCell retVal = null;
        if (userObject instanceof VcsFileRevision) {
            VcsFileRevision rev = (VcsFileRevision) userObject;
            retVal = _cellMap.get(rev.getRevisionNumber().asString());
        } else if (userObject instanceof BranchRevision) {
            BranchRevision bRev = (BranchRevision) userObject;
            retVal = _cellMap.get(bRev.getRevision());
        }
        Rectangle2D userRect = GraphConstants.getBounds(retVal.getAttributes());
        //Calculate position from neighbor
        Point2D.Double p = null;
        Rectangle2D neighborRect = null;
        if (neighborCell == null) {
            if (relativePosition == SwingConstants.BOTTOM) {
                neighborRect = new Rectangle2D.Double(CELL_SPACING, 0, 0, 0);
            } else if (relativePosition == SwingConstants.RIGHT) {
                neighborRect = new Rectangle2D.Double(0, CELL_SPACING, 0, 0);
            }
        } else {
            neighborRect = GraphConstants.getBounds(neighborCell.getAttributes());
        }
        if (relativePosition == SwingConstants.BOTTOM) {
            double x = neighborRect.getX();
            double y = neighborRect.getY() + neighborRect.getHeight() + CELL_SPACING;
            p = new Point2D.Double(x, y);
        } else if (relativePosition == SwingConstants.RIGHT) {
            double x = neighborRect.getX() + neighborRect.getWidth() + CELL_SPACING;
            double y = neighborRect.getY();
            p = new Point2D.Double(x, y);
        } else if (relativePosition == SwingConstants.TOP) {
            double x = neighborRect.getX();
            double y = neighborRect.getY() - userRect.getHeight() - CELL_SPACING;
            p = new Point2D.Double(x, y);
        }
        if (preferredX != null) {
            p.setLocation(preferredX.doubleValue(), p.getY());
        }
        CellView cv = _g.getGraphLayoutCache().getMapping(retVal, true);
        Component comp = cv.getRendererComponent(_g, false, false, false);
        Dimension prefSize = comp.getPreferredSize();
        neighborRect = new Rectangle2D.Double(p.getX(), p.getY(), prefSize.getWidth(), prefSize.getHeight());
        GraphConstants.setBounds(retVal.getAttributes(), neighborRect);
        _g.getGraphLayoutCache().editCell(retVal, retVal.getAttributes());
        return (retVal);
    }

    public void dispose() {
        _g.removeGraphSelectionListener(this);
        revisionsContainer.dispose();
        _cellMap.clear();
        _cellMap = null;
        GraphLayoutCache glc = _g.getGraphLayoutCache();
        glc.remove(glc.getCells(glc.getAllViews()));
        _g.setModel(new DefaultGraphModel());
        glc.setFactory(null);
        _cellViewFactory = null;
        _g = null;
        _project = null;
        _filePath = null;
        super.dispose();
    }

    public void setCurrentRevision(String currentRevision) {
        _cellViewFactory.setCurrentRevision(currentRevision);
        DefaultGraphCell cell = _cellMap.get(currentRevision);
        _g.setSelectionCell(cell);
        _g.scrollCellToVisible(cell);
    }

    public void compare() {
        Object selCells[] = _g.getSelectionCells();
        if ((selCells == null) || (selCells.length != 2)) {
            return;
        }
        DefaultGraphCell selCell1 = (DefaultGraphCell) selCells[0];
        DefaultGraphCell selCell2 = (DefaultGraphCell) selCells[1];
        VcsFileRevision selRev1 = (VcsFileRevision) selCell1.getUserObject();
        VcsFileRevision selRev2 = (VcsFileRevision) selCell2.getUserObject();
        compareFileRevisions(selRev1, selRev2);
    }

    protected void compareFileRevisions(VcsFileRevision rev1, VcsFileRevision rev2) {
        if (_revCompare.compare(rev1, rev2) > 0) {
            VcsFileRevision tempRev = rev2;
            rev2 = rev1;
            rev1 = tempRev;
        }
        VcsVirtualFile selFile1 = new VcsVirtualFile(_filePath.getPath(), rev1, VcsFileSystem.getInstance());
        VcsVirtualFile selFile2 = new VcsVirtualFile(_filePath.getPath(), rev2, VcsFileSystem.getInstance());
        DiffTool dt = DiffManager.getInstance().getDiffTool();
        dt.show(SimpleDiffRequest.compareFiles(selFile1, selFile2, _project, _filePath.getName() + ": " + rev1.getRevisionNumber().asString() + "->" + rev2.getRevisionNumber().asString()));
    }

    public void refresh(boolean readRevisions) {
        GraphLayoutCache glc = _g.getGraphLayoutCache();
        glc.remove(glc.getCells(glc.getAllViews()));
        _cellMap = new HashMap<String, DefaultGraphCell>();
        VcsHistorySession historySession = null;
        if (readRevisions) {
            try {
                historySession = _historyProvider.createSessionFor(_filePath);
            } catch (Throwable t) {
                throw new RuntimeException(t.getMessage());
            }
            _revisions = historySession.getRevisionList();
        }
        createGraph();
        layoutGraph();
        if (readRevisions) {
            VcsRevisionNumber currentRevision = historySession.getCurrentRevisionNumber();
            if (currentRevision == null) {
                historySession.shouldBeRefreshed();
                currentRevision = historySession.getCurrentRevisionNumber();
            }
            setCurrentRevision(currentRevision.asString());
        }

    }

    public void zoomIn() {
        _g.setScale(_g.getScale() + 0.1);
    }

    public void zoomOut() {
        double scale = _g.getScale();
        if (scale <= 0.1) {
            return;
        }
        _g.setScale(scale - 0.1);
    }

    public void zoomDefault() {
        _g.setScale(1.0);
    }

    public void addTag(String tagName) {
        Object selCells[] = _g.getSelectionCells();
        if ((selCells == null) || (selCells.length != 1)) {
            return;
        }
        boolean doRefresh = true;
        if (tagName == null) {
            tagName = Messages.showInputDialog(_project, "Enter tag name to add:", "Add Tag", Messages.getQuestionIcon());
            if (tagName == null) {
                return;
            }
        } else {
            doRefresh = false;
        }
        DefaultGraphCell selCell = (DefaultGraphCell) selCells[0];
        VcsFileRevision selRev = (VcsFileRevision) selCell.getUserObject();
        FilePath filePaths[] = new FilePath[1];
        filePaths[0] = _filePath;
        CvsOperationExecutor executor = new CvsOperationExecutor(_project);
        FileSetToBeUpdated fileSet = FileSetToBeUpdated.selectedFiles(filePaths);
        ExtendedTagOperation tagOp = new ExtendedTagOperation(filePaths, tagName, false, true, selRev.getRevisionNumber().asString());
        CommandCvsHandler tagHandler = new CommandCvsHandler("tag", tagOp, fileSet, false);
        executor.performActionSync(tagHandler, CvsOperationExecutorCallback.EMPTY);
        if (doRefresh) {
            refresh(true);
        }
    }

    public void removeTag() {
        Object selCells[] = _g.getSelectionCells();
        if ((selCells == null) || (selCells.length != 1)) {
            return;
        }
        DefaultGraphCell selCell = (DefaultGraphCell) selCells[0];
        VcsFileRevision selRev = (VcsFileRevision) selCell.getUserObject();
        Collection<String> selRevLabels = null;
        if (selRev instanceof CvsFileRevision) {
            CvsFileRevision cvsRev = (CvsFileRevision) selRev;
            //noinspection unchecked
            selRevLabels = cvsRev.getTags();
        }
        if (selRevLabels == null) {
            selRevLabels = new ArrayList<String>();
        }
        String selRevLabelChoices[] = selRevLabels.toArray(new String[selRevLabels.size()]);
        Arrays.sort(selRevLabelChoices);
        if (selRevLabelChoices.length == 0) {
            Messages.showMessageDialog(_project, "There are no tags on this revision to remove", "Invalid Revision", Messages.getErrorIcon());
            return;
        }
        int tagIndex = Messages.showChooseDialog(_project, "Select tag name to remove:", "Remove Tag", Messages.getQuestionIcon(), selRevLabelChoices, null);
        if (tagIndex == -1) {
            return;
        }
        FilePath filePaths[] = new FilePath[1];
        filePaths[0] = _filePath;
        CvsOperationExecutor executor = new CvsOperationExecutor(_project);
        FileSetToBeUpdated fileSet = FileSetToBeUpdated.selectedFiles(filePaths);
        ExtendedTagOperation tagOp = new ExtendedTagOperation(filePaths, selRevLabelChoices[tagIndex], true, true, selRev.getRevisionNumber().asString());
        CommandCvsHandler tagHandler = new CommandCvsHandler("tag", tagOp, fileSet, false);
        executor.performActionSync(tagHandler, CvsOperationExecutorCallback.EMPTY);
        refresh(true);
    }

    public void moveMergeTags() {
        CVSMoveMergeTagsOptions moveTagsOptions = new CVSMoveMergeTagsOptions(_project, revisionsContainer.getBranches());
        moveTagsOptions.pack();
        moveTagsOptions.show();
        CVSMoveMergeTagsOptions.MoveMergeTagsOptions options = moveTagsOptions.getReturnValue();
        if (options == null) {
            return;
        }
        CVSRevisionGraphProjectComponent rgpc = _project.getComponent(CVSRevisionGraphProjectComponent.class);
        if (options._moveBefore) {
            String beforeTagName = rgpc.getBeforeTagName(options._sourceBranchName, options._destBranchName);
            addTag(beforeTagName);
        }
        if ((options._moveAfter) && rgpc.getConfig().is_useTwoTagConvention()) {
            String afterTagName = rgpc.getAfterTagName(options._sourceBranchName, options._destBranchName);
            addTag(afterTagName);
        }
        refresh(true);
    }

    public void getRevision() {
        Object selCells[] = _g.getSelectionCells();
        if ((selCells == null) || (selCells.length != 1)) {
            return;
        }
        DefaultGraphCell selCell = (DefaultGraphCell) selCells[0];
        VcsFileRevision selRev = (VcsFileRevision) selCell.getUserObject();
        try {
            Editor editor = FileEditorManager.getInstance(_project).getSelectedTextEditor();
            final Document doc = editor.getDocument();
            //Check not modified
            ReplaceFileConfirmationDialog confirmDialog = new ReplaceFileConfirmationDialog(_project, "Get Selected Revision");
            VirtualFile confirmFiles[] = new VirtualFile[1];
            VirtualFile selFile = FileDocumentManager.getInstance().getFile(doc);
            confirmFiles[0] = selFile;
            if (!confirmDialog.confirmFor(confirmFiles)) {
                return;
            }
            selRev.loadContent();
            byte content[] = selRev.getContent();
            final String contentStr = new String(content);
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    doc.setText(contentStr);
                    FileDocumentManager.getInstance().saveDocument(doc);
                }
            });
        } catch (Throwable t) {
            Messages.showErrorDialog(_project, "Cannot get selected revision", "CVS Error");
        }
    }

    public void hideSelectedBranch() {
        Object selCells[] = _g.getSelectionCells();
        if ((selCells == null) || (selCells.length != 1)) {
            return;
        }
        DefaultGraphCell selCell = (DefaultGraphCell) selCells[0];
        BranchRevision selRev = (BranchRevision) selCell.getUserObject();
        if (_showBranchFilter) {
            _branchFilter.remove(selRev.getName());
        } else {
            _branchFilter.add(selRev.getName());
        }
        refresh(false);
    }

    public void showAllBranches() {
        if (_showBranchFilter) {
            _showBranchFilter = false;
        }
        _branchFilter.clear();
        refresh(false);
    }

    public void toggleShowTags() {
        _cellViewFactory.setShowTags(!_cellViewFactory.isShowTags());
        refresh(false);
    }

    public void tagFilter() {
        TagFilterOptions dialog = new TagFilterOptions(_project);
        dialog.show(_cellViewFactory.isShowTagFilter(), _cellViewFactory.getTagFilter());
        TagFilterOptions.TagFilteringOptions options = dialog.getReturnValue();
        if (options == null) {
            return;
        }
        _cellViewFactory.setShowTagFilter(options._showTagFilter);
        _cellViewFactory.setTagFilter(options._tagFilter);
        refresh(false);
    }

    public void dateFilter() {
        DateFilterOptions dialog = new DateFilterOptions(_project);
        dialog.show(_showRevisionFilter, _afterDateTimeFilter, _beforeDateTimeFilter, _afterDateTime, _beforeDateTime);
        DateFilterOptions.DateFilteringOptions options = dialog.getReturnValue();
        if (options == null) {
            return;
        }
        _showRevisionFilter = options._showRevisionFilter;
        _afterDateTimeFilter = options._afterDateTimeFilter;
        _beforeDateTimeFilter = options._beforeDateTimeFilter;
        _afterDateTime = options._afterDateTime;
        _beforeDateTime = options._beforeDateTime;
        refresh(false);
    }

    public void valueChanged(GraphSelectionEvent gse) {
        Object selCells[] = _g.getSelectionCells();
        Object selectedRev1 = null;
        Object selectedRev2 = null;
        if ((selCells != null) && (selCells.length == 1)) {
            if (selCells[0] instanceof Edge) {
                Edge edge = (Edge) selCells[0];
                DefaultPort port1 = (DefaultPort) edge.getSource();
                DefaultGraphCell cell1 = (DefaultGraphCell) port1.getParent();
                Object user1 = cell1.getUserObject();
                DefaultPort port2 = (DefaultPort) edge.getTarget();
                DefaultGraphCell cell2 = (DefaultGraphCell) port2.getParent();
                Object user2 = cell2.getUserObject();
                if ((user1 instanceof VcsFileRevision) && (user2 instanceof VcsFileRevision)) {
                    selectedRev1 = user1;
                    selectedRev2 = user2;
                }
            } else if (selCells[0] instanceof GraphCell) {
                selectedRev1 = ((DefaultGraphCell) selCells[0]).getUserObject();
                if (selectedRev1 instanceof BranchRevision) {
                    selectedRev1 = null;
                }
            }
        }
        _tagsLM.removeAllElements();
        if ((selectedRev1 == null) || (selectedRev2 != null)) {
            _revisionTF.setText(null);
            _authorTF.setText(null);
            _dateTF.setText(null);
            _messageTA.setText(null);
        } else {
            VcsFileRevision rev = (VcsFileRevision) selectedRev1;
            _revisionTF.setText(rev.getRevisionNumber().asString());
            _authorTF.setText(rev.getAuthor());
            Date d = rev.getRevisionDate();
            if (d != null) {
                _dateTF.setText(_dateTimeFormat.format(d));
            }
            _messageTA.setText(rev.getCommitMessage());
            if (rev instanceof CvsFileRevision) {
                CvsFileRevision cvsRev = (CvsFileRevision) rev;
                Collection<String> revLabelsC = cvsRev.getTags();
                if (revLabelsC != null) {
                    ArrayList<String> revLabels = new ArrayList<String>(revLabelsC);
                    Collections.sort(revLabels);
                    for (String revLabel : revLabels) {
                        _tagsLM.addElement(revLabel);
                    }
                }
            }
        }
        if (selectedRev2 != null) {
            VcsFileRevision rev1 = (VcsFileRevision) selectedRev1;
            VcsFileRevision rev2 = (VcsFileRevision) selectedRev2;
            compareFileRevisions(rev1, rev2);
        }
    }

    protected void oneFileRevisionCellSelectedRequired(Presentation p) {
        if (_g == null) {
            p.setEnabled(false);
            return;
        }
        Object selCells[] = _g.getSelectionCells();
        if ((selCells != null) && (selCells.length == 1)) {
            if (((DefaultGraphCell) selCells[0]).getUserObject() instanceof VcsFileRevision) {
                p.setEnabled(true);
            } else {
                p.setEnabled(false);
            }
        } else {
            p.setEnabled(false);
        }
    }

    protected void oneBranchRevisionCellSelectedRequired(Presentation p) {
        if (_g == null) {
            p.setEnabled(false);
            return;
        }
        Object selCells[] = _g.getSelectionCells();
        if ((selCells != null) && (selCells.length == 1)) {
            if (((DefaultGraphCell) selCells[0]).getUserObject() instanceof BranchRevision) {
                p.setEnabled(true);
            } else {
                p.setEnabled(false);
            }
        } else {
            p.setEnabled(false);
        }
    }

    protected void twoFileRevisionSellsSelectedRequired(Presentation p) {
        if (_g == null) {
            p.setEnabled(false);
            return;
        }
        Object selCells[] = _g.getSelectionCells();
        if ((selCells != null) && (selCells.length == 2)) {
            if ((((DefaultGraphCell) selCells[0]).getUserObject() instanceof VcsFileRevision) && (((DefaultGraphCell) selCells[1]).getUserObject() instanceof VcsFileRevision)) {
                p.setEnabled(true);
            } else {
                p.setEnabled(false);
            }
        } else {
            p.setEnabled(false);
        }
    }

    protected class CompareAction extends AnAction {
        public CompareAction() {
            super("Compare Revisions", "Compare Revisions", IconLoader.getIcon("/actions/diff.png"));
            registerCustomShortcutSet(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, getRootPane());
        }

        public void actionPerformed(AnActionEvent ae) {
            compare();
        }

        public void update(AnActionEvent ae) {
            twoFileRevisionSellsSelectedRequired(ae.getPresentation());
        }
    }

    protected class RefreshAction extends AnAction {
        public RefreshAction() {
            super("Refresh Graph", "Refresh Graph", IconLoader.getIcon("/org/cvstoolbox/graph/images/arrow-circle-double-135.png"));
            registerCustomShortcutSet(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, getRootPane());
        }

        public void actionPerformed(AnActionEvent ae) {
            refresh(true);
        }
    }

    protected class ZoomInAction extends AnAction {
        public ZoomInAction() {
            super("Zoom In", "Zoom In", IconLoader.getIcon("/org/cvstoolbox/graph/images/magnifier-zoom-in.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            zoomIn();
        }
    }

    protected class ZoomOutAction extends AnAction {
        public ZoomOutAction() {
            super("Zoom Out", "Zoom Out", IconLoader.getIcon("/org/cvstoolbox/graph/images/magnifier-zoom-out.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            zoomOut();
        }
    }

    protected class ZoomDefaultAction extends AnAction {
        public ZoomDefaultAction() {
            super("Zoom Default", "Zoom Default", IconLoader.getIcon("/org/cvstoolbox/graph/images/magnifier-zoom-fit.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            zoomDefault();
        }
    }

    protected class AddTagAction extends AnAction {
        public AddTagAction() {
            super("Add Tag to Selected Revision", "Add Tag to Selected Revision", IconLoader.getIcon("/org/cvstoolbox/graph/images/tag--plus.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            addTag(null);
        }

        public void update(AnActionEvent ae) {
            oneFileRevisionCellSelectedRequired(ae.getPresentation());
        }
    }

    protected class RemoveTagAction extends AnAction {
        public RemoveTagAction() {
            super("Remove Tag from Selected Revision", "Remove Tag from Selected Revision", IconLoader.getIcon("/org/cvstoolbox/graph/images/tag--minus.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            removeTag();
        }

        public void update(AnActionEvent ae) {
            oneFileRevisionCellSelectedRequired(ae.getPresentation());
        }
    }

    protected class MoveMergeTagsAction extends AnAction {
        public MoveMergeTagsAction() {
            super("Move Merge Tags to Selected Revision", "Move Merge Tags to Selected Revision", IconLoader.getIcon("/org/cvstoolbox/graph/images/tag--pencil.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            moveMergeTags();
        }

        public void update(AnActionEvent ae) {
            oneFileRevisionCellSelectedRequired(ae.getPresentation());
        }
    }

    protected class GetRevisionAction extends AnAction {
        public GetRevisionAction() {
            super("Get Selected Revision as Current", "Get Selected Revision as Current", IconLoader.getIcon("/org/cvstoolbox/graph/images/get.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            getRevision();
        }

        public void update(AnActionEvent ae) {
            oneFileRevisionCellSelectedRequired(ae.getPresentation());
        }
    }

    protected class HideSelectedBranchAction extends AnAction {
        public HideSelectedBranchAction() {
            super("Hide Selected Branch", "Hide Selected Branch", IconLoader.getIcon("/org/cvstoolbox/graph/images/hideBranch.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            hideSelectedBranch();
        }

        public void update(AnActionEvent ae) {
            oneBranchRevisionCellSelectedRequired(ae.getPresentation());
        }
    }

    protected class ShowAllBranchesAction extends AnAction {
        public ShowAllBranchesAction() {
            super("Show All Branches", "Show All Branches", IconLoader.getIcon("/org/cvstoolbox/graph/images/showAllBranches.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            showAllBranches();
        }

        public void update(AnActionEvent ae) {
            Presentation p = ae.getPresentation();
            if (!_showBranchFilter) {
                p.setEnabled(!_branchFilter.isEmpty());
            } else {
                p.setEnabled(true);
            }
        }
    }

    protected class ToggleShowTagsAction extends AnAction {
        public void actionPerformed(AnActionEvent ae) {
            toggleShowTags();
        }

        public void update(AnActionEvent ae) {
            Presentation p = ae.getPresentation();
            if (_cellViewFactory.isShowTags()) {
                p.setText("Hide Tags within Cells");
                p.setDescription("Hide Tags within Cells");
                p.setIcon(IconLoader.getIcon("/org/cvstoolbox/graph/images/hideTags.png"));
            } else {
                p.setText("Show Tags within Cells");
                p.setDescription("Show Tags within Cells");
                p.setIcon(IconLoader.getIcon("/org/cvstoolbox/graph/images/showTags.png"));
            }
        }
    }

    protected class TagFilterAction extends AnAction {
        public TagFilterAction() {
            super("Set Tag Filter", "Set Tag Filter", IconLoader.getIcon("/org/cvstoolbox/graph/images/tagFilter.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            tagFilter();
        }

        public void update(AnActionEvent ae) {
            Presentation p = ae.getPresentation();
            p.setEnabled(_cellViewFactory.isShowTags());
        }
    }

    protected class DateFilterAction extends AnAction {
        public DateFilterAction() {
            super("Set Date Filter", "Set Date Filter", IconLoader.getIcon("/org/cvstoolbox/graph/images/dateFilter.png"));
        }

        public void actionPerformed(AnActionEvent ae) {
            dateFilter();
        }
    }

    protected class PopupListener extends MouseAdapter {
        protected void maybeShowPopup(MouseEvent me) {
            if (me.isPopupTrigger()) {
                DefaultGraphCell cell = (DefaultGraphCell) _g.getFirstCellForLocation(me.getX(), me.getY());
                if (cell == null) {
                    _viewPopup.getComponent().show(me.getComponent(), me.getX(), me.getY());
                } else {
                    Object user = cell.getUserObject();
                    if (user instanceof VcsFileRevision) {
                        _fileRevisionPopup.getComponent().show(me.getComponent(), me.getX(), me.getY());
                    } else if (user instanceof BranchRevision) {
                        _branchRevisionPopup.getComponent().show(me.getComponent(), me.getX(), me.getY());
                    }
                }
            }
        }

        public void mousePressed(MouseEvent me) {
            maybeShowPopup(me);
        }

        public void mouseReleased(MouseEvent me) {
            maybeShowPopup(me);
        }
    }
}
