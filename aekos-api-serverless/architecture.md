# AEKOS API Architecture
This document tries to convey the technical decisions that might not be apparent from just reading the code.

## POST with query string
I'll admit that it's a bit weird to use a POST request *and* have query string parameters. Usually you only see query string parameters on a GET request. 

Firstly, let's cover why we're using POST. We have inputs to our resources that are one-to-many. For example, the user can supply one or more species names to retrieve data for. As we're using AWS API Gateway, we aren't allowed to have duplicated/repeated query string parameter names; they aren't supported. You can try to pass them but you'll only end up with the last assigned value. This means we would have to get creative to support one-to-many values for a single query string parameter by having a string-ified JSON array as value or doing some custom serialisation. Neither are very appealing because we'll be subject to URL length restrictions and URL encoding will complicate matters. POST is a way out that makes it easy for clients to easily map objects into HTTP requests (probably very easily in most languages).

Now let's assume we need three parameters: `speciesNames`, `rows` and `start`. We could put all of them in the POST body, and that would work fine. However, we provide the HATEOAS `link` header in our responses and they can only convey a URI, not a POST body. It's for this reason that we've chosen to put the paging related parameters in the query string and the rest of the parameters in the body. This way clients can grab a URI from the `link` header response and submit the same POST body to get another page.
