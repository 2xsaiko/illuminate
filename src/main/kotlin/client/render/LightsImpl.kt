package therealfarfetchd.illuminate.client.render

import net.minecraft.client.MinecraftClient
import therealfarfetchd.illuminate.client.api.Light
import therealfarfetchd.illuminate.client.api.Lights
import therealfarfetchd.illuminate.client.postProcess

object LightsImpl : Lights {

  private val pp
    get() = MinecraftClient.getInstance().gameRenderer.postProcess

  private val lights
    get() = pp.lights.keys

  override val size: Int
    get() = lights.size

  override fun add(element: Light): Boolean {
    if (element in pp.lights) return false
    pp.lights[element] = LightContainer(element)
    return true
  }

  override fun addAll(elements: Collection<Light>): Boolean {
    return elements.map(this::add).all { it }
  }

  override fun contains(element: Light): Boolean = lights.contains(element)

  override fun containsAll(elements: Collection<Light>): Boolean = lights.containsAll(elements)

  override fun isEmpty(): Boolean = lights.isEmpty()

  override fun clear() = lights.clear()

  override fun iterator(): MutableIterator<Light> = lights.iterator()

  override fun remove(element: Light): Boolean = lights.remove(element)

  override fun removeAll(elements: Collection<Light>): Boolean = lights.removeAll(elements)

  override fun retainAll(elements: Collection<Light>): Boolean = lights.retainAll(elements)

}