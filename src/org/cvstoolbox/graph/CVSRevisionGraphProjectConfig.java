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

/*
 * @(#) $Id:  $
 */
package org.cvstoolbox.graph;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;

@State(
    name = "CVSRevisionGraphProjectComponent",
    storages = {
        @Storage(
            id = "other",
            file = "$WORKSPACE_FILE$"
        ),
        @Storage(
            id = "dir",
            file = "$PROJECT_CONFIG_DIR$/other.xml",
            scheme = StorageScheme.DIRECTORY_BASED
        )
    }
)
public class CVSRevisionGraphProjectConfig implements PersistentStateComponent<CVSRevisionGraphProjectConfig> {

    public static final boolean DEFAULT_USE_TWO_TAG_CONVENTION = true;
    public static final boolean DEFAULT_SHOW_TAGS = true;
    public static final boolean DEFAULT_SHOW_TAG_FILTER = false;
    public static final boolean DEFAULT_SHOW_BRANCH_FILTER = false;
    public static final boolean DEFAULT_SHOW_REVISION_FILTER = false;
    public static final boolean DEFAULT_AFTER_DATE_TIME_FILTER = false;
    public static final boolean DEFAULT_BEFORE_DATE_TIME_FILTER = false;
    public static final String DEFAULT_AFTER_DATE_TIME = "";
    public static final String DEFAULT_BEFORE_DATE_TIME = "";
    public static final String DEFAULT_TAG_FILTER = "";
    public static final String DEFAULT_BRANCH_FILTER = null;
    public static final String DEFAULT_TWO_TAG_NAMING = "TAG_$T_MERGE_$S_TO_$D";
    public static final String DEFAULT_ONE_TAG_NAMING = "TAG_MERGE_$S_TO_$D";

    private boolean _useTwoTagConvention = DEFAULT_USE_TWO_TAG_CONVENTION;
    private boolean _showTags = DEFAULT_SHOW_TAGS;
    private String _tagNaming = DEFAULT_TWO_TAG_NAMING;
    private String _tagFilter = DEFAULT_TAG_FILTER;
    private String _branchFilter = DEFAULT_BRANCH_FILTER;
    private boolean _showRevisionFilter = DEFAULT_SHOW_REVISION_FILTER;
    private boolean _afterDateTimeFilter = DEFAULT_AFTER_DATE_TIME_FILTER;
    private boolean _beforeDateTimeFilter = DEFAULT_BEFORE_DATE_TIME_FILTER;
    private String _afterDateTime = DEFAULT_AFTER_DATE_TIME;
    private String _beforeDateTime = DEFAULT_BEFORE_DATE_TIME;
    private boolean _showTagFilter = DEFAULT_SHOW_TAG_FILTER;
    private boolean _showBranchFilter = DEFAULT_SHOW_BRANCH_FILTER;

    public boolean is_useTwoTagConvention() {
        return _useTwoTagConvention;
    }

    public void set_useTwoTagConvention(final boolean useTwoTagConvention) {
        _useTwoTagConvention = useTwoTagConvention;
    }

    public boolean is_showTags() {
        return _showTags;
    }

    public void set_showTags(final boolean showTags) {
        _showTags = showTags;
    }

    public String get_tagFilter() {
        return _tagFilter;
    }

    public void set_tagFilter(final String tagFilter) {
        _tagFilter = tagFilter;
    }

    public String get_branchFilter() {
        return _branchFilter;
    }

    public void set_branchFilter(String branchFilter) {
        _branchFilter = branchFilter;
    }

    public boolean is_showTagFilter() {
        return _showTagFilter;
    }

    public void set_showTagFilter(boolean showTagFilter) {
        _showTagFilter = showTagFilter;
    }

    public boolean is_showBranchFilter() {
        return _showBranchFilter;
    }

    public void set_showBranchFilter(boolean showBranchFilter) {
        _showBranchFilter = showBranchFilter;
    }

    public String get_tagNaming() {
        return _tagNaming;
    }

    public void set_tagNaming(final String tagNaming) {
        _tagNaming = tagNaming;
    }

    public String get_afterDateTime() {
        return _afterDateTime;
    }

    public void set_afterDateTime(String afterDateTime) {
        _afterDateTime = afterDateTime;
    }

    public boolean is_afterDateTimeFilter() {
        return _afterDateTimeFilter;
    }

    public void set_afterDateTimeFilter(boolean afterDateTimeFilter) {
        _afterDateTimeFilter = afterDateTimeFilter;
    }

    public String get_beforeDateTime() {
        return _beforeDateTime;
    }

    public void set_beforeDateTime(String beforeDateTime) {
        _beforeDateTime = beforeDateTime;
    }

    public boolean is_beforeDateTimeFilter() {
        return _beforeDateTimeFilter;
    }

    public void set_beforeDateTimeFilter(boolean beforeDateTimeFilter) {
        _beforeDateTimeFilter = beforeDateTimeFilter;
    }

    public boolean is_showRevisionFilter() {
        return _showRevisionFilter;
    }

    public void set_showRevisionFilter(boolean showRevisionFilter) {
        _showRevisionFilter = showRevisionFilter;
    }

    @Override
    public CVSRevisionGraphProjectConfig getState() {
        return this;
    }

    @Override
    public void loadState(CVSRevisionGraphProjectConfig state) {
        set_afterDateTime(state.get_afterDateTime());
        set_afterDateTimeFilter(state.is_afterDateTimeFilter());
        set_beforeDateTime(state.get_beforeDateTime());
        set_beforeDateTimeFilter(state.is_beforeDateTimeFilter());
        set_branchFilter(state.get_branchFilter());
        set_showBranchFilter(state.is_showBranchFilter());
        set_showRevisionFilter(state.is_showRevisionFilter());
        set_showTagFilter(state.is_showTagFilter());
        set_showTags(state.is_showTags());
        set_tagFilter(state.get_tagFilter());
        set_tagNaming(state.get_tagNaming());
        set_useTwoTagConvention(state.is_useTwoTagConvention());
    }
}
