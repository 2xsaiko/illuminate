package therealfarfetchd.illuminate.client.api

/**
 * The main API to manage lights. Allows you to add/remove/iterate over currently present lights.
 * Lights will be automatically cleared whenever a new world is loaded.
 */
interface Lights : MutableCollection<Light> {

  companion object : Lights {
    var Instance: Lights = Dummy
      @JvmStatic get
      @JvmSynthetic internal set

    override val size: Int
      get() = Instance.size

    override fun contains(element: Light): Boolean = Instance.contains(element)

    override fun containsAll(elements: Collection<Light>): Boolean = Instance.containsAll(elements)

    override fun isEmpty(): Boolean = Instance.isEmpty()

    override fun add(element: Light): Boolean = Instance.add(element)

    override fun addAll(elements: Collection<Light>): Boolean = Instance.addAll(elements)

    override fun clear() = Instance.clear()

    override fun iterator(): MutableIterator<Light> = Instance.iterator()

    override fun remove(element: Light): Boolean = Instance.remove(element)

    override fun removeAll(elements: Collection<Light>): Boolean = Instance.removeAll(elements)

    override fun retainAll(elements: Collection<Light>): Boolean = Instance.retainAll(elements)
  }

  private object Dummy : Lights {

    override val size: Int = 0

    override fun contains(element: Light): Boolean = false

    override fun containsAll(elements: Collection<Light>): Boolean = false

    override fun isEmpty(): Boolean = true

    override fun add(element: Light): Boolean = false

    override fun addAll(elements: Collection<Light>): Boolean = false

    override fun clear() {}

    override fun iterator(): MutableIterator<Light> = EmptyIterator

    override fun remove(element: Light): Boolean = false

    override fun removeAll(elements: Collection<Light>): Boolean = false

    override fun retainAll(elements: Collection<Light>): Boolean = false

    private object EmptyIterator : MutableIterator<Light> {
      override fun hasNext(): Boolean = false

      override fun next(): Light = throw NoSuchElementException()

      override fun remove() {}
    }

  }

}