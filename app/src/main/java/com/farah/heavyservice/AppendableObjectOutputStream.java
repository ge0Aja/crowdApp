package com.farah.heavyservice;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by Georgi on 8/19/2016.
 * this class is created to override writing the steam header when appending data to the backup binary
 * files
 * this class is used when we want to append data to already existing files
 * which suppresses the generated  IO exception
 */
public class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream output) throws IOException {
        super(output);
    }

    @Override
    protected void writeStreamHeader() throws IOException {

    }
}
