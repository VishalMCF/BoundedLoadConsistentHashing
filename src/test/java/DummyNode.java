import java.util.random.RandomGenerator;

import com.gatomalvado.consistent.contracts.Member;

import lombok.Getter;

@Getter
public class DummyNode implements Member {

    private String host;
    private int port;

    public DummyNode() {
        this.host = RandomGenerator.getDefault().nextInt(1,255)
            + "." + RandomGenerator.getDefault().nextInt(1,255)
            + "." + RandomGenerator.getDefault().nextInt(1,255);
        this.port = RandomGenerator.getDefault().nextInt(8080,9090);
    }

    @Override
    public String convertToString() {
        return this.host+":"+this.port;
    }
}
