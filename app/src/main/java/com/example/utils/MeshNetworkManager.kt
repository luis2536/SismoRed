package com.example.utils

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MeshNetworkManager {
    private const val SERVICE_ID = "com.syntropy.delta.sismoredven.MESH"
    
    // We use P2P_CLUSTER for true mesh networking where all nodes act as clients and servers
    private val STRATEGY = Strategy.P2P_CLUSTER

    private val _connectedNodes = MutableStateFlow<List<String>>(emptyList())
    val connectedNodes: StateFlow<List<String>> = _connectedNodes

    private val _networkStatus = MutableStateFlow("Desconectado")
    val networkStatus: StateFlow<String> = _networkStatus

    private val endpoints = mutableSetOf<String>()
    private var isAdvertising = false
    private var isDiscovering = false

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                val message = String(bytes, Charsets.UTF_8)
                LogManager.log("MESH", "Mensaje P2P recibido de $endpointId: $message")
                // Here we would parse and route to Room DB for synced chat or data
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Update transfer progress if needed
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            LogManager.log("MESH", "Conexión iniciada con: ${connectionInfo.endpointName} ($endpointId)")
            // Accept the connection automatically for the mesh
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    endpoints.add(endpointId)
                    _connectedNodes.value = endpoints.toList()
                    _networkStatus.value = "Nodos Conectados: ${endpoints.size}"
                    LogManager.log("MESH", "Conexión P2P establecida: $endpointId")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    LogManager.log("MESH", "Conexión P2P rechazada: $endpointId")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    LogManager.log("MESH", "Error de Conexión P2P: $endpointId")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            endpoints.remove(endpointId)
            _connectedNodes.value = endpoints.toList()
            _networkStatus.value = "Nodos Conectados: ${endpoints.size}"
            LogManager.log("MESH", "Nodo desconectado de la Malla P2P: $endpointId")
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: com.google.android.gms.nearby.connection.DiscoveredEndpointInfo) {
            LogManager.log("MESH", "Nodo descubierto: ${info.endpointName}")
            // Request connection to the discovered node
        }

        override fun onEndpointLost(endpointId: String) {
            LogManager.log("MESH", "Nodo perdido: $endpointId")
        }
    }

    fun startMeshNetwork(context: Context, localNodeName: String) {
        val connectionsClient = Nearby.getConnectionsClient(context)
        
        // Start Advertising
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            localNodeName, SERVICE_ID, connectionLifecycleCallback, advertisingOptions
        ).addOnSuccessListener {
            isAdvertising = true
            _networkStatus.value = "Anunciando Malla P2P"
            LogManager.log("MESH", "Anunciando nodo P2P...")
        }.addOnFailureListener { e ->
            LogManager.log("MESH", "Fallo al iniciar anuncio P2P: ${e.message}")
        }

        // Start Discovering
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(
            SERVICE_ID, endpointDiscoveryCallback, discoveryOptions
        ).addOnSuccessListener {
            isDiscovering = true
            LogManager.log("MESH", "Buscando nodos P2P...")
        }.addOnFailureListener { e ->
            LogManager.log("MESH", "Fallo al buscar nodos P2P: ${e.message}")
        }
    }

    fun stopMeshNetwork(context: Context) {
        val connectionsClient = Nearby.getConnectionsClient(context)
        connectionsClient.stopAllEndpoints()
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        isAdvertising = false
        isDiscovering = false
        endpoints.clear()
        _connectedNodes.value = emptyList()
        _networkStatus.value = "Desconectado"
        LogManager.log("MESH", "Malla P2P detenida")
    }

    fun broadcastMessage(context: Context, message: String) {
        if (endpoints.isEmpty()) return
        val bytes = message.toByteArray(Charsets.UTF_8)
        Nearby.getConnectionsClient(context).sendPayload(
            endpoints.toList(), Payload.fromBytes(bytes)
        )
    }
}
