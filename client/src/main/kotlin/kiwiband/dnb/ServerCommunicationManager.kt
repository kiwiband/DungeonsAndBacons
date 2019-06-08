package kiwiband.dnb

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kiwiband.dnb.events.Event
import kiwiband.dnb.events.EventUpdateMap
import kiwiband.dnb.map.LocalMap
import kiwiband.dnb.rpc.GameServiceGrpc
import kiwiband.dnb.rpc.Gameservice
import org.json.JSONObject
import java.util.concurrent.locks.ReentrantLock

class ServerCommunicationManager(private val eventLock: ReentrantLock) {

    private lateinit var gameService: GameServiceGrpc.GameServiceBlockingStub
    private lateinit var gameServiceAsync: GameServiceGrpc.GameServiceStub
    private val mapdUpdateHandler = MapUpdateHandler()
    private var id = -1


    fun connect(): Pair<Int, LocalMap> {
        val channel = ManagedChannelBuilder.forAddress("localhost", 12345).usePlaintext().build()
        gameService = GameServiceGrpc.newBlockingStub(channel)
        gameServiceAsync = GameServiceGrpc.newStub(channel)
        val state = gameService.connect(Gameservice.Empty.getDefaultInstance())
        id = state.playerId

        println("Connected to server with id: $id")
        val idMessage = Gameservice.PlayerId.newBuilder().setId(id).build()

        gameServiceAsync.updateMap(idMessage, mapdUpdateHandler)
        return id to LocalMap.loadMap(JSONObject(state.mapJson))
    }

    fun sendEvent(event: Event) {
        gameService.userEvent(
            Gameservice.UserEvent.newBuilder().setPlayerId(id).setJson(event.toJSON().toString()).build()
        )
    }

    fun disconnect() {
        gameService.disconnect(Gameservice.PlayerId.newBuilder().setId(id).build())
    }

    private inner class MapUpdateHandler : StreamObserver<Gameservice.JsonString> {
        override fun onNext(value: Gameservice.JsonString) {
            println("Map has been updated")
            eventLock.lock()
            EventUpdateMap.dispatcher.run(
                EventUpdateMap(LocalMap.loadMap(JSONObject(value.json)))
            )
            eventLock.unlock()
        }

        override fun onError(t: Throwable) {
            println("Error getting map update: ${t.message}")
        }

        override fun onCompleted() {
            println("Game session completed")
        }
    }
}