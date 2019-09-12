/*
 * ====================================================================
 * Copyright (c) 2004-2011 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package examples.repository;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/*
 * This is an example of how to commit several types of changes to a repository:
 *  - a new directory with a file,
 *  - modification to anexisting file,
 *  - copying a directory into a branch,
 *  - deletion of the directory and its entries.
 *
 * Main aspects of performing a commit with the help of ISVNEditor:
 * 0)initialize the library (this is done in setupLibrary() method);
 *
 * 1)create an SVNRepository driver for a particular  repository  location, that
 * will be the root directory for committing - that is all paths that are  being
 * committed will be below that root;
 *
 * 2)provide user's authentication information - name/password, since committing
 * generally requires authentication;
 *
 * 3)"ask" your SVNRepository for a commit editor:
 *
 *     ISVNCommitEditor editor = SVNRepository.getCommitEditor();
 *
 * 4)"edit"  a repository - perform a sequence of edit calls (for
 * example, here you "say" to the server that you have added such-and-such a new
 * directory at such-and-such a path as well as a new file). First of all,
 * ISVNEditor.openRoot()  is called to 'open' the root directory;
 *
 * 5)at last you close the editor with the  ISVNEditor.closeEdit()  method  that
 * fixes your modificaions in the repository finalizing the commit.
 *
 * For  each  commit  a  new  ISVNEditor is required - that is after having been
 * closed the editor can no longer be used!
 *
 * This example can be run for a locally installed Subversion repository via the
 * svn:// protocol. This is how you can do it:
 *
 * 1)after   you   install  the   Subversion  pack (available  for  download  at
 * http://subversion.tigris.org)  you  should  create  a  new  repository in  a
 * directory, like this (in a command line under a Windows OS):
 *
 * >svnadmin create X:\path\to\rep
 *
 * 2)after the repository is created you can add a new account: navigate to your
 * repository root (X:\path\to\rep\),  then  move  to  \conf  and  open the file
 * 'passwd'. In the file you'll see the section [users]. Uncomment it and add  a
 * new account below the section name, like:
 *
 * [users]
 * userName = userPassword.
 *
 * In the program you may further use this account as user's credentials.
 *
 * 3)the next step is to launch the custom Subversion  server  (svnserve)  in  a
 * background mode for the just created repository:
 *
 * >svnserve -d -r X:\path\to
 *
 * That's all. The repository is now available via svn://localhost/rep.
 *
 */
public class Commit2Test implements CreatedFiles{
    private static final String IOTA = "iota";
    private static final String MU = "mu";
    private static final String LAMBDA = "lambda";
    private static final String ALPHA = "alpha";
    private static final String BETA = "beta";
    private static final String GAMMA = "gamma";
    private static final String PI = "pi";
    private static final String RHO = "rho";
    private static final String TAU = "tau";
    private static final String CHI = "chi";
    private static final String OMEGA = "omega";
    private static final String PSI = "psi";

    private static final Map ourGreekTreeFiles = new HashMap();

    static {
        ourGreekTreeFiles.put(IOTA, "This is the file 'iota'.\n");
        ourGreekTreeFiles.put(MU, "This is the file 'A/mu'.\n");
        ourGreekTreeFiles.put(LAMBDA, "This is the file 'A/B/lambda'.\n");
        ourGreekTreeFiles.put(ALPHA, "This is the file 'A/B/E/alpha'.\n");
        ourGreekTreeFiles.put(BETA, "This is the file 'A/B/E/beta'.\n");
        ourGreekTreeFiles.put(GAMMA, "This is the file 'A/D/gamma'.\n");
        ourGreekTreeFiles.put(PI, "This is the file 'A/D/G/pi'.\n");
        ourGreekTreeFiles.put(RHO, "This is the file 'A/D/G/rho'.\n");
        ourGreekTreeFiles.put(TAU, "This is the file 'A/D/G/tau'.\n");
        ourGreekTreeFiles.put(CHI, "This is the file 'A/D/H/chi'.\n");
        ourGreekTreeFiles.put(OMEGA, "This is the file 'A/D/H/omega'.\n");
        ourGreekTreeFiles.put(PSI, "This is the file 'A/D/H/psi'.\n");
    }

