package scrpgHelper.chargen.archetypes

object RealityShaper:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val realityShaper = Archetype(
    "Reality Shaper",
    18,
    _ => true,
    _ => true,
    1,
    List(
      Power.densityControl,
      Power.intangibility,
      Power.invisibility,
      Power.speed,
      Power.teleportation,
      Power.transmutation,
    ) ++ Power.intellectualPowers ++ Power.psychicPowers ++ Power.technologicalPowers,
    Quality.informationQualities ++ Quality.mentalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Negative Likelihood",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Hinder),
            List(
              "Hinder using",
              PowerChoice(AbilityChoice.noDupes),
              ". That penalty is persistent and exclusive."
            )
          ),
          AbilityTemplate(
            "Not Quite Right",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After a dice pool is rolled, adjust one die up or down one value on the die."
            )
          ),
          AbilityTemplate(
            "Probability Insight",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Attack),
            List(
              "Boost using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. If you roll doubles, you may also Attack using your Mid die."
            )
          ),
          AbilityTemplate(
            "Warp Space",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". You may move the target of that Attack anywhere else nearby. If the target goes next, you decide who takes the next turn after that."
            )
          ),
        )
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Alternate Outcome",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When a nearby enemy rolls their dice pool for the turn, you may los 1 Health to reroll their entire pool."
            )
          ),
          AbilityTemplate(
            "Helpful Analysis",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "One nearby ally may reroll their dice pool. You lose Health equal to the Min die of the new roll."
            )
          ),
          AbilityTemplate(
            "Never Happened",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When a nearby enemy would create a bonus or penalty, you may remove it immediately."
            )
          ),
          AbilityTemplate(
            "Retroactive Rewrite",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When a nearby enemy would create a bonus or penalty, you may remove it immediately."
            )
          ),
        )
      )
    ),
    PrincipleCategory.Expertise
  )
end RealityShaper
