package com.scalaworks.OXO
package classpath

import java.net.URLStreamHandler
import java.net.URL

class Handler(val classLoader: ClassLoader = getClass().getClassLoader()) extends URLStreamHandler {
  /** The classloader to find resources from. */

  override def openConnection(u: URL) = {
    val resourceUrl = classLoader.getResource(u.getPath());
    resourceUrl.openConnection()
  }
}