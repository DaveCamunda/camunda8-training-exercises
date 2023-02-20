import asyncio

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

# Will use environment variable ZEEBE_ADDRESS or localhost:26500 and NOT use TLS
# create_insecure_channel returns a grpc.aio.Channel instance. If needed you
# can build one on your own
grpc_channel = create_insecure_channel()
worker = ZeebeWorker(grpc_channel)

# With custom hostname/port
grpc_channel = create_insecure_channel(hostname=configs.get("hostname").data, port=443)
worker = ZeebeWorker(grpc_channel)

# Connect to zeebe cluster in camunda cloud
grpc_channel = create_camunda_cloud_channel(
    client_id=configs.get("zeebe.client.cloud.clientId").data,
    client_secret=configs.get("zeebe.client.cloud.clientSecret").data,
    cluster_id=configs.get("zeebe.client.cloud.clusterId").data,
    region=configs.get("zeebe.client.cloud.region").data,  # Default value is bru-2
)
worker = ZeebeWorker(grpc_channel)

# Create a task like this:
@worker.task(task_type="credit-deduction")
def credit_deduction_task(job: Job):
    print("Deducting credit...")
    return {}

@worker.task(task_type="credit-card-charging")
def credit_card_charging_task(job: Job):
    print("Charging credit card...")
    return {}

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(worker.work())
