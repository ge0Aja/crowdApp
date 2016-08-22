package com.farah.heavyservice;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by Georgi on 8/19/2016.
 */
public class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream output) throws IOException {
        super(output);
    }

    @Override
    protected void writeStreamHeader() throws IOException {

    }
}
