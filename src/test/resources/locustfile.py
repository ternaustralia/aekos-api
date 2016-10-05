#
# Simple load tester for hitting the services and determining performance under load.
# uses locust.io
#
from locust import HttpLocust, TaskSet, task

class MyTaskSet(TaskSet):
    @task
    def my_task(self):
        print "executing my_task"
	#self.client.get("https://api.aekos.org.au/v1/speciesAutocomplete.json?q=acacia")
	self.client.get("https://api.aekos.org.au/v1/speciesData.json?speciesName=Acacia%20aneura%20var.%20intermedia&rows=20")


class MyLocust(HttpLocust):
    task_set = MyTaskSet
    min_wait = 1000
    max_wait = 3000

