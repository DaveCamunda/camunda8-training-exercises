package worker;

import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
public class WorkerApplication {

	private static Logger log = LoggerFactory.getLogger(WorkerApplication.class);

	public static void main(final String... args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	private static void logJob(final ActivatedJob job, Object parameterValue) {
	  
		log.info("complete job\n>>> [type: {}, key: {}, element: {}, workflow instance: {}]\n{deadline; {}]\n[headers: {}]\n[variable parameter: {}\n[variables: {}]",
				job.getType(),
				job.getKey(),
				job.getElementId(),
				job.getProcessInstanceKey(),
				Instant.ofEpochMilli(job.getDeadline()),
				job.getCustomHeaders(),
				parameterValue,
				job.getVariables());
	}

	@ZeebeWorker(type = "credit-deduction") 
	public void handleCreditDeduction(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
    
		client.newCompleteCommand(job.getKey()).send().join();
	}
  
	@ZeebeWorker(type = "credit-card-charging") 
	public void handleChargeCreditCard(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
    
		client.newCompleteCommand(job.getKey()).send().join();
	}
  
}
