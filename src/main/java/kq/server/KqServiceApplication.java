package kq.server;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author wangsh
 */
@SpringBootApplication
@MapperScan("kq.server.**.mapper")
@EnableScheduling
public class KqServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(KqServiceApplication.class, args);
    }
}
