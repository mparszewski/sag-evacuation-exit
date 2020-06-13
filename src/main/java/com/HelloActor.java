package com;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class HelloActor extends AbstractBehavior<HelloMessage>
{
    public HelloActor(ActorContext<HelloMessage> context) {
        super(context);
    }

    @Override
    public Receive<HelloMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(HelloMessage.class, this::printMessage)
                .build();
    }

    public HelloActor printMessage(HelloMessage message) {
        System.out.println( "My message is: " +  message.getMessage() );
        return this;
    }

    public static void main( String[] args )
    {
        ActorSystem actorSystem = ActorSystem.create( "MySystem" );
        ActorRef actorRef = actorSystem.actorOf( Props.create( HelloActor.class ), "myActor" );
        actorRef.tell( new HelloMessage( "Hello, Akka!" ), actorRef );

        try
        {
            Thread.sleep( 1000 );
        }
        catch( Exception e ) {}

        actorSystem.stop( actorRef );
        actorSystem.terminate();
    }
}
