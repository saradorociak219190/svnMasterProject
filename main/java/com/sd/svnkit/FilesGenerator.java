package com.sd.svnkit;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class FilesGenerator {
    private final SvnManager svnManager;
    private final RandomBytesProvider randomBytesProvider;

    public FilesGenerator(SvnManager svnManager,
                          RandomBytesProvider randomBytesProvider) {
        this.svnManager = svnManager;
        this.randomBytesProvider = randomBytesProvider;
    }

    public void generateRandomFiles(String dirPath, String filePrefix, int numberOfFiles, int contentSize) {
        IntStream.range(0, numberOfFiles)
                .forEach(fileId -> {
                    String fileName = format("%sfile_%s.data", filePrefix, fileId);
                    svnManager.commitFile(
                            "commit_" + fileName,
                            Paths.get(dirPath, fileName),
                            new ByteArrayInputStream(randomBytesProvider.getRandomBytes(contentSize)));
                });

    }
}
