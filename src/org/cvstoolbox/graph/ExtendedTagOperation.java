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

import java.io.IOException;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.AbstractCommand;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.ICvsFiles;
import org.netbeans.lib.cvsclient.command.IOCommandException;
import org.netbeans.lib.cvsclient.command.DefaultEntryParser;
import org.netbeans.lib.cvsclient.IRequestProcessor;
import org.netbeans.lib.cvsclient.IClientEnvironment;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.request.Requests;
import org.netbeans.lib.cvsclient.request.CommandRequest;
import org.netbeans.lib.cvsclient.progress.IProgressViewer;
import org.netbeans.lib.cvsclient.progress.RangeProgressViewer;
import org.netbeans.lib.cvsclient.progress.receiving.FileInfoAndMessageResponseProgressHandler;
import org.netbeans.lib.cvsclient.progress.sending.IRequestsProgressHandler;
import org.netbeans.lib.cvsclient.progress.sending.FileStateRequestsProgressHandler;
import org.netbeans.lib.cvsclient.event.IEventSender;
import org.netbeans.lib.cvsclient.event.ICvsListenerRegistry;
import org.netbeans.lib.cvsclient.event.ICvsListener;
import org.netbeans.lib.cvsclient.event.DualListener;
import com.intellij.cvsSupport2.cvsoperations.common.CvsOperationOnFiles;
import com.intellij.cvsSupport2.cvsoperations.common.CvsExecutionEnvironment;
import com.intellij.cvsSupport2.connections.CvsRootProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.FilePath;

public class ExtendedTagOperation extends CvsOperationOnFiles {
  protected String _tag;
  protected boolean _removeTag;
  protected boolean _overrideExisting;
  protected String _tagByRevision = null;

  public ExtendedTagOperation(VirtualFile files[],String tag,boolean removeTag,boolean overrideExisting,String tagByRevision)
  {
    VirtualFile arr[] = files;
    int len = arr.length;
    for(int i = 0; i < len; i++) {
      VirtualFile file = arr[i];
      addFile(file);
    }
    _removeTag = removeTag;
    _tag = tag;
    _overrideExisting = overrideExisting;
    _tagByRevision = tagByRevision;
  }

  public ExtendedTagOperation(FilePath files[],String tag,boolean removeTag,boolean overrideExisting,String tagByRevision)
  {
    FilePath arr[] = files;
    int len = arr.length;
    for(int i = 0; i < len; i++) {
      FilePath file = arr[i];
      addFile(file.getIOFile());
    }
    _removeTag = removeTag;
    _tag = tag;
    _overrideExisting = overrideExisting;
    _tagByRevision = tagByRevision;
  }

  protected Command createCommand(CvsRootProvider root, CvsExecutionEnvironment cvsExecutionEnvironment)
  {
    MyTagCommand tagCommand = new MyTagCommand();
    addFilesToCommand(root,tagCommand);
    tagCommand.setTag(_tag);
    tagCommand.setDeleteTag(_removeTag);
    tagCommand.setOverrideExistingTag(_overrideExisting);
    tagCommand.setTagByRevision(_tagByRevision);
    return tagCommand;
  }

  protected String getOperationName()
  {
    return "tag";
  }
}

class MyTagCommand extends AbstractCommand {
  public static final String EXAM_DIR_TAG = "server: Tagging ";
  public static final String EXAM_DIR_UNTAG = "server: Untagging ";

  protected String _tag = null;
  protected boolean _checkThatUnmodified;
  protected boolean _deleteTag;
  protected boolean _allowMoveDeleteBranchTag;
  protected boolean _makeBranchTag;
  protected boolean _overrideExistingTag;
  protected String _tagByRevision = null;

  public MyTagCommand()
  {
  }

