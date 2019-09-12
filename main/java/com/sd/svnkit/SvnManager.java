package com.sd.svnkit;

import java.io.InputStream;
import java.nio.file.Path;

public interface SvnManager {

    void commitEmptyFolder(String file);

    void commitFile(String commitName, Path filePath, InputStream content);

    void copyDir(String sourceDir, String targetDir);

}
