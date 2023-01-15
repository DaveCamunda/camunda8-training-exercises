package worker;

import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import services.CreditCardService;
import services.CustomerService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableZeebeClient
@ComponentScan(basePackages = {"services"})
public class WorkerApplication {
	
	@Autowired
	public CustomerService customerService;
	
	@Autowired
	public CreditCardService creditCardService;
	
	@Autowired
	public ZeebeClient zeebeClient;

	private static Logger log = LoggerFactory.getLogger(WorkerApplication.class);

	public static void main(final String... args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	private static void logJob(final ActivatedJob job, Object parameterValue) {
	  
		log.info("Job Received: [type: {}, process key: {}]\n[variables: {}]",
				job.getType(),
				job.getProcessInstanceKey(),
				job.getVariables());
	}

	@JobWorker(type = "credit-deduction") 
	public Map<String, Object> handleCreditDeduction(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
    
		String customerId = (String) job.getVariablesAsMap().get("customerId");
		
		Double orderTotal = (Double) job.getVariablesAsMap().get("orderTotal");
		
	    Double openAmount = customerService.deductCredit(customerId, orderTotal);
	    
	    Double customerCredit = customerService.getCustomerCredit(customerId);	
	    
	    Map<String, Object> variables = new HashMap<String, Object>();
	    
	    variables.put("openAmount", openAmount);
	    variables.put("customerCredit", customerCredit);
		
	    return variables;
	}
  
	@JobWorker(type = "credit-card-charging") 
	public void handleChargeCreditCard(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
		
	    String cardNumber = (String) job.getVariablesAsMap().get("cardNumber"), 
  		              cvc = (String) job.getVariablesAsMap().get("CVC"), 
  	           expiryDate = (String) job.getVariablesAsMap().get("expiryDate");
  
	    Double openAmount = (Double) job.getVariablesAsMap().get("openAmount");
	    
	    try {
	    	creditCardService.chargeAmount(cardNumber, cvc, expiryDate, openAmount);
	    } catch (IllegalArgumentException e) {
	    	throw new ZeebeBpmnError("creditCardChargeError", "Invalid expiry date");	  
	    } 
	}
	
	@JobWorker(type = "payment-invocation") 
	public void handlePaymentInvocation(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
		
		zeebeClient.newPublishMessageCommand().messageName("paymentRequestMessage").correlationKey("").variables(job.getVariablesAsMap()).send().join();
	}
	
	@JobWorker(type = "payment-completion") 
	public void handlePaymentCompletion(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
		
		String orderId = (String) job.getVariablesAsMap().get("orderId");
		
		zeebeClient.newPublishMessageCommand().messageName("paymentCompletedMessage").correlationKey(orderId).variables(job.getVariablesAsMap()).send().join();
	}
	
	@JobWorker(type = "calculate-discount") 
	public Map<String, Object> handleDiscountApplication(final JobClient client, final ActivatedJob job) {
		
		logJob(job, null);
		
		Integer discount = (Integer) job.getVariablesAsMap().get("discount");
		
		Double orderTotal = (Double) job.getVariablesAsMap().get("orderTotal");
		
		Double discountedAmount = orderTotal - (orderTotal * discount / 100);
		
	    Map<String,Object> variables = new HashMap<String, Object>();
	    
	    variables.put("discountedAmount", discountedAmount);
    
		return variables;
	}
  
}
