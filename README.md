# RSocket Test Server

If you have an application that connects to an RSocket server at runtime, how do you test it? We need a way for a test to start a server and tell us where it is listening, and then we need to be able to register request and response examples (a.k.a. "contracts"). That's what this [project](https://github.com/dsyer/rsocket-test-server) provides - it's like Wiremock but for RSocket.

## Getting Started

The easiest way to use this project is as a JUnit (Jupiter) extension, e.g:

```java
@SpringBootTest
@ExtendWith(RSocketServerExtension.class)
class SocketsApplicationTests {
	...
}
```

With this extension installed the Spring Boot tests will run with an RSocket server listening on a port given by `test.rsocket.server.port`, so the test can connect directly to it, or (more likely) the code that it is testing will connect to it. You might need to tell it where to connect via the `@SpringBootTest` annotation, e.g. if the application is looking for a property at runtime called `rsocket.port`:

```java
@SpringBootTest("rsocket.port=${test.rsocket.server.port}")
@ExtendWith(RSocketServerExtension.class)
class SocketsApplicationTests {
	...
}
```

## Defining message mappings in JSON

Test methods can inject an `RSocketMessageCatalog` or `RSocketMessageRegistry` and then use those to set up or inspect the state of the server. By default the server reads JSON contracts from the classpath at `/catalog/*.json`, so you can set those up locally or in a test library that you share with the sarver. The structure of the JSON mirrors the `MessageMapping` that is stored in the `RSocketMessageCatalog`. Here's an example (the request and response are just JSON objects):

```json
{
	"pattern": "events.response.*",
	"frameType": "REQUEST_RESPONSE",
	"request": {
		"origin": "Client"
	},
	"response": {
		"origin": "Server",
		"interaction": "Response",
		"index": 0
	}
}
```

This mapping will match any `REQUEST_RESPONSE` frame type on a route that matches the pattern, and which also has a request with a field "origin" equal to "Client". You can also match on a pattern in a field in the request by adding wildcards. Or you can leave the request out to match only on the route. If the frame type is `REQUEST_RESPONSE` then the response is single valued. If the frametype is `REQUEST_STREAM` you can provide a multi-valued "responses", for example:

```json
{
	"pattern": "my.stream.route",
	"frameType": "REQUEST_STREAM",
	"responses": [
		{
			"origin": "Server",
			"interaction": "Stream",
			"index": 0
		},
		{
			"origin": "Server",
			"interaction": "Stream",
			"index": 1
		},
		{
			"origin": "Server",
			"interaction": "Stream",
			"index": 2
		}
	]
}
```

In addition you can specify an integer field "repeat" to repeat the responses to form a longer stream. If the frame type is `REQUEST_FNF` then there is no response, and the request will be ignored. And finally, if the frame type is `REQUEST_CHANNEL` then the format of the mapping JSON is the same as the `REQUEST_STREAM`, except that every time a message comes in from the input stream, the output stream is emitted again.

## Manipulating and inspecting the message catalog

If you need more flexibility with the processing rules you can create your own `MessageMapping` from the convenient static factory methods in the interface. You can supply a handler function (except for the fire and forget case), a pattern to match, and optionally a request to match. These methods can be used to dynamically register mappings to define the expected behaviour of the server at runtime.

You can inspect the state of the server by acquiring a `MessageMapping` from the catalog, and then calling one of the `drain()` methods to drain off the requests that have been received. For example:

```java
@SpringBootTest
@ExtendWith(RSocketServerExtension.class)
class DynamicRouteTests {

	private RSocketRequester rsocketRequester;

	public DynamicRouteTests(@Autowired RSocketRequester.Builder rsocketRequesterBuilder,
			@Value("${test.rsocket.server.port:7000}") int port) {
		rsocketRequester = rsocketRequesterBuilder.tcp("localhost", port);
	}

	@Test
	void response(RSocketMessageRegistry catalog) {
		MessageMapping response = MessageMapping.response("response")
				.response(new Foo("Server", "Response"));
		catalog.register(response);
		assertThat(rsocketRequester.route("response").data(new Foo("Client", "Request"))
				.retrieveMono(Foo.class).doOnNext(foo -> {
					System.err.println(foo);
					assertThat(foo.getOrigin()).isEqualTo("Server");
				}).block()).isNotNull();
		assertThat(response.drain()).hasSize(1);
		assertThat(response.drain()).hasSize(0);
	}

}
```

The code here is still really a prototype, but it's already potentially quite useful. So try it out and sens feedback and maybe we can mature it to the point where we can release it.
