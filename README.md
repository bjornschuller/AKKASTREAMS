This is just one of my learning projects that I use to become a better programmer. 

#Akka Streams
With this library you can create linear and non-linear streams for all kinds of processing.

Akka Streams build up a stream library based on Akka Actors 
In big data processing, one of the challenges is how to **consume** and **transform** **large amounts of data** efficiently and within a fixed set of resources. There are a few key problems faced when trying to consume the data:

**1. Blocking**

Blocking typically occurs in a "pull" based system. These systems pull data as required. The problem is that when there is no data to pull, they often block the thread which is inefficient.

**2. Back pressure**

In a "push" based system, it is possible for the producer to create more data than the consumer can handle which can cause the consumer to crash.

**Akka Streams attempts to solve both of these problems!**

Overall, the primary goal of streams is to provide a simple way to:

- build concurrent and memory bounded computations
- that can safely interact with various forms of non-blocking IO interfaces
- without involving blocking
- while embracing multi-core concurrency,
- and solving the typical pitfall of missing backpressure: faster producers overwhelm slower consumers that run on a separate thread, resulting in OutOfMemoryExceptions.

Going into some depth: 

- Streams do not run on the caller thread. Instead, they run on a different thread in the background, without blocking the caller.
- Stream stages usually share the same thread unless they are explicitly demarcated from each other by an asynchronous boundary (which can be added by calling .async between the stages we want to separate).
- Stages demarcated by asynchronous boundaries might run concurrently with each other.
- Stages do not run on a dedicated thread, but they borrow one from a common pool for a short period.


#Terminology Streams

####Source
A Source is the **input to the stream** and has a single output channel. Data flows from the Source, through the output channel, and into whatever might be connected to that Source. Examples of Sources:

A. a database query
B. an http request
c. something as simple as a random number generator
d. etc....

####Sink
A Sink is the endpoint for the stream. The data from the stream will eventually find it's way to the Sink. A Sink has a single input channel and no output channel. Data flows into the input channel and collects in the Sink. Examples of Sink behavior could be:

A. writing to a database
B. writing to a file 
C. aggregating data in memory
D. etc....

####Runnable Flow: [SOURCE]-->[ ]-->[SINK]
If you connect a Source to a Sink you get a Runnable Flow. This is the most basic complete form you can make in Akka Streams. 

####Flow **[SOURCE]-->[FLOW]-->[SINK]**
While you can do a lot with just a Source and a Sink, things get more interesting when you add a Flow into the mix. **A Flow can be used to apply transformations to the data coming out of a Source before putting it into a Sink.** The Flow then has a single input channel and a single output channel, this way the flow can be connected to a Source and a Sink. Connecting a Flow to just a Source gives you a new Source. Connecting a Flow to just a Sink gives you a new Sink. Connecting a Source, Flow and Sink gives you a **Runnable Flow**.  


A Runnable Flow, no matter how complex, includes all the facilities for **back pressure**. Data flows through the system one way, but requests for additional data to flow back through the system in the other direction. Under the hood, the Sink sends a request back through the Flows to the Source. This request notifies the Source that the Sink is ready to handle some more data. The Source will then push a set amount of data through the Flows into the Sink. The Sink will then process this data and when it has finished it will send another request for more data. This means that if the Sink gets backed up, then the time between those requests will increase and the necessary back pressure is generated.

#### Summary: Source -- Flow -- Sink 
The Source class represents the origin of the data, from here the various events will flow downstream; the Flow gets data from upstream, possibly applies transformations and emits the result to the next stage; finally the Sink is the last part of the stream, the final destination of your data.

This stages are characterized by the kind of ports they expose: **Source has a single output port, Flow has input and output port and Sink has just the input port.** For this reason, when you append a Flow to a Source, what you get in return is a new Source, and so on. You can compose as many stages as needed, given that you respect the kind of ports they expose (e.g. you can’t connect two Sources together).

In particular the Flow class exposes many of the functions anyone familiar with scala collections already knows, like map, filter and fold. A notable missing piece is the flatMap function: it’s actually there under the mapConcat alias (you know, you can’t run a for-comprehension on a Flow). Finally there are many more functions that are strictly related to the nature of the stream, like mapAsync, throttle or buffer.


#### Streaming File IO
Akka Streams provide simple **Sources** and **Sinks** that can work with **ByteString instances to perform IO operations on files**. The content of a ByteString is a sequence of bytes instead of characters. You can use the "utf8String" method to decodes this ByteString as a UTF-8 encoded String.

