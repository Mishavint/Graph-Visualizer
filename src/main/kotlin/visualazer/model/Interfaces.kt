package visualazer.model

interface Vertex {
    var element: String
}

interface Edge {
    var element: String
    val vertexes: Pair<Vertex, Vertex>

    fun incident(v: Vertex) = v == vertexes.first || v == vertexes.second
}

interface Graph {
    fun vertexes(): MutableCollection<Vertex>
    fun edges(): MutableCollection<Edge>

    fun addVertex(v: String): Vertex
    fun addEdge(u: String, v: String, e: String): Edge
}