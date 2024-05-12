package scrpgHelper.chargen.archetypes

object Sorcerer:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val sorcerer = Archetype(
    "Sorcerer",
    11,
    _ => true,
    _ => true,
    1,
    Power.energyPowers ++ Power.materialPowers ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    Quality.informationQualities ++ Quality.mentalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Banish",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Hinder, Action.Attack),
            List(
              "Hinder using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. If you roll doubles, also Attack using your Mid die."
            )
          ),
          AbilityTemplate(
            "Energy Jaunt",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using",
              PowerChoice(AbilityChoice.noDupes),
              ", applying your Min die against each."
            )
          ),
          AbilityTemplate(
            "Powerful Blast",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              "and use your Max die."
            )
          ),
          AbilityTemplate(
            "Subdue",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Hinder the same target using your Min die."
            )
          ),
        )
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Cords of Magic",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Hinder),
            List(
              "Destory all bonuses and penalties on a target. Then, Hinder that target using",
              PowerChoice(),
              ", using your Max die."
            )
          ),
          AbilityTemplate(
            "Field of Energy",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets near each other using",
              PowerChoice(),
              "."
            )
          ),
          AbilityTemplate(
            "Living Bomb",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Destroy one d6 or d8 minion. Roll that minion's die as an Attack against another target."
            )
          ),
        )
      )
    ),
    PrincipleCategory.Esoteric
  )
end Sorcerer
