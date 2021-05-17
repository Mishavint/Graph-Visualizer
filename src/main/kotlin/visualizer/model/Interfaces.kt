package visualizer.model

interface Vertex {
    var element: String
}

interface Edge {
    var element: String
    val vertices: Pair<Vertex, Vertex>
}

interface Graph {
    fun vertices(): MutableCollection<Vertex>
    fun edges(): MutableCollection<Edge>
    fun listOfAdjacency(): HashMap<Vertex, ArrayList<Vertex>>

    fun addVertex(v: String): Vertex
    fun addEdge(u: String, v: String, e: String): Edge
}