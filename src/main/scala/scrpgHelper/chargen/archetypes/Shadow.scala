package scrpgHelper.chargen.archetypes

object Shadow:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val shadow = Archetype(
    "Shadow",
    2,
    _ => true,
    signatureQuality(Quality.stealth),
    1,
    List(
      Power.intangibility,
      Power.invisibility,
      Power.signatureWeapon
    ) ++ Power.athleticPowers,
    Quality.physicalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Sabotage",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ". Remove one physical bonus or penalty. Hinder a target using your Min die, or maneuver to a new location in your environment."
            )
          ),
          AbilityTemplate(
            "Shadowy Figure",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ". Defend using your Min die against all Attacks until your next turn."
            )
          ),
          AbilityTemplate(
            "Untouchable",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Attack),
            List(
              s"When you would be dealt damage, roll a ${d(4)} while in the Green zone, ${d(6)} while in the Yellow, or ${d(8)} while in Red. Reduce the damage you take by the value rolled. Attack another target with that roll."
            )
          )
        )
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Overcome From the Darkness",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Overcome, Action.Attack, Action.Boost),
            List(
              "Attack or Overcome using",
              PowerQualityChoice(),
              ". Boost yourself using your Min die."
            )
          ),
          AbilityTemplate(
            "Diversion",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When you would take damage, Defend against that damage by rolling your single",
              PowerQualityChoice(),
              "die."
            )
          ),
        )
      )
    ),
    PrincipleCategory.Expertise
  )
end Shadow
