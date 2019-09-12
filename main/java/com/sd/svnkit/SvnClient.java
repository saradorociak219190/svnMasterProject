package com.sd.svnkit;

import java.io.File;
import java.net.URI;

public interface SvnClient {

    void checkoutRepository(URI uri, File localDirectory);

    void commit(String commitName, File... files);

    void merge(URI sourceBranch, File targetLocalBranch);

}
