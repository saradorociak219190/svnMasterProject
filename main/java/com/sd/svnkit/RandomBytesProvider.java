package com.sd.svnkit;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBytesProvider {

    byte[] getRandomBytes(int number) {
        final byte[] bytes = new byte[number];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }
}
