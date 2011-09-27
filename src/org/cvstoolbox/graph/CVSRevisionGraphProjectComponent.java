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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CVSRevisionGraphProjectComponent implements ProjectComponent, Configurable {

    private CVSRevisionGraphProjectConfig config;
    protected CVSRevisionGraphConfiguration _form = null;
    protected Icon _graphIcon = IconLoader.getIcon("/org/cvstoolbox/graph/images/graph_24.png");

    public CVSRevisionGraphProjectComponent(Project project) {
        config = project.getComponent(CVSRevisionGraphProjectConfig.class);
    }

    public String getBeforeTagName(String sourceBranchName, String destBranchName) {
        String retVal = config.get_tagNaming().replace("$T", "BEFORE");
        retVal = retVal.replace("$S", sourceBranchName);
        retVal = retVal.replace("$D", destBranchName);
        return (retVal);
    }

    public String getAfterTagName(String sourceBranchName, String destBranchName) {
        String retVal = config.get_tagNaming().replace("$T", "AFTER");
        retVal = retVal.replace("$S", sourceBranchName);
        retVal = retVal.replace("$D", destBranchName);
        return (retVal);
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return ("CVSRevisionGraphProjectComponent");
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public CVSRevisionGraphProjectConfig getConfig() {
        return config;
    }

    public List<String> getBranchFilter() {
        List<String> retVal = new ArrayList<String>();
        if (config.get_branchFilter() == null) {
            return (retVal);
        }
        String bFilters[] = config.get_branchFilter().split(",");
        Collections.addAll(retVal, bFilters);
        return (retVal);
    }

    public void setBranchFilter(List<String> branchFilter) {
        if ((branchFilter == null) || (branchFilter.size() == 0)) {
            config.set_branchFilter(null);
            return;
        }
        StringBuilder bFilters = new StringBuilder();
        for (String bFilter : branchFilter) {
            if (bFilters.length() != 0) {
                bFilters.append(",");
            }
            bFilters.append(bFilter);
        }
        config.set_branchFilter(bFilters.toString());
    }

    @Nls
    public String getDisplayName() {
        return ("CVS Revision Graph");
    }

    public Icon getIcon() {
        return (_graphIcon);
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return ("doc-cvsRG");
    }

    public JComponent createComponent() {
        if (_form == null) {
            _form = new CVSRevisionGraphConfiguration();
        }
        return (_form.get_mainPanel());
    }

    public boolean isModified() {
        if (_form == null) {
            return (false);
        }
        return (_form.isModified(this));
    }

    public void apply() throws ConfigurationException {
        if (_form == null) {
            return;
        }
        _form.getData(this);
    }

    public void reset() {
        if (_form == null) {
            return;
        }
        _form.setData(this);
    }

    public void disposeUIResources() {
        _form = null;
    }
}
