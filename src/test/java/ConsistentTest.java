import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gatomalvado.consistent.config.ConsistentConfig;
import com.gatomalvado.consistent.contracts.Member;
import com.gatomalvado.consistent.core.Consistent;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class ConsistentTest {

    private static Consistent consistent;

    @BeforeAll
    public static void init(){
        consistent = createDefaultConsistentBean();
    }

    @Test
    public void test_the_creation_of_the_consistent_object_with_provided_config() {
        Assertions.assertNotNull(consistent);
    }

    @Test
    public void test_the_assignment_of_partition_to_the_members() {
        Map<String, List<Integer>> owners = new HashMap<>();
        for (int i = 1; i <= consistent.getConfig().getPartitionCount(); i++) {
            Member owner = consistent.getPartitionOwner(i);
            List<Integer> partitionAssignedList = owners.getOrDefault(owner.convertToString(), new ArrayList<>());
            partitionAssignedList.add(i);
            owners.put(owner.convertToString(), partitionAssignedList);
        }
        System.out.println(owners);
        System.out.println(consistent.getAverageLoad());
    }

    @Test
    public void test_the_location_of_the_key_among_the_partition(){
        String dummyKeys = "This is my key";
        ByteBuffer buffer = ByteBuffer.wrap(dummyKeys.getBytes());
        Member member = consistent.locateKey(buffer);
        System.out.println(member);
    }

    private static Consistent createDefaultConsistentBean() {
        // create the configuration
        ConsistentConfig cc = ConsistentConfig.builder()
            .load(1.25)
            .partitionCount(220)
            .replicationFactor(20)
            .hasher((b) -> {
                HashFunction hashFunction = Hashing.murmur3_128();
                return hashFunction.hashBytes(b.array()).asLong();
            }).build();

        // create the members
        List<Member> members = List.of(new DummyNode(), new DummyNode(), new DummyNode(),
            new DummyNode(), new DummyNode(), new DummyNode(),
            new DummyNode(), new DummyNode());

        return Consistent.init(cc, members);
    }

}
