const ZB = require('zeebe-node');

const zbc = new ZB.ZBClient();

(async () => {

	const topology = await zbc.topology()

	console.log(JSON.stringify(topology, null, 2))

	zbc.createWorker({
		taskType: 'credit-deduction',
		taskHandler: job => { creditDeduction(job) },
	})

    zbc.createWorker({
        taskType: 'credit-card-charging',
        taskHandler: job => { creditCardCharging(job) },
     })

	zbc.createWorker({
		taskType: 'payment-invocation',
		taskHandler: job => { paymentInvocation(job) },
	})

	zbc.createWorker({
		taskType: 'payment-completion',
		taskHandler: job => { paymentCompletion(job) },
	})

    zbc.createWorker({
		taskType: 'calculate-discount',
		taskHandler: job => { calculateDiscount(job) },
	})

})()

function creditDeduction(job) {
    console.log(job.variables);

    console.log("Deducting customer credit...");

    const customerId = job.variables.customerId;
    const orderTotal = Number(job.variables.orderTotal);

    const customerCredit = getCustomerCredit(customerId);
    const openAmount = deductCredit(orderTotal, customerCredit);

    console.log("Charged " + customerCredit + " EUR from customer's credit. Open amount is: " + openAmount + " EUR");

    job.complete({openAmount: openAmount, customerCredit: customerCredit});
}


function creditCardCharging(job) {

    console.log("Charging card...");

    const cardNumber = job.variables.cardNumber,
        expiryDate = job.variables.expiryDate,
        amount = job.variables.openAmount,
        cvc = job.variables.cvc;

		if (isInvalidExpiryDate(expiryDate)) {

            console.log("Invalid expiration date: " + expiryDate);

            // job.error() doesnt seem to work somehow
            zbc.throwError({
                jobKey: job.key,
                errorCode: "creditCardChargeError",
                errorMessage: "Invalid expiration date"
            });

    } else {

        console.log("Charged card " + cardNumber + " that expires on " + expiryDate + " and has cvc " + cvc + " with amount of " + amount + " EUR");

    	job.complete();
    }
}

function paymentInvocation(job) {

	console.log("Starting payment process...");

	zbc.publishStartMessage({	name: 'paymentRequestMessage',
	                            variables: job.variables, });

	job.complete();
}

function paymentCompletion(job) {

	console.log("Responding to order process...");

	zbc.publishMessage({ correlationKey: job.variables.orderId,
                         name: 'paymentCompletedMessage',
	                     variables: job.variables, });

	job.complete();
}

function calculateDiscount(job) {

    orderTotal = job.variables.orderTotal;
    discount = job.variables.discount;

    discountedAmount = orderTotal - (orderTotal * discount / 100);

    job.complete({discountedAmount: discountedAmount});
}

function getCustomerCredit(customerId) {

    var credit = 0.0;

    const regEx = /\d+/;

    const match = customerId.match(regEx);

    if (match) { credit = parseFloat(match); }

    return credit;
}

function deductCredit(amount, credit) {

    var openAmount = 0.0;

    if (credit < amount) {
        openAmount = amount - credit;
    }

    return openAmount;
}

function isInvalidExpiryDate(expiryDate) {

    if (expiryDate.length != 5) {
        return true;
    } else {
        return false;
    }
}

