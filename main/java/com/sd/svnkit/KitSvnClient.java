package com.sd.svnkit;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.net.URI;
import java.util.Collections;

public class KitSvnClient implements SvnClient {
    private final SVNClientManager svnClientManager;

    private KitSvnClient(SVNClientManager svnClientManager) {
        this.svnClientManager = svnClientManager;
    }

    public static KitSvnClient create() {
        return new KitSvnClient(SVNClientManager.newInstance());
    }

    @Override
    public void checkoutRepository(URI uri, File localDirectory) {
        try {
            SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
            updateClient.doCheckout(
                    SVNURL.parseURIEncoded(uri.toString()),
                    localDirectory,
                    SVNRevision.UNDEFINED,
                    SVNRevision.HEAD,
                    SVNDepth.INFINITY,
                    false);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit(String commitName, File... files) {
        try {
            SVNCommitClient commitClient = svnClientManager.getCommitClient();
            commitClient.doCommit(files, false, commitName, null, null, false, false, SVNDepth.INFINITY);
        } catch (SVNException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void merge(URI sourceBranch, File targetLocalBranch) {
        try {
            SVNDiffClient diffClient = svnClientManager.getDiffClient();
            SVNRevisionRange rangeToMerge = new SVNRevisionRange(SVNRevision.create(1), SVNRevision.HEAD);
            diffClient.doMerge(SVNURL.parseURIEncoded(sourceBranch.toString()), SVNRevision.HEAD, Collections.singleton(rangeToMerge), targetLocalBranch, SVNDepth.INFINITY, true, false, false, false);
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }

    }

}
