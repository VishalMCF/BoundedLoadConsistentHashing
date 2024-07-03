package com.gatomalvado.consistent.contracts;

import java.nio.ByteBuffer;

public interface Hasher {
    long convertByteToHash(ByteBuffer buffer);
}
