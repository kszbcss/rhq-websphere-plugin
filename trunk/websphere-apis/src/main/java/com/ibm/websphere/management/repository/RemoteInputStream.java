package com.ibm.websphere.management.repository;

import java.io.IOException;
import java.io.InputStream;

public class RemoteInputStream extends InputStream {
    @Override
    public int read() throws IOException {
        return 0;
    }
}
