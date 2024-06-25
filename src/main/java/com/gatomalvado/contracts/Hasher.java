package com.gatomalvado.contracts;

import java.nio.ByteBuffer;

public interface Hasher {
    long convertByteToHash(ByteBuffer buffer);
}
