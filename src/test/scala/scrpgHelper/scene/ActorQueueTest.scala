package scrpgHelper.scene

class ActorQueueTest extends munit.FunSuite:
    import ActorQueue.*

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
      assert(q.acted == List())
      assert(q.remaining == List("a", "b", "c"))

      val q2 = q.advanceQueue("a")
      assert(q2.flatMap(_.acting) == Some("a"))
      assert(q2.map(_.acted) == Some(List()))
      assert(q2.map(_.remaining) == Some(List("b", "c")))

      val qNot = q2.flatMap(_.advanceQueue("a"))
      assert(qNot == q2)

      val q3 = q2.flatMap(_.advanceQueue("b"))
      assert(q3.flatMap(_.acting) == Some("b"))
      assert(q3.map(_.acted) == Some(List("a")))
      assert(q3.map(_.remaining) == Some(List("c")))

      val q4 = q3.flatMap(_.advanceQueue("c"))
      assert(q4.flatMap(_.acting) == Some("c"))
      assert(q4.map(_.acted) == Some(List("a", "b")))
      assert(q4.map(_.remaining) == Some(List()))

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

    test("replaces actor") {
      val q = ActorQueue(List("a","b","c"))
      assert(q.replaceActor("a", "z").totalActors == Set("z", "b", "c"))
      assert(q.advanceQueue("a").map(_.replaceActor("a", "z").totalActors) == Some(Set("z", "b", "c")))
      assert(q.advanceQueue("a").flatMap(_.advanceQueue("b")).map(_.replaceActor("a", "z").totalActors) == Some(Set("z", "b", "c")))
    }

    test("updates actor") {
      val q = ActorQueue(List("a","b","c"))
      assert(q.updateActor("a", s => Some(s*2)).map(_.totalActors) == Some(Set("aa", "b", "c")))
      assert(q.updateActor("a", s => None).map(_.totalActors) == None)
      assert(q.advanceQueue("a").flatMap(_.updateActor("a", s => Some(s*2))).map(_.totalActors) == Some(Set("aa", "b", "c")))
      assert(q.advanceQueue("a").flatMap(_.advanceQueue("b")).flatMap(_.updateActor("a", s => Some(s*2))).map(_.totalActors) == Some(Set("aa", "b", "c")))
    }
end ActorQueueTest
