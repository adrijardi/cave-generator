import scala.util.Random

object CaveGenerator {

  def create(size: (Int, Int), smoothSteps: Int): Vector[Vector[Boolean]] = {
    val smoothed =
      (1 to smoothSteps).foldLeft(rnd(size))((prev, _) => smooth(prev))
    val walled = wallBorders(smoothed)
    update(walled)(_._3)
  }

  private def rnd(size: (Int, Int)): Vector[Vector[(Int, Int, Boolean)]] =
    (0 until size._1).map { x =>
      (0 until size._2).map { y =>
        (x, y, Random.nextBoolean())
      }.toVector
    }.toVector

  private type UpdateFn[O] = ((Int, Int, Boolean)) => O

  private def smooth(
    cave: Vector[Vector[(Int, Int, Boolean)]]
  ): Vector[Vector[(Int, Int, Boolean)]] =
    update(cave) { elem =>
      val newValue = neighbours(cave, (elem._1, elem._2))
        .map(_._3)
        .count(identity) + Random.nextInt(2) > 4
      elem.copy(_3 = newValue)
    }

  private def update[O](cave: Vector[Vector[(Int, Int, Boolean)]])(fn: UpdateFn[O]) =
    cave.map(_.map(fn))

  private def wallBorders(
    cave: Vector[Vector[(Int, Int, Boolean)]]
  ): Vector[Vector[(Int, Int, Boolean)]] =
    update(cave) { elem =>
      if (elem._1 == 0 || elem._2 == 0 || elem._1 == cave.length - 1 || elem._2 == cave.head.length - 1)
        elem.copy(_3 = true)
      else elem
    }

  private def neighbours(cave: Vector[Vector[(Int, Int, Boolean)]], pos: (Int, Int)): Vector[(Int, Int, Boolean)] = {
    def trimmedRange(origin: Int, max: Int): Vector[Int] =
      (origin - 1 to origin + 1).filter(x => x >= 0 && x < max).toVector

    trimmedRange(pos._1, cave.length).flatMap { x =>
      trimmedRange(pos._2, cave.head.length).flatMap { y =>
        if ((x, y) == pos) Vector.empty
        else Vector(cave(x)(y))
      }
    }
  }

}
