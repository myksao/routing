package africa.payaza.routing;

import africa.payaza.routing.security.domain.CourseClass;
import africa.payaza.routing.security.domain.ClassRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
public class RoutingApplication {

    private final ClassRepo classRepo;

    @Autowired
    public RoutingApplication(ClassRepo classRepo) {
        this.classRepo = classRepo;
        // Constructor for dependency injection if needed
    }

    public static void main(String[] args) {
        SpringApplication.run(RoutingApplication.class, args);
    }

    @GetMapping(value = "/api/v1/health/status",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, String>> status(){
        List<CourseClass> classsses = classRepo.findAll();
        Map<String, String> data = Map.of("status", classsses.toString(), "code", "200", "message", "HEALTH STATUS: OK!");
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok().headers(headers).body(data);
    }

}
