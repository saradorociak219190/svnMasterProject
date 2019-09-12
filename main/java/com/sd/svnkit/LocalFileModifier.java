package com.sd.svnkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LocalFileModifier {
    private final RandomBytesProvider randomBytesProvider;

    public LocalFileModifier(RandomBytesProvider randomBytesProvider) {
        this.randomBytesProvider = randomBytesProvider;
    }

    public void modify(File filePath, double modificationFactor) {
        try {
            byte[] bytes = Files.readAllBytes(filePath.toPath());
            int numberOfBytesToBeModified = (int) (bytes.length * modificationFactor);
            byte[] overridingBytes = randomBytesProvider.getRandomBytes(numberOfBytesToBeModified);
            System.arraycopy(overridingBytes, 0, bytes, 0, overridingBytes.length);
            Files.write(filePath.toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
