package com.example

import java.util.UUID

import com.google.protobuf.ByteString
import io.axoniq.eventstore.grpc.events_api._
import io.axoniq.eventstore.messages.{Event, SerializedObject}
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

object MyApp extends App {
  val channel = ManagedChannelBuilder.forAddress("localhost", 8123).usePlaintext(true).build
  val eventWriter = EventStoreGrpc.stub(channel)

  val event = Event(
    aggregateIdentifier = UUID.randomUUID().toString,
    payload = Some(SerializedObject(data = ByteString.copyFromUtf8("Hello World!"))))

  val eventWriterResponseObserver = new StreamObserver[Confirmation] {
    override def onNext(value: Confirmation) = println(s"write confirmation: ${value.success}")
    override def onError(t: Throwable) =  println("write error ", t)
    override def onCompleted() = println("write completed")
  }
  val eventWriterRequestObserver = eventWriter.appendEvent(eventWriterResponseObserver)

  eventWriterRequestObserver.onNext(event)
  eventWriterRequestObserver.onCompleted()
  println("sleeping")
  Thread.sleep(1000)
  println("done sleeping")

  val eventReader = EventStoreGrpc.stub(channel)
  val eventReaderResponseObserver = new StreamObserver[EventWithToken] {
    override def onNext(value: EventWithToken) = println(s"read event: ${value}")
    override def onCompleted() = println("read completed")
    override def onError(t: Throwable) = println("read error")
  }
  val eventReaderRequestObserver = eventReader.listEvents(eventReaderResponseObserver)
  eventReaderRequestObserver.onNext(GetEventsRequest())
  eventReaderRequestObserver.onCompleted();

  println("sleeping")
  Thread.sleep(1000)
  println("done sleeping")

}