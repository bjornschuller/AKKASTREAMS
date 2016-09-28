
First of all, this is just one of my learning projects that I use to become a better programmer. 

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

sources

http://boldradius.com/blog-post/VS0NpTAAADAACs_E/introduction-to-akka-streams

http://blog.akka.io/streams/2016/07/06/threading-and-concurrency-in-akka-streams-explained

http://doc.akka.io/docs/akka/2.4.10/scala/stream/stream-io.html#streaming-file-io

http://janlisse.github.io/blog/2015/12/21/stream-processing-of-large-csv-files/

http://www.measurence.com/tech-blog/2016/06/01/a-dive-into-akka-streams.html