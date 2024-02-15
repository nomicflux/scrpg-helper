package scrpgHelper.scene

class ActorQueueTest extends munit.FunSuite:
    import scrpgHelper.scene.ActorQueue.*

    test("adds to queue") {
      val q = ActorQueue[String]()
      assert(q.totalActors.isEmpty)

      val q2 = q.addActor("a")
      assert(q2.totalActors == Set("a"))

      val q3 = q2.addActor("b")
      assert(q3.totalActors == Set("a", "b"))
    }

    test("advances queue") {
      val q = ActorQueue(Seq("a", "b", "c"))
      assert(q.acting == None)
      assert(q.acted == Set())
      assert(q.remaining == Set("a", "b", "c"))

      val q2 = q.advanceQueue("a")
      assert(q2.flatMap(_.acting) == Some("a"))
      assert(q2.map(_.acted) == Some(Set()))
      assert(q2.map(_.remaining) == Some(Set("b", "c")))

      val qNot = q2.flatMap(_.advanceQueue("a"))
      assert(qNot == q2)

      val q3 = q2.flatMap(_.advanceQueue("b"))
      assert(q3.flatMap(_.acting) == Some("b"))
      assert(q3.map(_.acted) == Some(Set("a")))
      assert(q3.map(_.remaining) == Some(Set("c")))

      val q4 = q3.flatMap(_.advanceQueue("c"))
      assert(q4.flatMap(_.acting) == Some("c"))
      assert(q4.map(_.acted) == Some(Set("a", "b")))
      assert(q4.map(_.remaining) == Some(Set()))

      val qActing = q4.flatMap(_.advanceQueue("c"))
      assert(qActing == q4)

      val q5 = q4.flatMap(_.advanceQueue("a"))
      assert(q5.isEmpty)
    }

    test("removes from queue") {
      val q = ActorQueue(Seq("a", "b", "c"))

      val q1 = q.removeActor("b")
      assert(q1.totalActors == Set("a", "c"))

      val q2 = q.advanceQueue("a").map(_.removeActor("a"))
      assert(q2.map(_.totalActors) == Some(Set("b", "c")))

      val q3 = q.advanceQueue("c").flatMap(_.advanceQueue("b")).map(_.removeActor("c"))
      assert(q3.map(_.totalActors) == Some(Set("a", "b")))
    }
end ActorQueueTest
