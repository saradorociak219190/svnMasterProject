//package examples.repository;
//
///*
// * ====================================================================
// * Copyright (c) 2004-2011 TMate Software Ltd.  All rights reserved.
// *
// * This software is licensed as described in the file COPYING, which
// * you should have received as part of this distribution.  The terms
// * are also available at http://svnkit.com/license.html
// * If newer versions of this license are posted there, you may use a
// * newer version instead, at your option.
// * ====================================================================
// */
//
//
//import org.tmatesoft.svn.core.*;
//import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
//import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
//import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
//import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
//import org.tmatesoft.svn.core.io.*;
//import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
//import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
//import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
//import org.tmatesoft.svn.core.wc.*;
//import org.tmatesoft.svn.core.wc2.SvnCheckout;
//import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
//import org.tmatesoft.svn.core.wc2.SvnTarget;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Random;
//
//public class TestClass implements CreatedFiles {
//
//    private final File file;
//    private SvnCheckout checkout;
//
//    public TestClass(File f) {
//        file = f;
//    }
//
//    public static void main(String[] args) {
//
//        setupLibrary();
//
//        try {
//            commitExample();
//        } catch (SVNException e) {
//            SVNErrorMessage err = e.getErrorMessage();
//            while (err != null) {
//                System.err.println(err.getErrorCode().getCode() + " : " + err.getMessage());
//                err = err.getChildErrorMessage();
//            }
//            System.exit(1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.exit(0);
//    }
//
//    private static void commitExample() throws SVNException, IOException {
//
//        SVNURL url = SVNURL.parseURIEncoded("http://DESKTOP-QOVLI62/svn/testRepository");
//        String userName = "saradorociak";
//        String userPassword = "Przyjaciele95";
//
//        byte[] contents = "This is a new file".getBytes();
//        byte[] modifiedContents = "This is the same file but modified a little.".getBytes();
//
//        SamplesUtility.initializeFSFSprotocol();
//        File baseDirectory = new File("baseDirectory");
//                File reposRoot = new File(baseDirectory, "exampleRepository");
//               File wcRoot = new File(baseDirectory, "exampleWC");
//
//        SamplesUtility.createRepository(reposRoot);
//        SVNURL reposURL = url;
//
//
//        ISVNEditor commitEditor = repository.getCommitEditor("initializing the repository with a tree",
//                null, false, null, null);
//        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
//
//        commitEditor.openRoot(SVNRepository.INVALID_REVISION);
//
//        ISVNEditor commitEditor.addFile("someFile", null, SVNRepository.INVALID_REVISION);
//        commitEditor.applyTextDelta("someFile", null);
//        byte[] iotaFileText = "someFile".getBytes();
//        String checksum = deltaGenerator.sendDelta("someFile", new ByteArrayInputStream(iotaFileText), commitEditor,
//                true);
//        commitEditor.closeFile("someFile", checksum);
//
//        commitEditor.addDir("A", null, SVNRepository.INVALID_REVISION);
//        String bPath = "A/" + "bFile";
//        commitEditor.addFile(bPath, null, SVNRepository.INVALID_REVISION);
//        commitEditor.applyTextDelta(bPath, null);
//        byte[] bFileText = "bFile".getBytes();
//        checksum = deltaGenerator.sendDelta(bPath, new ByteArrayInputStream(bFileText), commitEditor,
//                true);
//        commitEditor.closeFile(bPath, checksum);
//
//        System.out.println("added A dir");
//        String aPath = "A/" + "aFile.txt";
//        commitEditor.addFile(aPath, null, SVNRepository.INVALID_REVISION);
//        commitEditor.applyTextDelta(aPath, null);
//        byte[] aFileText = "aFile.txt".getBytes();
//        checksum = deltaGenerator.sendDelta(aPath, new ByteArrayInputStream(aFileText), commitEditor,
//                true);
//        //close /A/aFile
//        commitEditor.closeFile(aPath, checksum);
//
//        String cPath = "A/" + "cFile";
//        commitEditor.addFile(cPath, null, SVNRepository.INVALID_REVISION);
//        commitEditor.applyTextDelta(cPath, null);
//        byte[] cFileText = "cFile".getBytes();
//        checksum = deltaGenerator.sendDelta(cPath, new ByteArrayInputStream(cFileText), commitEditor,
//                true);
//        //close /A/c file
//        commitEditor.closeFile(cPath, checksum);
//
//
////        for(int i=0;i<3;i++)
////        {
////            CreatedFiles createdFiles = new CommitTest(
////                    new File("A"+"/" +"someFile" + i));
////            createdFiles.createFiles();
////            String BigfilesPath = "A" + "/" +"someFile"+i;
////            commitEditor.addFile(BigfilesPath, null, SVNRepository.INVALID_REVISION);
////            commitEditor.applyTextDelta(BigfilesPath, null);
////            byte[] BigfilesText = ("someFile"+i).getBytes();
////            checksum = deltaGenerator.sendDelta(BigfilesPath, new ByteArrayInputStream(BigfilesText), commitEditor,
////                    true);
////
////            commitEditor.closeFile(BigfilesPath, checksum);
////
////        }
//
//        commitEditor.closeDir(); //close A directory
//        commitEditor.closeDir();
//        commitEditor.closeEdit();
//
//        //copy repo -> branch
//
//        String absoluteSrcPath = repository.getRepositoryPath("A"); //adres dodanego katalogu
//        long srcRevision = repository.getLatestRevision();
//
//        ISVNEditor editor = repository.getCommitEditor("directory copied", null);
//        SVNCommitInfo commitInfo = copyDir(editor, absoluteSrcPath, "copiedRepoA", srcRevision);//kopiuje folder do repo na serwerze
//        System.out.println("The directory was copied: " + commitInfo);
//
//        Path pathCopiedDirectoryForRepo = Paths.get("copiedRepoA");
//        long size = calculateSize(pathCopiedDirectoryForRepo);
//        System.out.println("copied repos directory location: " + pathCopiedDirectoryForRepo.toAbsolutePath() + " ; size:" + size);
//
//
//        //export all repo to local repository
//
//        File exportDir = new File("export");
//        if (exportDir.exists()) {
//            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "Path ''{0}'' already exists", exportDir);
//            throw new SVNException(err);
//        }
//        exportDir.mkdirs();
//
//        long latestRevision = repository.getLatestRevision();
//        ISVNReporterBaton reporterBaton = new ExportReporterBaton(latestRevision);
//        ISVNEditor exportEditor = new ExportEditor(exportDir);
//        repository.update(latestRevision, null, true, reporterBaton, exportEditor);
//
//        System.out.println("Exported revision: " + latestRevision);
//
//
//        // File baseDirectory = new File("some baseDirectory");
//
//        // File wcRoot = new File(baseDirectory, "exampleWC");
//
//        SVNURL exportURL = SVNURL.fromFile(exportDir);
//        //SamplesUtility.checkOutWorkingCopy(exportURL, wcRoot);
//
//
//        //checkout
//
//        final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
//        try {
//            final SvnCheckout checkout = svnOperationFactory.createCheckout();
//            checkout.setSingleTarget(SvnTarget.fromFile(exportDir));
//            checkout.setSource(SvnTarget.fromURL(exportURL));
//            checkout.run();
//        } finally {
//            svnOperationFactory.dispose();
//
//        }
//        commitEditor.addFile("someFile2", null, SVNRepository.INVALID_REVISION);
//        commitEditor.applyTextDelta("someFile2", null);
//        byte[] someFile2 = "someFile2".getBytes();
//        checksum = deltaGenerator.sendDelta("someFile2", new ByteArrayInputStream(someFile2), commitEditor,
//                true);
//        commitEditor.closeFile("someFile2", checksum);
//
//
//
//        //merge
//
//
//    }
//
//    private static SVNCommitInfo addDir(ISVNEditor editor, String dirPath,
//                                        String filePath, byte[] data) throws SVNException {
//        editor.openRoot(-1);
//        editor.addDir(dirPath, null, -1);
//        editor.addFile(filePath, null, -1);
//        editor.applyTextDelta(filePath, null);
//        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
//        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(data), editor, true);
//
//        editor.closeFile(filePath, checksum);
//        editor.closeDir();
//        editor.closeDir();
//        return editor.closeEdit();
//    }
//
//    private static long calculateSize(Path pathCopiedDirectoryForRepo) {
//        try {
//            if (Files.isRegularFile(pathCopiedDirectoryForRepo)) {
//                return Files.size(pathCopiedDirectoryForRepo);
//            }
//
//            return Files.list(pathCopiedDirectoryForRepo).mapToLong(TestClass::calculateSize).sum();
//        } catch (IOException e) {
//            return 0L;
//        }
//    }
//
//    private static SVNCommitInfo modifyFile(ISVNEditor editor, String dirPath,
//                                            String filePath, byte[] oldData, byte[] newData) throws SVNException {
//        editor.openRoot(-1);
//        editor.openDir(dirPath, -1);
//        editor.openFile(filePath, -1);
//        editor.applyTextDelta(filePath, null);
//        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
//        String checksum = deltaGenerator.sendDelta(filePath, new ByteArrayInputStream(oldData), 0, new ByteArrayInputStream(newData), editor, true);
//        editor.closeFile(filePath, checksum);
//        editor.closeDir();
//        editor.closeDir();
//        return editor.closeEdit();
//    }
//
//    private static SVNCommitInfo deleteDir(ISVNEditor editor, String dirPath) throws SVNException {
//        editor.openRoot(-1);
//        editor.deleteEntry(dirPath, -1);
//
//        editor.closeDir();
//        return editor.closeEdit();
//    }
//
//
//    private static SVNCommitInfo copyDir(ISVNEditor editor, String srcDirPath,
//                                         String dstDirPath, long revision) throws SVNException {
//
//        editor.openRoot(-1);
//
//        editor.addDir(dstDirPath, srcDirPath, revision);
//        /*
//         * Closes the just added copy of the directory.
//         */
//        editor.closeDir();
//        /*
//         * Closes the root directory.
//         */
//        editor.closeDir();
//
//        return editor.closeEdit();
//    }
//
//
//    private static void setupLibrary() {
//        DAVRepositoryFactory.setup();
//
//        SVNRepositoryFactoryImpl.setup();
//
//        FSRepositoryFactory.setup();
//    }
//
//
//    @Override
//    public void createFiles() {
//        System.out.println("CreateFiles path " + file);
//        try (FileOutputStream out = new FileOutputStream(file)) {
//
//            int nbDesiredBytes = 9999;
//            int bufferSize = 1024;
//            byte[] buffer = new byte[bufferSize];
//            Random r = new Random();
//
//            int nbBytes = 0;
//            while (nbBytes < nbDesiredBytes) {
//                int nbBytesToWrite = Math.min(nbDesiredBytes - nbBytes, bufferSize);
//                byte[] bytes = new byte[nbBytesToWrite];
//                r.nextBytes(bytes);
//                out.write(bytes);
//                nbBytes += nbBytesToWrite;
//            }
//
//            System.out.println(" created binary file " + out.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static class ExportReporterBaton implements ISVNReporterBaton {
//
//        private long exportRevision;
//
//        public ExportReporterBaton(long revision) {
//            exportRevision = revision;
//        }
//
//        public void report(ISVNReporter reporter) throws SVNException {
//            try {
//                reporter.setPath("", null, exportRevision, SVNDepth.INFINITY, true);
//
//                reporter.finishReport();
//            } catch (SVNException svne) {
//                reporter.abortReport();
//                System.out.println("Report failed.");
//            }
//        }
//    }
//
//    private static class ExportEditor implements ISVNEditor {
//
//        private File myRootDirectory;
//        private SVNDeltaProcessor myDeltaProcessor;
//
//        public ExportEditor(File root) {
//            myRootDirectory = root;
//            myDeltaProcessor = new SVNDeltaProcessor();
//        }
//
//        public void targetRevision(long revision) throws SVNException {
//        }
//
//        public void openRoot(long revision) throws SVNException {
//        }
//
//        public void addDir(String path, String copyFromPath, long copyFromRevision) throws SVNException {
//            File newDir = new File(myRootDirectory, path);
//            if (!newDir.exists()) {
//                if (!newDir.mkdirs()) {
//                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: failed to add the directory ''{0}''.", newDir);
//                    throw new SVNException(err);
//                }
//            }
//            System.out.println("dir added: " + path);
//
//        }
//
//        public void openDir(String path, long revision) throws SVNException {
//        }
//
//        public void changeDirProperty(String name, SVNPropertyValue property) throws SVNException {
//        }
//
//        public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
//            File file = new File(myRootDirectory, path);
//            if (file.exists()) {
//                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: exported file ''{0}'' already exists!", file);
//                throw new SVNException(err);
//            }
//            try {
//                file.createNewFile();
//                for (int i = 0; i < 3; i++) {
//                    CreatedFiles createdFiles = new Export(
//                            new File(myRootDirectory + "/otherFile" + i));
//                    createdFiles.createFiles();
//                }
//
//
//            } catch (IOException e) {
//                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: cannot create new  file ''{0}''", file);
//                throw new SVNException(err);
//            }
//        }
//
//        public void openFile(String path, long revision) throws SVNException {
//        }
//
//        public void changeFileProperty(String path, String name, SVNPropertyValue property) throws SVNException {
//        }
//
//        public void applyTextDelta(String path, String baseChecksum) throws SVNException {
//            myDeltaProcessor.applyTextDelta((File) null, new File(myRootDirectory, path), false);
//        }
//
//        public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow) throws SVNException {
//            return myDeltaProcessor.textDeltaChunk(diffWindow);
//        }
//
//        public void textDeltaEnd(String path) throws SVNException {
//            myDeltaProcessor.textDeltaEnd();
//        }
//
//        public void closeFile(String path, String textChecksum) throws SVNException {
//            System.out.println("file added: " + path);
//        }
//
//        public void closeDir() throws SVNException {
//        }
//
//        public void deleteEntry(String path, long revision) throws SVNException {
//        }
//
//        public void absentDir(String path) throws SVNException {
//        }
//
//        public void absentFile(String path) throws SVNException {
//        }
//
//        public SVNCommitInfo closeEdit() throws SVNException {
//            return null;
//        }
//
//        public void abortEdit() throws SVNException {
//        }
//    }
//}