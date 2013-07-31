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

import com.intellij.cvsSupport2.CvsUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vfs.VirtualFile;

public class CVSRevisionGraphAction extends AnAction {
    private final Logger LOG = Logger.getInstance("#org.cvstoolbox.graph.CVSRevisionGraphAction");

    @Override
    public void update(AnActionEvent ae) {
        VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(ae.getDataContext());
        boolean enabled = (file != null && CvsUtil.fileIsUnderCvs(file));
        ae.getPresentation().setEnabledAndVisible(enabled);
    }

    public void actionPerformed(AnActionEvent ae) {
        Project project = null;
        VirtualFile file = null;
        try {
            project = PlatformDataKeys.PROJECT.getData(ae.getDataContext());
            file = PlatformDataKeys.VIRTUAL_FILE.getData(ae.getDataContext());
            ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
            AbstractVcs vcs = vcsManager.getVcsFor(file);
            VcsHistoryProvider histProvider = vcs.getVcsHistoryProvider();
            VcsContextFactory vcsContext = VcsContextFactory.SERVICE.getInstance();
            FilePath filePath = vcsContext.createFilePathOn(file);
            CVSRevisionGraph dialog = new CVSRevisionGraph(project, filePath, histProvider);
            dialog.pack();
            dialog.show();
        } catch (Throwable t) {
            LOG.error(t);

            String title = "Revision Graph Error";
            String message;
            if (file != null) {
                message = "Error obtaining information necessary to calculate revision graph for file: " + file.getName();
            } else {
                message = "Error obtaining information necessary to calculate revision graph for current file";
            }
            message += "\n\n" + t.getMessage();
            if (project != null) {
                Messages.showErrorDialog(project, message, title);
            } else {
                Messages.showErrorDialog(message, title);
            }
        }
    }
}
