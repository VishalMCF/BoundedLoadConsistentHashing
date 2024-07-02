import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.gatomalvado.config.ConsistentConfig;
import com.gatomalvado.contracts.Member;
import com.gatomalvado.core.Consistent;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class ConsistentTest {

    @Test
    public void test_the_creation_of_the_consistent_object_with_provided_config(){

        // create the configuration
        ConsistentConfig cc = ConsistentConfig.builder()
            .load(1.25)
            .partitionCount(20)
            .partitionCount(71)
            .hasher((b)->{
                HashFunction hashFunction = Hashing.murmur3_128();
                return hashFunction.hashBytes(b.array()).asLong();
            }).build();

        // create the members
        List<Member> members = List.of(new DummyNode(), new DummyNode(), new DummyNode(),
            new DummyNode(), new DummyNode(), new DummyNode(),
            new DummyNode(), new DummyNode());

        Consistent consistent = Consistent.getInstance(cc, members);

        Assertions.assertNotNull(consistent);
    }

}
