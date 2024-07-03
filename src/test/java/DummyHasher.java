import java.nio.ByteBuffer;

import com.gatomalvado.consistent.contracts.Hasher;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class DummyHasher implements Hasher {

    @Override
    public long convertByteToHash(ByteBuffer buffer) {
        HashFunction hashFunction = Hashing.murmur3_128();
        return hashFunction.hashBytes(buffer.array()).asLong();
    }
}