    private final File file;

    public Commit2Test(File f) {
        file = f;
    }

    public static void main(String[] args) {

        setupLibrary();
        try {
            commitExample();
        } catch (SVNException e) {
            SVNErrorMessage err = e.getErrorMessage();
            while(err != null) {
                System.err.println(err.getErrorCode().getCode() + " : " + err.getMessage());
                err = err.getChildErrorMessage();
            }
            System.exit(1);
        }
        System.exit(0);
    }

    private static void commitExample() throws SVNException {

        SVNURL url = SVNURL.parseURIEncoded("http://DESKTOP-QOVLI62/svn/testRepository");
        String userName = "saradorociak";
        String userPassword = "Przyjaciele95";

        byte[] contents = "This is a new file".getBytes();
        byte[] modifiedContents = "This is the same file but modified a little.".getBytes();

        SVNRepository repository = SVNRepositoryFactory.create(url);
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPassword);
        repository.setAuthenticationManager(authManager);

        SVNNodeKind nodeKind = repository.checkPath("/", -1);

        if (nodeKind == SVNNodeKind.NONE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "No entry at URL ''{0}''", url);
            throw new SVNException(err);
        } else if (nodeKind == SVNNodeKind.FILE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "Entry at URL ''{0}'' is a file while directory was expected", url);
            throw new SVNException(err);
        }

        long latestRevision = repository.getLatestRevision();
        System.out.println("Repository latest revision (before committing): " + latestRevision);

        ISVNEditor commitEditor = repository.getCommitEditor("initializing the repository with a greek tree",
                null, false, null, null);

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();

        commitEditor.openRoot(SVNRepository.INVALID_REVISION);

        //add /iota file
        commitEditor.addFile(IOTA, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(IOTA, null);
        String fileText = (String) ourGreekTreeFiles.get(IOTA);
        String checksum = deltaGenerator.sendDelta(IOTA, new ByteArrayInputStream(fileText.getBytes()),
                commitEditor, true);
        commitEditor.closeFile(IOTA, checksum);

        //add /A directory
        commitEditor.addDir("A", null, SVNRepository.INVALID_REVISION);

        //add /A/mu file
        String muPath = "A/" + MU;
        commitEditor.addFile(muPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(muPath, null);
        fileText = (String) ourGreekTreeFiles.get(MU);
        checksum = deltaGenerator.sendDelta(muPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        commitEditor.closeFile(muPath, checksum);

        commitEditor.addDir("A/B", null, SVNRepository.INVALID_REVISION);

        //add /A/B/lambda file
        String lambdaPath = "A/B/" + LAMBDA;
        commitEditor.addFile(lambdaPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(lambdaPath, null);
        fileText = (String) ourGreekTreeFiles.get(LAMBDA);
        checksum = deltaGenerator.sendDelta(lambdaPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        commitEditor.closeFile(lambdaPath, checksum);

        commitEditor.addDir("A/B/E", null, SVNRepository.INVALID_REVISION);

        //add /A/B/E/alpha file
        String alphaPath = "A/B/E/" + ALPHA;
        commitEditor.addFile(alphaPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(alphaPath, null);
        fileText = (String) ourGreekTreeFiles.get(ALPHA);
        checksum = deltaGenerator.sendDelta(alphaPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        commitEditor.closeFile(alphaPath, checksum);

        //add /A/B/E/beta file
        String betaPath = "A/B/E/" + BETA;
        commitEditor.addFile(betaPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(betaPath, null);
        fileText = (String) ourGreekTreeFiles.get(BETA);
        checksum = deltaGenerator.sendDelta(betaPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        commitEditor.closeFile(betaPath, checksum);

        //close /A/B/E
        commitEditor.closeDir();

        //add /A/B/F
        commitEditor.addDir("A/B/F", null, SVNRepository.INVALID_REVISION);

        //close /A/B/F
        commitEditor.closeDir();

        //close /A/B
        commitEditor.closeDir();

        //add /A/C
        commitEditor.addDir("A/C", null, SVNRepository.INVALID_REVISION);

        //close /A/C
        commitEditor.closeDir();

        //add /A/D
        commitEditor.addDir("A/D", null, SVNRepository.INVALID_REVISION);

        //add /A/D/gamma file
        String gammaPath = "A/D/" + GAMMA;
        commitEditor.addFile(gammaPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(gammaPath, null);
        fileText = (String) ourGreekTreeFiles.get(GAMMA);
        checksum = deltaGenerator.sendDelta(gammaPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        commitEditor.closeFile(gammaPath, checksum);

        //add /A/D/G
        commitEditor.addDir("A/D/G", null, SVNRepository.INVALID_REVISION);

        //add /A/D/G/pi file
        String piPath = "A/D/G/" + PI;
        commitEditor.addFile(piPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(piPath, null);
        fileText = (String) ourGreekTreeFiles.get(PI);
        checksum = deltaGenerator.sendDelta(piPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        //close /A/D/G/pi file
        commitEditor.closeFile(piPath, checksum);

        //add /A/D/G/rho file
        String rhoPath = "A/D/G/" + RHO;
        commitEditor.addFile(rhoPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(rhoPath, null);
        fileText = (String) ourGreekTreeFiles.get(RHO);
        checksum = deltaGenerator.sendDelta(rhoPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        //close /A/D/G/rho file
        commitEditor.closeFile(rhoPath, checksum);

        //add /A/D/G/tau file
        String tauPath = "A/D/G/" + TAU;
        commitEditor.addFile(tauPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(tauPath, null);
        fileText = (String) ourGreekTreeFiles.get(TAU);
        checksum = deltaGenerator.sendDelta(tauPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        //close /A/D/G/tau file
        commitEditor.closeFile(tauPath, checksum);

        //close /A/D/G
        commitEditor.closeDir();

        //add /A/D/H
        commitEditor.addDir("A/D/H", null, SVNRepository.INVALID_REVISION);

        //add /A/D/H/chi file
        String chiPath = "A/D/H/" + CHI;
        commitEditor.addFile(chiPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(chiPath, null);
        fileText = (String) ourGreekTreeFiles.get(CHI);
        checksum = deltaGenerator.sendDelta(chiPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        //close /A/D/H/chi file
        commitEditor.closeFile(chiPath, checksum);

        //add /A/D/H/omega file
        String omegaPath = "A/D/H/" + OMEGA;
        commitEditor.addFile(omegaPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(omegaPath, null);
        fileText = (String) ourGreekTreeFiles.get(OMEGA);
        checksum = deltaGenerator.sendDelta(omegaPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        //close /A/D/H/omega file
        commitEditor.closeFile(omegaPath, checksum);

        //add /A/D/H/psi file
        String psiPath = "A/D/H/" + PSI;
        commitEditor.addFile(psiPath, null, SVNRepository.INVALID_REVISION);
        commitEditor.applyTextDelta(psiPath, null);
        fileText = (String) ourGreekTreeFiles.get(PSI);
        checksum = deltaGenerator.sendDelta(psiPath, new ByteArrayInputStream(fileText.getBytes()), commitEditor,
                true);
        //close /A/D/H/psi file
        commitEditor.closeFile(psiPath, checksum);

        //close /A/D/H
        commitEditor.closeDir();

        //close /A/D
        commitEditor.closeDir();

        //close /A
        commitEditor.closeDir();
        //close the root of the edit - / (repository root) in our case
        commitEditor.closeDir();

        commitEditor.closeEdit();


    }

    private static long calculateSize(Path pathCopiedDirectoryForRepo) {
        try {
            if (Files.isRegularFile(pathCopiedDirectoryForRepo)) {
                return Files.size(pathCopiedDirectoryForRepo);
            }

            return Files.list(pathCopiedDirectoryForRepo).mapToLong(Commit2Test::calculateSize).sum();
        } catch (IOException e) {
            return 0L;
        }
    }


    private static SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath,
                                            String filePath, byte[] oldData, byte[] newData) throws SVNException {
        /*
         * Always called first. Opens the current root directory. It  means  all
         * modifications will be applied to this directory until  a  next  entry
         * (located inside the root) is opened/added.
         *
         * -1 - revision is HEAD
         */
        editor.openRoot(-1);
        /*
         * Opens a next subdirectory (in this example program it's the directory
         * added  in  the  last  commit).  Since this moment all changes will be
         * applied to this directory.
         *
         * dirPath is relative to the root directory.
         * -1 - revision is HEAD
         */
        editor.openDir(dirPath, -1);
        /*
         * Opens the file added in the previous commit.
         *
         * filePath is also defined as a relative path to the root directory.
         */
        editor.openFile(filePath, -1);

        /*
         * The next steps are directed to applying and writing the file delta.
         */
        editor.applyTextDelta(filePath, null);

        /*
         * Use delta generator utility class to generate and send delta
         *
         * Note that you may use only 'target' data to generate delta when there is no
         * access to the 'base' (previous) version of the file. However, here we've got 'base'
         * data, what in case of larger files results in smaller network overhead.
         *
         * SVNDeltaGenerator will call editor.textDeltaChunk(...) method for each generated
         * "diff window" and then editor.textDeltaEnd(...) in the end of delta transmission.
         * Number of diff windows depends on the file size.
         *
         */
        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);

        /*
         * Closes the file.
         */
        editor.closeFile(filePath, checksum);

        /*
         * Closes the directory.
         */
        editor.closeDir();

        /*
         * Closes the root directory.
         */
        editor.closeDir();

        /*
         * This is the final point in all editor handling. Only now all that new
         * information previously described with the editor's methods is sent to
         * the server for committing. As a result the server sends the new
         * commit information.
         */
        return editor.closeEdit();
    }

    /*
     * This method performs committing a deletion of a directory.
     */
    private static SVNCommitInfo deleteDir(ISVNEditor editor, String dirPath) throws SVNException {
        /*
         * Always called first. Opens the current root directory. It  means  all
         * modifications will be applied to this directory until  a  next  entry
         * (located inside the root) is opened/added.
         *
         * -1 - revision is HEAD
         */
        editor.openRoot(-1);
        /*
         * Deletes the subdirectory with all its contents.
         *
         * dirPath is relative to the root directory.
         */
        editor.deleteEntry(dirPath, -1);
        /*
         * Closes the root directory.
         */
        editor.closeDir();
        /*
         * This is the final point in all editor handling. Only now all that new
         * information previously described with the editor's methods is sent to
         * the server for committing. As a result the server sends the new
         * commit information.
         */
        return editor.closeEdit();
    }

    /*
     * This  method  performs how a directory in the repository can be copied to
     * branch.
     */
    private static SVNCommitInfo copyDir(ISVNEditor editor, String srcDirPath,
                                         String dstDirPath, long revision) throws SVNException {
        /*
         * Always called first. Opens the current root directory. It  means  all
         * modifications will be applied to this directory until  a  next  entry
         * (located inside the root) is opened/added.
         *
         * -1 - revision is HEAD
         */
        editor.openRoot(-1);

        /*
         * Adds a new directory that is a copy of the existing one.
         *
         * srcDirPath   -  the  source  directory  path (relative  to  the  root
         * directory).
         *
         * dstDirPath - the destination directory path where the source will be
         * copied to (relative to the root directory).
         *
         * revision    - the number of the source directory revision.
         */
        editor.addDir(dstDirPath, srcDirPath, revision);
        /*
         * Closes the just added copy of the directory.
         */
        editor.closeDir();
        /*
         * Closes the root directory.
         */
        editor.closeDir();
        /*
         * This is the final point in all editor handling. Only now all that new
         * information previously described with the editor's methods is sent to
         * the server for committing. As a result the server sends the new
         * commit information.
         */
        return editor.closeEdit();
    }

    /*
     * Initializes the library to work with a repository via
     * different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }



    @Override
    public void createFiles() {
        System.out.println("CreateFiles path " + file);
        try (FileOutputStream out = new FileOutputStream(file)) {

            int nbDesiredBytes = 9999;
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            Random r = new Random();

            int nbBytes = 0;
            while (nbBytes < nbDesiredBytes) {
                int nbBytesToWrite = Math.min(nbDesiredBytes - nbBytes, bufferSize);
                byte[] bytes = new byte[nbBytesToWrite];
                r.nextBytes(bytes);
                out.write(bytes);
                nbBytes += nbBytesToWrite;
            }

            System.out.println(" created binary file " + out.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}