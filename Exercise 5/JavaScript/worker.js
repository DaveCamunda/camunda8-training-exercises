const ZB = require('zeebe-node');

(async () => {

  const zbc = new ZB.ZBClient()
	const topology = await zbc.topology()

	console.log(JSON.stringify(topology, null, 2))

	zbc.createWorker({
		taskType: 'credit-deduction',
		taskHandler: job => { console.log(job.variables)
			                    job.complete() },
	})

  zbc.createWorker({
    taskType: 'credit-card-charging',
    taskHandler: job => { console.log(job.variables)
                          job.complete() },
  })

})()
