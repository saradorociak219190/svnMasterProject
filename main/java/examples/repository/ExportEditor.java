package examples.repository;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ExportEditor implements ISVNEditor {
    private File myRootDirectory;
    private SVNDeltaProcessor myDeltaProcessor;
    public ExportEditor(File root) {
        myRootDirectory = root;
        /*
         * Utility class that will help us to transform 'deltas' sent by the
         * server to the new file contents.
         */
        myDeltaProcessor = new SVNDeltaProcessor();
    }

    /*
     * Server reports revision to which application of the further
     * instructions will update working copy to.
     */
    public void targetRevision(long revision) throws SVNException {
    }

    /*
     * Called before sending other instructions.
     */
    public void openRoot(long revision) throws SVNException {
    }

    /*
     * Called when a new directory has to be added.
     *
     * For each 'addDir' call server will call 'closeDir' method after
     * all children of the added directory are added.
     *
     * This implementation creates corresponding directory below root directory.
     */
    public void addDir(String path, String copyFromPath, long copyFromRevision) throws SVNException {
        File newDir = new File(myRootDirectory, path);
        if (!newDir.exists()) {
            if (!newDir.mkdirs()) {
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: failed to add the directory ''{0}''.", newDir);
                throw new SVNException(err);
            }
        }
        System.out.println("dir added: " + path);

    }



    /*
     * Called when there is an existing directory that has to be 'opened' either
     * to modify this directory properties or to process other files and directories
     * inside this directory.
     *
     * In case of export this method will never be called because we reported
     * that our 'working copy' is empty and so server knows that there are
     * no 'existing' directories.
     */
    public void openDir(String path, long revision) throws SVNException {
    }

    /*
     * Instructs to change opened or added directory property.
     *
     * This method is called to update properties set by the user as well
     * as those created automatically, like "svn:committed-rev".
     * See SVNProperty class for default property names.
     *
     * When property has to be deleted value will be 'null'.
     */

    public void changeDirProperty(String name, SVNPropertyValue property) throws SVNException {
    }

    /*
     * Called when a new file has to be created.
     *
     * For each 'addFile' call server will call 'closeFile' method after
     * sending file properties and contents.
     *
     * This implementation creates empty file below root directory, file contents
     * will be updated later, and for empty files may not be sent at all.
     */
    public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
        File file = new File(myRootDirectory, path);
        if (file.exists()) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: exported file ''{0}'' already exists!", file);
            throw new SVNException(err);
        }
        try {
            file.createNewFile();
            for(int i=0;i<3;i++)
            {
                CreatedFiles createdFiles = new Export(
                        new File(myRootDirectory + "/OTHERfile1.txt" + i));
                createdFiles.createFiles();
            }


        } catch (IOException e) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: cannot create new  file ''{0}''", file);
            throw new SVNException(err);
        }
    }

    /*
     * Called when there is an existing files that has to be 'opened' either
     * to modify file contents or properties.
     *
     * In case of export this method will never be called because we reported
     * that our 'working copy' is empty and so server knows that there are
     * no 'existing' files.
     */
    public void openFile(String path, long revision) throws SVNException {
    }

    /*
     * Instructs to add, modify or delete file property.
     * In this example we skip this instruction, but 'real' export operation
     * may inspect 'svn:eol-style' or 'svn:mime-type' property values to
     * transfor file contents propertly after receiving.
     */

    public void changeFileProperty(String path, String name, SVNPropertyValue property) throws SVNException {
    }

    /*
     * Called before sending 'delta' for a file. Delta may include instructions
     * on how to create a file or how to modify existing file. In this example
     * delta will always contain instructions on how to create a new file and so
     * we set up deltaProcessor with 'null' base file and target file to which we would
     * like to store the result of delta application.
     */
    public void applyTextDelta(String path, String baseChecksum) throws SVNException {
        myDeltaProcessor.applyTextDelta((File) null, new File(myRootDirectory, path), false);
    }

    /*
     * Server sends deltas in form of 'diff windows'. Depending on the file size
     * there may be several diff windows. Utility class SVNDeltaProcessor processes
     * these windows for us.
     */
    public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow)   throws SVNException {
        return myDeltaProcessor.textDeltaChunk(diffWindow);
    }

    /*
     * Called when all diff windows (delta) is transferred.
     */
    public void textDeltaEnd(String path) throws SVNException {
        myDeltaProcessor.textDeltaEnd();
    }

    /*
     * Called when file update is completed.
     * This call always matches addFile or openFile call.
     */
    public void closeFile(String path, String textChecksum) throws SVNException {
        System.out.println("file added: " + path);
    }

    /*
     * Called when all child files and directories are processed.
     * This call always matches addDir, openDir or openRoot call.
     */
    public void closeDir() throws SVNException {
    }

    /*
     * Insturcts to delete an entry in the 'working copy'. Of course will not be
     * called during export operation.
     */
    public void deleteEntry(String path, long revision) throws SVNException {
    }

    /*
     * Called when directory at 'path' should be somehow processed,
     * but authenticated user (or anonymous user) doesn't have enough
     * access rights to get information on this directory (properties, children).
     */
    public void absentDir(String path) throws SVNException {
    }

    /*
     * Called when file at 'path' should be somehow processed,
     * but authenticated user (or anonymous user) doesn't have enough
     * access rights to get information on this file (contents, properties).
     */
    public void absentFile(String path) throws SVNException {
    }

    /*
     * Called when update is completed.
     */
    public SVNCommitInfo closeEdit() throws SVNException {
        return null;
    }

    /*
     * Called when update is completed with an error or server
     * requests client to abort update operation.
     */
    public void abortEdit() throws SVNException {
    }
}
