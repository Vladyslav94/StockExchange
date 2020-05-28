package startApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StartStockApp implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(StartStockApp.class, args);

    }

    @Autowired
    private GetDataFromWeb getDataFromWeb;

    public void run(String... args) throws Exception {
        getDataFromWeb.getData();
    }
}
