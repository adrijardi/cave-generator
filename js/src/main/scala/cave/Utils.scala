package cave

object Utils {

  def slidePairs[A](seq: Seq[A]): Vector[(A, A)] =
    seq
      .sliding(2)
      .collect {
        case it if it.length == 2 => (it.head, it(1))
      }
      .toVector
}
