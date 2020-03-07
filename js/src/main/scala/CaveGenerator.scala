import scala.annotation.tailrec
import scala.util.{Random, Try}

object CaveGenerator {

  def create(
    size: (Int, Int),
    smoothSteps: Int,
    wallsMargin: Int,
    minRoomSize: Int,
    createPassagesFlag: Boolean,
  ): Vector[Vector[Boolean]] = {
    val smoothed =
      (1 to smoothSteps).foldLeft(rnd(size))((prev, _) => smooth(prev))
    val walled = wallBorders(smoothed, wallsMargin)

    val rooms = detectRooms(walled)

    val culledRooms = rooms.filter(_.tiles.size < minRoomSize)

    val keptRooms = rooms -- culledRooms

    val culledMap = culledRooms.flatMap(_.tiles).foldLeft(walled) {
      case (rooms, remove) =>
        rooms.updated(remove._1, rooms(remove._1).updated(remove._2, (remove._1, remove._2, true)))
    }

    if (createPassagesFlag && keptRooms.nonEmpty) {

      val passages = createPassages(keptRooms)

      val newPassagePos = passages.passages.flatMap(_.tiles)
      update(culledMap) {
        case (x, y, v) =>
          if (newPassagePos.contains((x, y)))
            false
          else
            v
      }
    } else {
      update(culledMap)(_._3)
    }
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
    cave: Vector[Vector[(Int, Int, Boolean)]],
    wallsMargin: Int
  ): Vector[Vector[(Int, Int, Boolean)]] =
    update(cave) { elem =>
      if (elem._1 < wallsMargin || elem._2 < wallsMargin || elem._1 > cave.length - 1 - wallsMargin || elem._2 > cave.head.length - 1 - wallsMargin)
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

  final case class Room(tiles: Set[(Int, Int)], borders: Set[(Int, Int)], center: (Int, Int))
  final case class FloodFillRoom(tiles: Set[(Int, Int)], borders: Set[(Int, Int)])
  final case class Passage(tiles: Set[(Int, Int)], borders: Set[(Int, Int)])

  private def detectRooms(cave: Vector[Vector[(Int, Int, Boolean)]]): Set[Room] = {
    val roomSpace = cave.flatten.collect {
      case (x, y, false) => (x, y)
    }.toSet

    roomSpace
      .foldLeft(Set.empty[Room]) { (rooms, space) =>
        if (!rooms.exists(_.tiles.contains(space)))
          rooms + floodFill(cave, space)
        else
          rooms
      }
  }

  /**
   * https://en.wikipedia.org/wiki/Flood_fill
   * Not very efficient
   */
  private def floodFill(cave: Vector[Vector[(Int, Int, Boolean)]], toCheck: (Int, Int)): Room = {

    def isWall(pos: (Int, Int)) = Try(cave(pos._1)(pos._2)).fold(_ => true, _._3)

    @tailrec
    def floodFillRec(currentRoom: FloodFillRoom, toCheck: List[(Int, Int)]): FloodFillRoom =
      toCheck match {
        case (pos @ (x, y)) :: rest if !isWall(x, y) && !currentRoom.tiles.contains(pos) =>
          val neighbours  = squareNeighbours(pos)
          val newBorders  = currentRoom.borders ++ (if (neighbours.exists(isWall)) Set(pos) else Set.empty)
          val updatedRoom = FloodFillRoom(currentRoom.tiles + pos, newBorders)
          floodFillRec(updatedRoom, neighbours ++ rest)
        case _ :: Nil  => currentRoom
        case _ :: rest => floodFillRec(currentRoom, rest)
      }

    def squareNeighbours(pos: (Int, Int)): List[(Int, Int)] =
      List((pos._1 - 1, pos._2), (pos._1 + 1, pos._2), (pos._1, pos._2 - 1), (pos._1, pos._2 + 1))

    val result    = floodFillRec(FloodFillRoom(Set.empty, Set.empty), List(toCheck))
    val centerSum = result.borders.reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    val center    = (centerSum._1 / result.borders.size, centerSum._2 / result.borders.size)
    Room(result.tiles, result.borders, center)
  }

  case class CaveMap(rooms: Set[Room], passages: Set[Passage])

  private def createPassages(rooms: Set[Room]): CaveMap = {

    @tailrec
    def createPassagesRec(
      connected: Set[Room],
      disconnected: Set[Room],
      passages: Set[Passage],
      distances: List[RoomDistance]
    ): Set[Passage] =
      if (disconnected.isEmpty)
        passages
      else {
        // Find closest rooms where one is connected
        val closestOp = distances.find(distance => connected.contains(distance.a) || connected.contains(distance.b))

        if (closestOp.isDefined) {
          val closest = closestOp.get

          // If both are connected discard
          if (connected.contains(closest.a) && connected.contains(closest.b)) {
            createPassagesRec(connected, disconnected, passages, distances.filterNot(_ == closest))
          } else {
            val points     = closestPoints(closest.a, closest.b)
            val newPassage = passageTiles(points.fromPos, points.toPos)
            createPassagesRec(
              connected + closest.a + closest.b,
              disconnected - closest.a - closest.b,
              passages + Passage(newPassage, newPassage),
              distances.filterNot(_ == closest)
            )
          }
        } else {
          println("Error creating passages")
          Set.empty[Passage]
        }
      }

    // TODO cleanup?
    final case class ClosestMatch(fromRoom: Room, fromPos: (Int, Int), toRoom: Room, toPos: (Int, Int), distance: Int)

    def closestPoints(xa: Room, xb: Room): ClosestMatch = {
      val combinations = for {
        a        <- xa.borders
        b        <- xb.borders
        distance = getDistance(a, b)
      } yield ClosestMatch(xa, a, xb, b, distance)
      combinations.minBy(_.distance)
    }

    def getDistance(a: (Int, Int), b: (Int, Int)): Int = Math.abs(a._1 - b._1) + Math.abs(a._2 - b._2)

    def passageTiles(fromPos: (Int, Int), toPos: (Int, Int)): Set[(Int, Int)] = {

      @tailrec
      def passageTilesRec(fromPos: (Int, Int), toPos: (Int, Int), passageTilesSet: Set[(Int, Int)]): Set[(Int, Int)] =
        if (fromPos == toPos)
          passageTilesSet + fromPos
        else {
          val nextTile: (Int, Int) =
            if (Math.abs(toPos._1 - fromPos._1) > Math.abs(toPos._2 - fromPos._2)) {
              (if (fromPos._1 < toPos._1) fromPos._1 + 1 else fromPos._1 - 1, fromPos._2)
            } else {
              (fromPos._1, if (fromPos._2 < toPos._2) fromPos._2 + 1 else fromPos._2 - 1)
            }
          passageTilesRec(nextTile, toPos, passageTilesSet + fromPos)
        }

      passageTilesRec(fromPos, toPos, Set.empty)
    }

    final case class RoomDistance(a: Room, b: Room, distance: Int)

    val roomDistances =
      rooms.toList
        .combinations(2)
        .map {
          case roomA :: roomB :: Nil =>
            val distance = getDistance(roomA.center, roomB.center)
            RoomDistance(roomA, roomB, distance)
        }
        .toList
        .sortBy(_.distance)

    val passages = createPassagesRec(Set(rooms.head), rooms.drop(1), Set.empty, roomDistances)
    CaveMap(rooms, passages)
  }

}
