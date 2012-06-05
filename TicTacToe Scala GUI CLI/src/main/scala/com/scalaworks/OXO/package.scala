package com.scalaworks

/**
 * Package OXO is a container for Model (M), View (V) and Controller (C) Objects and Classes.
 *  As well for the necessary Main objects.
 */
package object OXO {
  def tryo[T](f: => T): Option[T] = try { Some(f) } catch { case _ => None }

  def using[A, B <: { def close(): Unit }](closeable: B)(f: B => A): A =
    try { f(closeable) } finally { closeable.close() }
}