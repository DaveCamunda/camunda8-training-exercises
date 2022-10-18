package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreditCardService {
  
	private static final Logger LOG = LoggerFactory.getLogger(CreditCardService.class);
  
	public void chargeAmount(String cardNumber, String cvc, String expiryDate, Double amount) {
		
		LOG.info("charging card {} that expires on {} and has cvc {} with amount of {}", cardNumber, expiryDate, cvc, amount);
    
		LOG.info("payment completed");
  }
  
}
