package scrpgHelper.chargen.archetypes

object Speedster:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val speedster = Archetype(
    "Speedster",
    1,
    signaturePower(Power.speed),
    _ => true,
    1,
    List(
      Power.agility,
      Power.intangibility,
      Power.lightningCalculator,
      Power.vitality
    ) ++ Power.mobilityPowers,
    Quality.mentalQualities ++ Quality.physicalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Always on the Move",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using ",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ". Defend yourself using your Min die."
            )
          ),
          AbilityTemplate(
            "Fast Fingers",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder, Action.Attack),
            List(
              "Boost or Hinder using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Max die. If you roll doubles, you may also Attack using your Mid die."
            )
          ),
          AbilityTemplate(
            "Non-stop Assault",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack multiple targets using ",
              QualityChoice(AbilityChoice.noDupes(_)),
              ". Use your Min die. Hinder each target equal to your Mid die."
            )
          )
        )
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Blinding Strike",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack multiple targets using ",
              QualityChoice(),
              ". Hinder each target equal to your Min die."
            )
          ),
          AbilityTemplate(
            "Flurry of Fists",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using ",
              QualityChoice(),
              ". Use your Max die. If you roll doubles, use Max + Min instead."
            )
          ),
          AbilityTemplate(
            "Supersonic Streak",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using ",
              PowerChoice(),
              ". Use your Max die against one target, and your Mid die against each other target. If you roll doubles, take irreducibl damage equal to your Mid die."
            )
          ),
          AbilityTemplate(
            "Speedy Analysis",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost multiple targets using ",
              PowerChoice(),
              ". Use your Max die."
            )
          )

        )
      )
    ),
    PrincipleCategory.Expertise
  )
end Speedster
