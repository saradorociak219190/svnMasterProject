package com.sd.svnkit;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.*;

public class ConflictResolverHandler implements ISVNConflictHandler {
    public SVNConflictResult handleConflict(SVNConflictDescription conflictDescription) throws SVNException {
        SVNConflictReason reason = conflictDescription.getConflictReason();
        SVNMergeFileSet mergeFiles = conflictDescription.getMergeFiles();

        SVNConflictChoice choice = SVNConflictChoice.THEIRS_FULL;
        if (reason == SVNConflictReason.EDITED) {
            //If the reason why conflict occurred is local edits, chose local version of the file
            //Otherwise the repository version of the file will be chosen.
            choice = SVNConflictChoice.MINE_FULL;
        }
        System.out.println("Automatically resolving conflict for " + mergeFiles.getWCFile() +
                ", choosing " + (choice == SVNConflictChoice.MINE_FULL ? "local file" : "repository file"));
        return new SVNConflictResult(choice, mergeFiles.getResultFile());
    }

}

