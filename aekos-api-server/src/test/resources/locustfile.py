#
# Simple load tester for hitting the services and determining performance under load.
# uses locust.io
# run with something like one of:
#    locust --host=https://api.aekos.org.au
#    locust --host=https://localhost:8443
#
from locust import HttpLocust, TaskSet, task

class MyTaskSet(TaskSet):

  def on_start(self):
    self.client.verify = False

  @task
  def my_task(self):
    print "executing my_task"
    #self.client.get("/v1/speciesAutocomplete.json?q=acacia")
    self.client.get("/v1/speciesData.json?speciesName=Acacia%20aneura%20var.%20intermedia&rows=20")


class MyLocust(HttpLocust):
    task_set = MyTaskSet
    min_wait = 1000
    max_wait = 3000

