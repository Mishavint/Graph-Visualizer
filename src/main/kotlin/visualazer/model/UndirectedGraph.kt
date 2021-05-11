package visualazer.model

internal class UndirectedGraph: Graph {
    private val vertexes = hashMapOf<String, Vertex>()
    private val edges = hashMapOf<String, Edge>()
    private val listOfAdjacency = hashMapOf<Vertex, ArrayList<Vertex>>()

    override fun vertexes(): MutableCollection<Vertex> = vertexes.values

    override fun edges(): MutableCollection<Edge> = edges.values

    override fun addVertex(v: String): Vertex {
        val u = UndirectedVertex(v)
        if (!listOfAdjacency.containsKey(u))
            listOfAdjacency[u] = arrayListOf()
        return vertexes.getOrPut(v) { u }
    }

    override fun addEdge(u: String, v: String, e: String): Edge {
        val first = addVertex(u)
        val second = addVertex(v)
        if (!listOfAdjacency[first]!!.contains(second))
            listOfAdjacency[first]!!.add(second)
        if (!listOfAdjacency[second]!!.contains(first))
            listOfAdjacency[second]!!.add(first)
        return edges.getOrPut(e) { UndirectedEdge(e, first, second) }
    }

    private data class UndirectedVertex(override var element: String): Vertex

    private data class UndirectedEdge(
        override var element: String,
        var first: Vertex,
        var second: Vertex,
    ): Edge {
        override val vertexes
            get() = first to second
    }
}