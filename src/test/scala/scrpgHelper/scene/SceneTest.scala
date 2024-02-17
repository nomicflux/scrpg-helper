package scrpgHelper.scene

class SceneTest extends munit.FunSuite:
    import Scene.*
    import scrpgHelper.status.Status

    test("totalSpaces") {
      assert(Scene(1,2,2).totalSpaces == 5)
      assert(Scene(3,2,2).totalSpaces == 7)
      assert(Scene(1,5,2).totalSpaces == 8)
      assert(Scene(1,2,7).totalSpaces == 10)
    }

    test("advanceTracker and currentStatus") {
      val scene1 = Scene(1,2,1)
      assert(scene1.position == 1)
      assert(scene1.currentStatus == Status.Green)

      val scene2 = scene1.advanceTracker
      assert(scene2.map(_.position) == Some(2))
      assert(scene2.map(_.currentStatus) == Some(Status.Yellow))

      val scene3 = scene2.flatMap(_.advanceTracker)
      assert(scene3.map(_.position) == Some(3))
      assert(scene3.map(_.currentStatus) == Some(Status.Yellow))

      val scene4 = scene3.flatMap(_.advanceTracker)
      assert(scene4.map(_.position) == Some(4))
      assert(scene4.map(_.currentStatus) == Some(Status.Red))

      val scene5 = scene4.flatMap(_.advanceTracker)
      assert(scene5.isEmpty)
    }

    test("advance scene") {
      val scene1 = Scene(1,2,1,List("a", "b", "c"))
      assert(scene1.position == 1)

      val scene2 = scene1.advanceScene("a")
      assert(scene2.map(_.position) == Some(1))
      assert(scene2.map(_.actorQueue.acted) == Some(List()))
      assert(scene2.map(_.actorQueue.acting) == Some(Some("a")))
      assert(scene2.map(_.actorQueue.remaining) == Some(List("b", "c")))

      val sceneNot = scene2.flatMap(_.advanceScene("a"))
      assert(sceneNot == scene2)

      val scene3 = scene2.flatMap(_.advanceScene("b"))
      assert(scene3.map(_.position) == Some(1))
      assert(scene3.map(_.actorQueue.acted) == Some(List("a")))
      assert(scene3.map(_.actorQueue.acting) == Some(Some("b")))
      assert(scene3.map(_.actorQueue.remaining) == Some(List("c")))

      val scene4 = scene3.flatMap(_.advanceScene("c"))
      assert(scene4.map(_.position) == Some(1))
      assert(scene4.map(_.actorQueue.acted) == Some(List("a", "b")))
      assert(scene4.map(_.actorQueue.acting) == Some(Some("c")))
      assert(scene4.map(_.actorQueue.remaining) == Some(List()))

      val sceneActing = scene4.flatMap(_.advanceScene("c"))
      assert(sceneActing == scene4)

      val scene5 = scene4.flatMap(_.advanceScene("b"))
      assert(scene5.map(_.position) == Some(2))
      assert(scene5.map(_.actorQueue.acted) == Some(List()))
      assert(scene5.map(_.actorQueue.acting) == Some(Some("b")))
      assert(scene5.map(_.actorQueue.remaining) == Some(List("a", "c")))
    }

    test("status boxes") {
      val scene = Scene(1,2,2)
      assert(scene.statusBoxes == List(Status.Green, Status.Yellow, Status.Yellow, Status.Red, Status.Red))
    }

    test("add and remove actors") {
      val scene = Scene[String](1,2,1)
      assert(scene.addActor("a").actorQueue.totalActors == Set("a"))
      assert(scene.addActor("a").addActor("b").actorQueue.totalActors == Set("a", "b"))
      assert(scene.addActor("a").addActor("b").removeActor("a").actorQueue.totalActors == Set("b"))
    }

    test("updates status") {
      val scene = Scene[String](1,1,1).copy(position = 3)
      val moreGreen = scene.updateGreen(2)
      assert(moreGreen.map(_.green) == Some(2))
      assert(moreGreen.map(_.yellow) == Some(1))
      assert(moreGreen.map(_.red) == Some(1))

      val moreYellow = scene.updateYellow(2)
      assert(moreYellow.map(_.green) == Some(1))
      assert(moreYellow.map(_.yellow) == Some(2))
      assert(moreYellow.map(_.red) == Some(1))

      val moreRed = scene.updateRed(2)
      assert(moreRed.map(_.green) == Some(1))
      assert(moreRed.map(_.yellow) == Some(1))
      assert(moreRed.map(_.red) == Some(2))

      val lessGreen = scene.updateGreen(0)
      assert(lessGreen.isEmpty)

      val lessYellow = scene.updateYellow(0)
      assert(lessYellow.isEmpty)

      val lessRed = scene.updateRed(0)
      assert(lessRed.isEmpty)
    }
end SceneTest
