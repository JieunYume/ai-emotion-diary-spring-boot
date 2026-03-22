package hanium.aidiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class AidiaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(AidiaryApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openSwagger() throws Exception {
		Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler https://localhost:8443/swagger-ui/index.html");
	}

}
