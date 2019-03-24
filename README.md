# fs2-nakadi
fs2-nakadi is [Nakadi](https://nakadi.io/) client for Scala based on FS2.

## Under the hood

- [http4s](https://github.com/http4s/http4s) as the underlying http client
- [circe](https://github.com/circe/circe) for JSON encoding/decoding
- [fs2](https://github.com/functional-streams-for-scala/fs2) for streaming

## Status
Work is still in progress but the basic DSLs are defined.

## Installation
```sbtshell
libraryDependencies += "io.nigo" %% "fs2-nakadi" % "0.1.0-M2"
```

## Usage

### Event Types
There are three main categories of event type defined by Nakadi:

- Business Event: An event that is part of, or drives a business process, such as a state transition in a customer order.

- Data Change Event: An event that represents a change to a record or other item, or a new item. Change events are associated with a create, update, delete, or snapshot operation.

- Undefined Event: A free form category suitable for events that are entirely custom to the producer.

fs2-nakadi provides a simple DSL for dealing with event types:
```scala
import cats.effect.IO
import java.net.URI
import fs2.nakadi.model._
import fs2.nakadi.dsl._

// Define Nakadi setting
implicit val config: NakadiConfig[IO] = NakadiConfig[IO](new URI("<nakadi-uri>"))

// Define EventType
val business = EventType(
    name = EventTypeName("business-data"), 
    owningApplication = "fs2-nakadi", 
    category = Category.Business
)

// Create the EventType
EventTypes[IO].create(business)

// Find the EventType
EventTypes[IO].get(EventTypeName("business-data"))
```

### Publish Events
You can define your own ADT of the events and simply publish them to the desired EventType using Events DSL:

```scala
import cats.effect.IO
import java.util.UUID
import java.time.ZonedDateTime
import fs2.nakadi.model._
import fs2.nakadi.model.Event.Business
import fs2.nakadi.dsl._
import fs2.Stream

// Define Event ADT
case class User(id: UUID, firstName: String, lastName: String, createdAt: ZonedDateTime)

object User {
  import io.circe.{Encoder, Decoder}
  import io.circe.derivation._ 
  
  implicit val userEncoder: Encoder[User] = deriveEncoder(renaming.snakeCase)
  implicit val userDecoder: Decoder[User] = deriveDecoder(renaming.snakeCase)
}

// Define Event
val user: User = User(UUID.randomUUID(), "john", "snow", ZonedDateTime.now()) 
val event: Event[User] = Business(
  data = user,
  metadata = Metadata()
)

// Publish a list of Event
Events[IO].publish[User](EventTypeName("user-data"), List(event))


// Publish a Stream
Stream
  .emits(List(event))
  .through(Events[IO].publishStream[User](EventTypeName("user-data")))

```

### Consume Events
fs2-nakadi supports high-level event consumption using subscriptions

```scala
import cats.effect.IO
import java.net.URI
import fs2.nakadi.model._
import fs2.nakadi.dsl._
import fs2.Stream

// Create a subscription if doesn't exist
val sub = 
Subscriptions[IO]
    .createIfDoesntExist(
        Subscription(
          owningApplication = "fs2-nakadi", 
          eventTypes = Some(List(EventTypeName("user-data")))
        )
    )
    

// Create event stream
Stream
    .eval(sub)
    .flatMap(s => Subscriptions[IO].eventStream[User](s.id.get, StreamConfig()))

```

You can also use `managedEventStream` which receives a callback and applies it to every event:

```scala
val callback =
    EventCallback.successAlways[User](
      _.subscriptionEvent.events
        .foreach(_.foreach(e => logger.info(s"Received Event: ${e.data.toString}"))))

Stream
    .eval(sub)
    .flatMap(s => Subscriptions[IO].managedEventStream[User](1)(s.id.get, callback, StreamConfig()))
```

 



