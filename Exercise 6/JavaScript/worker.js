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

    console.log("Charged card " + cardNumber + " that expires on " + expiryDate + " and has cvc " + cvc + " with amount of " + amount + " EUR");

    job.complete();
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
