package scrpgHelper.chargen.powers

object HigherPower:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val higherPower: PowerSource = PowerSource(
    "Higher Power",
    19,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Command Power",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When you take damage from ",
              EnergyChoice(),
              ", you may deal that much damage to another target."
            ),
          ),
          AbilityTemplate(
            "Dangerous Explosion",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack multiple targets using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Mid die. Hinder all targets damaged by this ability with your Min die. Hinder yourself with your Max die.",
            ),
          ),
          AbilityTemplate(
            "Embolden",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Boost),
            List(
              "Attack using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ", and Boost all nearby heroes taking ",
              ActionChoice(AbilityChoice.noDupes(_)),
              " or ",
              ActionChoice(AbilityChoice.noDupes(_)),
              " using your Min die until your next turn."
            ),
          ),
          AbilityTemplate(
            "Resolve",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Recover),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ", then remove a penalty on yourself or Recover using your Min die.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Resilience",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "At the start of your turn, remove any -1 penalties on you."
            ),
          ),
          AbilityTemplate(
            "Twist Reality",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After rolling during your turn, you may take 1 irreducible damage to reroll your entire dice pool."
            ),
          )
        ),
      )
    ),
    Power.athleticPowers ++ Power.energyPowers ++ Power.materialPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(10), d(8), d(8))
  )
end HigherPower
