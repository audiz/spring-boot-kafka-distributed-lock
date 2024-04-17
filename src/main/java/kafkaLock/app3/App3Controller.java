package kafkaLock.app3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Test controller to increment value
 * */
@RestController
public class App3Controller {
    private int i = 0;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("getInt")
    public Integer getInt() {
        return i;
    }

    @GetMapping("setInt/{num}")
    public String setInt(@PathVariable int num) {
        i = num;
        return "ok";
    }

    @GetMapping("start")
    public String start() {
        restTemplate.getForEntity("http://localhost:8083/setInt/0", String.class);
        new Thread(() -> {
            restTemplate.getForEntity("http://localhost:8081/test", String.class);
        }).start();
        new Thread(() -> {
            restTemplate.getForEntity("http://localhost:8082/test", String.class);
        }).start();
        return "ok";
    }
}
