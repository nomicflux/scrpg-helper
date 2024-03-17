package scrpgHelper.chargen.powers

object Nature:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val nature: PowerSource = PowerSource(
    "Nature",
    6,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Call to the Wild",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(),
            List(
              "Gain a d8 minion. It takes its turn before yours, but goes away at the end of the scene. You may only have one such minion at a time."
            ),
          ),
          AbilityTemplate(
            "Predator's Eye",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Boost),
            List(
              "Attack using ",
              PowerChoice(),
              ". Use your Max+Min dice. Then gain a Boost using your Mid die. The target of the Attack gains a bonus of the same size.",
            ),
          ),
          AbilityTemplate(
            "Wild Strength",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            List(
              "When you defeat a minion, roll that minion's die and Boost yourself using that roll to creat a bonus for your next action."
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Grasping Vine",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Hinder),
            List(
              "Hinder using ",
              PowerChoice(),
              ". Use your Max die. You may split that penalty across multiple nearby targets.",
            ),
          ),
          AbilityTemplate(
            "Natural Weapon",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using ",
              PowerChoice(),
              ". Use your Max die."
            ),
          )
        ),
      )
    ),
    List(Power.animalControl, Power.cold, Power.electricity, Power.fire, Power.flight, Power.leaping, Power.shapeshifting, Power.swimming, Power.swinging, Power.wallCrawling, Power.weather) ++ Power.athleticPowers ++ Power.materialPowers,
    List(d(10), d(8), d(8))
  )
end Nature
