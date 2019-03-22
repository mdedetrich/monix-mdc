package org.mdedetrich.monix.mdc

import monix.execution.misc.Local
import ch.qos.logback.classic.util.LogbackMDCAdapter

import java.{util => ju}

// Shamelessly copied from https://olegpy.com/better-logging-monix-1/

class MonixMDCAdapter extends LogbackMDCAdapter {
  private[this] val map = Local[ju.Map[String, String]](ju.Collections.emptyMap())

  override def put(key: String, `val`: String): Unit = {
    if (map() eq ju.Collections.EMPTY_MAP) {
      map := new ju.HashMap()
    }
    map().put(key, `val`)
    ()
  }

  override def get(key: String): String = map().get(key)
  override def remove(key: String): Unit = {
    map().remove(key)
    ()
  }

  // Note: we're resetting the Local to default, not clearing the actual hashmap
  override def clear(): Unit                               = map.clear()
  override def getCopyOfContextMap: ju.Map[String, String] = new ju.HashMap(map())
  override def setContextMap(contextMap: ju.Map[String, String]): Unit =
    map := new ju.HashMap(contextMap)

  override def getPropertyMap: ju.Map[String, String] = map()
  override def getKeys: ju.Set[String]                = map().keySet()
}

object MonixMDCAdapter {

  /**
    * Initializes the [[MonixMDCAdapter]] by overriding the default MDCAdaptor. Typically
    * you would call this once in your Main (or equivalent).
    *
    * NOTE: This will override the default MDCAdaptor which means that MDC will no longer
    * propagate via [[ThreadLocal]]
    */
  def initialize(): Unit = {
    import org.slf4j.MDC
    val field = classOf[MDC].getDeclaredField("mdcAdapter")
    field.setAccessible(true)
    field.set(null, new MonixMDCAdapter)
  }
}
