package com.sd.svnkit;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

public class SvnKitManager implements SvnManager {
    private final SVNRepository svnRepository;

    private SvnKitManager(SVNRepository svnRepository) {
        this.svnRepository = svnRepository;
    }

    public SVNRepository getSvnRepository() {
        return svnRepository;
    }

    public static SvnKitManager connect(URI svnUri, String userName, String password) {
        try {
            SVNURL url = SVNURL.parseURIEncoded(svnUri.toString());
            SVNRepository repository = SVNRepositoryFactory.create(url);

            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
            repository.setAuthenticationManager(authManager);
            return new SvnKitManager(repository);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void commitEmptyFolder(String folder) {
        try {
            ISVNEditor commitEditor = svnRepository.getCommitEditor("Create folder " + folder, null, false, null, null);
            commitEditor.openRoot(SVNRepository.INVALID_REVISION);
            commitEditor.addDir(folder, null, SVNRepository.INVALID_REVISION);
            commitEditor.closeEdit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void commitFile(String commitName, Path filePath, InputStream content) {
        try {
            String parent = filePath.getParent().toString().replaceAll("\\\\", "/");
            String fileName = filePath.getFileName().toString();
            ISVNEditor commitEditor = svnRepository.getCommitEditor(commitName, null, false, null, null);
            commitEditor.openRoot(SVNRepository.INVALID_REVISION);
            commitEditor.openDir(parent, SVNRepository.INVALID_REVISION);
            commitEditor.addFile(fileName, null, SVNRepository.INVALID_REVISION);
            commitEditor.applyTextDelta(fileName, null);
            SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
            String checksum = deltaGenerator.sendDelta(fileName, content, commitEditor, true);
            commitEditor.closeFile(fileName, checksum);
            commitEditor.closeDir();
            commitEditor.closeEdit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void copyDir(String sourceDir, String targetDir) {
        try {
            ISVNEditor editor = svnRepository.getCommitEditor("directory copied: " + sourceDir + " -> " + targetDir, null);
            SVNCommitInfo commitInfo = copyDir(editor, sourceDir, targetDir, SVNRepository.INVALID_REVISION);//kopiuje folder do repo na serwerze
            System.out.println("The directory was copied: " + commitInfo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private SVNCommitInfo copyDir(ISVNEditor editor,
                                  String srcDirPath,
                                  String dstDirPath,
                                  long revision) throws SVNException {

        editor.openRoot(-1);

        editor.addDir(dstDirPath, srcDirPath, revision);
        /*
         * Closes the just added copy of the directory.
         */
        editor.closeDir();
        /*
         * Closes the root directory.
         */
        editor.closeDir();

        return editor.closeEdit();
    }

    private static long switchToURL(File wcPath, SVNURL url, SVNRevision updateToRevision, boolean isRecursive) throws SVNException {
        final SVNUpdateClient updateClient = SVNClientManager
                .newInstance().getUpdateClient();

        updateClient.setIgnoreExternals(false);

        return updateClient.doSwitch(wcPath, url, updateToRevision, isRecursive);
    }

    public void switchToBranch(SVNURL branchUrl,
                               final File basedir,
                               SVNRevision updateToRevision, boolean isRecursive) throws SVNException, IOException {
        final SVNUpdateClient updateClient =
                SVNClientManager
                        .newInstance().getUpdateClient();

        updateClient.doSwitch(
                basedir, branchUrl,
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                SVNDepth.INFINITY,
                false,
                false);
    }

    public long doSwitch(File path, SVNURL url, SVNRevision pegRevision, SVNRevision revision, SVNDepth depth, boolean allowUnversionedObstructions, boolean depthIsSticky) throws SVNException {
        return doSwitch(path, url, pegRevision, revision, depth, allowUnversionedObstructions, true);
    }


}