Processing csv files with Akka Streams is quite straightforward. First you need an Akka Streams Source that emits ByteStrings from a local file or another storage location. Once you have Source[ByteString, Future[IOResult] you can do all kind of operations on it. 


#### Akka Streams publisher and subscriber
The reactive streams specification essentially boils down to a minimal set of interfaces that together can be used to achieve asynchonous streaming with the ability to apply non-blocking back pressure. At a high level these interfaces provide a two way mechanism that allows for subscribers to signal that they can handle more items and for publishers to push items to the subscribers when they're ready.


The publisher is responsible for implementing a single function subscribe that takes a subscriber instance.In contrast, a subscription is a subscriber's communication channel back to the publisher that allows it to either cancel the subscription or signal demand by requesting more data. When a subscriber is subscribed via the subscribe function, the publisher is responsible with providing the subscriber with a subscription. The publisher must provide stream elements to the subscriber by invoking the onNext function, but must not exceed the total number elements that the subscriber has expressed demand for. 
Nex to this, there is the Processor interface which consists of a single interface that combines the Subscriber and Publisher interfaces. 

This line of communication between the publisher and subscriber provided by the subscription is the mechanism by which non blocking back pressure is achieved in Reactive Streams.


#### Adding elements to Source dynamically -- Non-finite Source
There are three ways this can be achieved by: 

1. Post Materialization with SourceQueue

2. Post Materialization with Actor

3. Pre Materialization with Actor

** for more information see: http://stackoverflow.com/questions/30964824/how-to-create-a-source-that-can-receive-elements-later-via-a-method-call

#### Non-finite Source -- ActorPublisher[T]
**The Pre Materialization with Actor is described in more detail**. 
One way to have a **non-finite source** is to use a special kind of actor as the source, one that mixes in the ActorPublisher trait. Next to this, since Akka is at it's heart an actor based concurrency framework, it makes sense that Akka allows us to create reactive streams completely out of actors. The ActorPublisher[T] trait can be mixed into any actor. First off al, mixing in the ActorPublisher[T] trait declares that your Actor intends to produce [T] values. Next to this, this trait has several functions that track the state of the subscription:

- **isActive** - Returns a boolean indicating if the stream is in an active state
- **isCompleted** - Returns a boolean indicating if the stream has been completed
- **isErrorEmitted** - Returns a boolean indicating if the stream has encountered an exception
- **totalDemand** - Returns an long indicating the total demand that has been signaled by subscriber.

Each of these functions are updated automatically during the lifetime of the subscription. For example, as the subscriber signals that they can handle more demand, the ActorPublisher trait automatically increases the totalDemand.

The ActorPublisher[T] trait also defines several functions for interacting with the stream:

- **onNext** - Pushes an element into the stream (Throws an exception if isActive is false or totalDemand < 1). In other words, the PublishActor publishes elements to the stream by calling the onNext method. 
- **onComplete** - Completes the stream
- **onError** - Terminates the stream with an error 

You are allowed to send as many elements as have been requested by the stream subscriber. **This amount can be inquired with totalDemand**. In other words, the totalDemand method checks the total number of requested elements from the stream subscriber. It is only allowed to use onNext when isActive and totalDemand>0, otherwise onNext will throw IllegalStateException. This is very intuitive, because you will otherwise sends a message to the stream that is not requested and the whole point of backpressure is that the subscriber tells you when he wants a next message. The totalDemand is updated automatically.

Finally two messages are used to signal to the implementing actor when the subscriber either cancels the subscription or signals more demand: Cancel and Request(n: Long).

#### Non-finite Source -- ActorSubscriber[T]
It provides a number of messages that will be sent to the actor as normal stream lifecycle events occur:

- OnNext - Emitted when publisher pushes a new element into the stream
- OnComplete - Emitted when publisher completes the stream
- OnError - Emitted when the publisher ends the stream due to an exception


Additionally, the ActorSubscriber trait forces any subclass to define a request strategy. A request strategy is used to control stream back pressure and as such dictates when a request is made of the subscription and for how many elements. A request strategy is a simple object that the subscriber uses after every element pushed into the stream to determine whether or not more elements should be requested. There are several implementations provided by akka-stream, however it is also very simple to roll your own. 

#### Send message to an Actor and message is handled by a stream
In the package: com.github.bschuller.datastream.actorbasedstream an ActorPublisher[SimpleMessage] is created, which declares that your Actor intends to produce [SimpleMessage] values. In addition, to this actor you can send a SimpleMessage. The messages send to this Actor are handled in stream. To connect this actor to a stream the following line of code is used:

```
val actorBasedSource = Source.actorPublisher[SimpleMessage](Props[ActorBasedSourcePublisher])

```

This creates a 'Source' that is materialized to an actorRef that points to the ActorBasedSourcePublisher Actor. So each message send to this Actor will be handled in this stream.  

#### Sending a materialized source ActorRef as a value through the stream
A thing that can be handy is to send a materialized source ActorRef through the Stream. This way you will be able to handle messages asynchronous and send the 'final response' back to the initial sender.
An example is made in com.github.bschuller.datastream.actorrefallthewaythroughstream

Important to note is that this ActorPublisher declares to produce tuple values where the second element in the tuple relates to the ActorRef of the sender. The ActorRef of the sender has a different ActorRef then that of the ActorPublisher. 




##### **Sources**

http://boldradius.com/blog-post/VS0NpTAAADAACs_E/introduction-to-akka-streams
http://blog.akka.io/streams/2016/07/06/threading-and-concurrency-in-akka-streams-explained
http://doc.akka.io/docs/akka/2.4.10/scala/stream/stream-io.html#streaming-file-io
http://janlisse.github.io/blog/2015/12/21/stream-processing-of-large-csv-files/
http://zuchos.com/blog/2015/05/23/how-to-write-a-subscriber-for-akka-streams/
http://www.measurence.com/tech-blog/2016/06/01/a-dive-into-akka-streams.html
http://stackoverflow.com/questions/29072963/how-to-add-elements-to-source-dynamically