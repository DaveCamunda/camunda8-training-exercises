import asyncio, re, json

from typing import Dict

from pyzeebe import (
    Job,
    ZeebeWorker,
    create_camunda_cloud_channel,
    create_insecure_channel,
    create_secure_channel,
)
from pyzeebe.errors import BusinessError

from jproperties import Properties

configs = Properties()

with open('application.properties', 'rb') as config_file:
    configs.load(config_file)

# Connect to zeebe cluster in camunda cloud
grpc_channel = create_camunda_cloud_channel(
    client_id=configs.get("zeebe.client.cloud.clientId").data,
    client_secret=configs.get("zeebe.client.cloud.clientSecret").data,
    cluster_id=configs.get("zeebe.client.cloud.clusterId").data,
    region=configs.get("zeebe.client.cloud.region").data,  # Default value is bru-2
)
worker = ZeebeWorker(grpc_channel)

@worker.task(task_type="credit-deduction")
def credit_deduction_task(customerId: str, orderTotal: float) -> dict:
    print("Deducting credit...")
    return credit_deduction(customerId, orderTotal)

@worker.task(task_type="credit-card-charging", variables_to_fetch=None)
def credit_card_charging_task(cardNumber: str, expiryDate: str, openAmount: float, cvc: str) -> dict:
    print("Charging credit card...")
    credit_card_charging(cardNumber, expiryDate, openAmount, cvc)
    return {}

def credit_deduction(customerId, orderTotal):
    customerCredit = get_customer_credit(customerId)
    openAmount = deduct_credit(orderTotal, customerCredit)

    print("Deducted " + str(customerCredit) + " EUR from customer's credit. Open amount is: " + str(openAmount) + " EUR")

    return {'openAmount': openAmount, 'customerCredit': customerCredit}

def credit_card_charging(cardNumber, expiryDate, openAmount, cvc):

    print("Charged card " + cardNumber + " that expires on " + expiryDate + " and has cvc " + cvc + " with amount of " + str(openAmount) + " EUR")

    return

def get_customer_credit(customerId):

    credit = 0.0;

    regEx = re.compile('\\d+')

    match = regEx.search(customerId)

    if match is not None:
        credit = float(match.group())

    return credit;

def deduct_credit(amount, credit):

    openAmount = 0.0

    if credit < amount:
        openAmount = amount - credit

    return openAmount

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(worker.work())
