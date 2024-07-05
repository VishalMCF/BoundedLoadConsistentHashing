import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.gatomalvado.consistent.contracts.Hasher;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class DummyHasher implements Hasher {

    @Override
    public long convertByteToHash(ByteBuffer buffer) {
        HashFunction hashFunction = Hashing.murmur3_128();
        long hash = hashFunction.hashBytes(buffer.array()).asLong();
        return hash;
    }
}
