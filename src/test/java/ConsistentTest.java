import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public static void init() throws InterruptedException {
        Thread.sleep(2000);
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

    @Test
    public void test_the_removal_of_the_member_among_the_members(){
        Member m1 = ()->"Member100099999999";
        Member m2 = ()->"Member111111111111";
        Member m3 = ()->"Member777777777777";
        consistent.add(m1);
        Assertions.assertTrue(retrieveOwners().containsKey(m1.convertToString()));
        consistent.add(m2);
        Assertions.assertTrue(retrieveOwners().containsKey(m2.convertToString()));
        consistent.add(m3);
        Assertions.assertTrue(retrieveOwners().containsKey(m3.convertToString()));
        consistent.remove(m1.convertToString());
        Map<String, List<Integer>> owners = retrieveOwners();
        Assertions.assertFalse(retrieveOwners().containsKey(m1.convertToString()));
        consistent.remove(m2.convertToString());
        owners = retrieveOwners();
        Assertions.assertFalse(owners.containsKey(m2.convertToString()));
        consistent.remove(m3.convertToString());
        owners = retrieveOwners();
        Assertions.assertFalse(owners.containsKey(m3.convertToString()));
    }

    private static Consistent createDefaultConsistentBean() {
        // create the configuration
        ConsistentConfig cc = ConsistentConfig.builder()
            .load(1.25)
            .partitionCount(45)
            .replicationFactor(1)
            .hasher((b) -> {
                HashFunction hashFunction = Hashing.murmur3_128();
                return hashFunction.hashBytes(b.array()).asLong();
            }).build();

        // create the members
        List<Member> members = List.of(()->"Member1", ()->"Member2", ()->"Member3",
            ()->"Member4", ()->"Member5", ()->"Member6",
            ()->"Member7", ()->"Member8");

        return new Consistent(cc, members);
    }

    private Map<String, List<Integer>> retrieveOwners() {
        Map<String, List<Integer>> owners = new ConcurrentHashMap<>();
        for (int i = 1; i <= consistent.getConfig().getPartitionCount(); i++) {
            Member owner = consistent.getPartitionOwner(i);
            List<Integer> partitionAssignedList = owners.getOrDefault(owner.convertToString(), new ArrayList<>());
            partitionAssignedList.add(i);
            owners.put(owner.convertToString(), partitionAssignedList);
        }
        return owners;
    }

}
