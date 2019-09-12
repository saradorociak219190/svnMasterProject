package com.sd.svnkit;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class Application {
    private int commitNumber = 1;

    public static void main(String[] args) throws SVNException {
        new Application().run();
    }

    public void run() throws SVNException {
        RandomBytesProvider randomBytesProvider = new RandomBytesProvider();

        String repositoryUri = "http://DESKTOP-QOVLI62/svn/testRepository";
        SvnKitManager svnKitManager = SvnKitManager.connect(URI.create(repositoryUri), "saradorociak", "Przyjaciele95");
        FilesGenerator filesGenerator = new FilesGenerator(svnKitManager, randomBytesProvider);

        svnKitManager.commitEmptyFolder("trunk");
        filesGenerator.generateRandomFiles("trunk", "original", 5, 100);
        svnKitManager.copyDir("trunk", "feature");
        System.out.println("copy in branch");
        filesGenerator.generateRandomFiles("feature", "specific", 5, 100);
        System.out.println("DONE");

        LocalFileModifier localFileModifier = new LocalFileModifier(randomBytesProvider);


        ensureEmptyDirectories("/trunk", "/feature");

        KitSvnClient kitSvnClient = KitSvnClient.create();
        kitSvnClient.checkoutRepository(URI.create(repositoryUri + "/feature"), file("/feature"));
        File fileToBeModified = file("/feature/originalfile_0.data");
        localFileModifier.modify(fileToBeModified, 0.5);
        kitSvnClient.commit("Modified file", file("/feature"));

        kitSvnClient.checkoutRepository(URI.create(repositoryUri + "/trunk"), file("/trunk"));
        kitSvnClient.merge(URI.create(repositoryUri + "/feature"), file("/trunk"));
        kitSvnClient.commit("edi", file("/trunk"));
    }

    private void ensureEmptyDirectories(String... paths) {
        String classPath = file("/").getPath();
        ;
        Arrays.stream(paths).map(p -> new File(classPath + p)).peek(System.out::println).forEach(d -> {
            try {
                FileUtils.cleanDirectory(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Arrays.stream(paths).map(p -> new File(classPath + p)).forEach(File::mkdirs);
    }

    private File file(String path) {
        return new File(getClass().getResource(path).getFile());
    }

//        //working copy
//        File export = new File("export");
//        if (export.exists()) {
//            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "Path ''{0}'' already exists", export);
//            try {
//                throw new SVNException(err);
//            } catch (SVNException e) {
//                e.printStackTrace();
//            }
//        }
//        export.mkdirs();
//
//        SVNRepository repository = svnKitManager.getSvnRepository();
//        long latestRevision = 0;
//        try {
//            latestRevision = repository.getLatestRevision();
//        } catch (SVNException e) {
//            e.printStackTrace();
//        }
//        ISVNReporterBaton reporterBaton = new Export.ExportReporterBaton(latestRevision);
//        ISVNEditor exportEditor = new Export.ExportEditor(export);
//        try {
//            repository.getLatestRevision();
//            repository.update(latestRevision, null, true, reporterBaton, exportEditor);
//            System.out.println("Exported revision: " + latestRevision);
//
//        } catch (SVNException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            File reposRoot = new File("reposRoot", "exampleRepository");
//            SamplesUtility.createRepository(reposRoot);
////            SVNCommitInfo info = examples.wc.SamplesUtility.createRepositoryTree(reposRoot);
//            //print out new revision info
//            //System.out.println(info);
//            SVNClientManager clientManager = SVNClientManager.newInstance();
//            SVNCopyClient copyClient = clientManager.getCopyClient();
//            SVNURL reposURL = SVNURL.fromFile(export);
//            SVNURL Folder_1_URL = reposURL.appendPath("Folder_1", true);
//            SVNURL copyTargetURL = reposURL.appendPath("Folder_2_copy", true); //adres lokalnego folderu
//
//            //switch to branch
//
////            try {
////              //  svnKitManager.switchToBranch(Folder_1_URL,working_copy,SVNRevision.HEAD,true);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//            File wcBranch = new File("wcBranch", "private");
//           examples.wc.SamplesUtility.checkOutWorkingCopy(copyTargetURL, wcBranch);
//
//
//
//           try {
//                SamplesUtility.writeToFile(new File(wcBranch,"originalfile_0.data"),"some change",true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println("changed file in working copy branch");
//            SVNCommitClient commitClient = clientManager.getCommitClient();
//            commitClient.doCommit(new File[] {wcRoot }, false, "committing changes", null, null, false, false, SVNDepth.INFINITY);
//
//            RandomBytesProvider randomBytesProvider = null;
//            // svnKitManager.commitFile("changeed filed",working_copy.getAbsolutePath(),randomBytesProvider.getRandomBytes(2));
//
//            commitClient = clientManager.getCommitClient();
//           // commitClient.doCommit(new File[] { working_copy }, false, "committing changes", null, null, false,
//              //      false, SVNDepth.INFINITY);
//            //  svnKitManager.commitFile("changes from trunk's working copy ",wcRoot,);
//            SVNDiffClient diffClient = clientManager.getDiffClient();
//            SVNRevisionRange rangeToMerge = new SVNRevisionRange(SVNRevision.create(1), SVNRevision.HEAD);
//            //diffClient.doMerge(Folder_1_URL, SVNRevision.HEAD, Collections.singleton(rangeToMerge),new File(working_copy, "A_copy"), SVNDepth.INFINITY, true, false, false, false);
//
//
//
//
//        } catch (SVNException e) {
//            e.printStackTrace();
//        }
//

}