  public boolean execute(IRequestProcessor requestProcessor, IEventSender eventManager, ICvsListenerRegistry listenerRegistry, IClientEnvironment clientEnvironment, IProgressViewer progressViewer) throws CommandException
  {
    Requests requests;
    IRequestsProgressHandler requestsProgressHandler;
    ICvsListener listener;
    ICvsFiles cvsFiles;
    try {
      cvsFiles = scanFileSystem(getFileObjects(),clientEnvironment);
    } catch(IOException ex) {
      throw new IOCommandException(ex);
    }
    requests = new Requests(CommandRequest.TAG,clientEnvironment);
    addFileRequests(cvsFiles,requests,clientEnvironment);
    requests.addArgumentRequest(isDeleteTag(),"-d");
    requests.addArgumentRequest(isMakeBranchTag(),"-b");
    requests.addArgumentRequest(isCheckThatUnmodified(),"-c");
    requests.addArgumentRequest(isOverrideExistingTag(),"-F");
    requests.addArgumentRequest(isAllowMoveDeleteBranchTag(),"-B");
    if((getTagByRevision() != null) && (getTagByRevision().length() > 0))
      requests.addArgumentRequests(getTagByRevision(),"-r");
    requests.addArgumentRequest(true,getTag());
    requests.addLocalPathDirectoryRequest();
    addArgumentRequests(requests);
    requestsProgressHandler = new FileStateRequestsProgressHandler(new RangeProgressViewer(progressViewer,0.0D,0.5D),cvsFiles);
    ICvsListener responseProgressHandler = new FileInfoAndMessageResponseProgressHandler(new RangeProgressViewer(progressViewer,0.5D,1.0D),cvsFiles,isDeleteTag() ? "server: Untagging " : "server: Tagging ");
    ICvsListener tagParser = new DefaultEntryParser(eventManager,clientEnvironment.getCvsFileSystem());
    listener = new DualListener(tagParser,responseProgressHandler);
    listener.registerListeners(listenerRegistry);
    boolean flag = false;
    try {
      flag = requestProcessor.processRequests(requests,requestsProgressHandler);
    } catch (AuthenticationException e) {
      throw new CommandException(e, "Authentication failed");
    } finally {
      listener.unregisterListeners(listenerRegistry);
    }
    return flag;
  }

  public String getCvsCommandLine()
  {
    StringBuffer cvsCommandLine = new StringBuffer("tag ");
    cvsCommandLine.append(getCvsArguments());
    if(getTag() != null) {
      cvsCommandLine.append(getTag());
      cvsCommandLine.append(" ");
    }
    appendFileArguments(cvsCommandLine);
    return cvsCommandLine.toString();
  }

  public void resetCvsCommand()
  {
    super.resetCvsCommand();
    setRecursive(true);
    setCheckThatUnmodified(false);
    setDeleteTag(false);
    setAllowMoveDeleteBranchTag(false);
    setMakeBranchTag(false);
    setOverrideExistingTag(false);
    setTagByRevision(null);
  }

  public String getTag()
  {
    return _tag;
  }

  public void setTag(String tag)
  {
    _tag = tag;
  }

  public String getTagByRevision()
  {
    return _tagByRevision;
  }

  public void setTagByRevision(String tagByRevision)
  {
    _tagByRevision = tagByRevision;
  }

  protected boolean isCheckThatUnmodified()
  {
    return _checkThatUnmodified;
  }

  public void setCheckThatUnmodified(boolean checkThatUnmodified)
  {
    _checkThatUnmodified = checkThatUnmodified;
  }

  protected boolean isDeleteTag()
  {
    return _deleteTag;
  }

  public void setDeleteTag(boolean deleteTag)
  {
    _deleteTag = deleteTag;
  }

  public boolean isAllowMoveDeleteBranchTag()
  {
    return _allowMoveDeleteBranchTag;
  }

  public void setAllowMoveDeleteBranchTag(boolean allowMoveDeleteBranchTag)
  {
    _allowMoveDeleteBranchTag = allowMoveDeleteBranchTag;
  }

  protected boolean isMakeBranchTag()
  {
    return _makeBranchTag;
  }

  public void setMakeBranchTag(boolean makeBranchTag)
  {
    _makeBranchTag = makeBranchTag;
  }

  protected boolean isOverrideExistingTag()
  {
    return _overrideExistingTag;
  }

  public void setOverrideExistingTag(boolean overrideExistingTag)
  {
    _overrideExistingTag = overrideExistingTag;
  }

  protected String getCvsArguments()
  {
    StringBuffer arguments = new StringBuffer();
    if(!isRecursive())
      arguments.append("-l ");
    if(isDeleteTag())
      arguments.append("-d ");
    if(isMakeBranchTag())
      arguments.append("-b ");
    if(isCheckThatUnmodified())
      arguments.append("-c ");
    if(isOverrideExistingTag())
      arguments.append("-F ");
    if(isAllowMoveDeleteBranchTag())
      arguments.append("-B ");
    if((getTagByRevision() != null) && (getTagByRevision().length() > 0)) {
      arguments.append("-r ");
      arguments.append(getTagByRevision());
      arguments.append(" ");
    }
    return arguments.toString();
  }
}
