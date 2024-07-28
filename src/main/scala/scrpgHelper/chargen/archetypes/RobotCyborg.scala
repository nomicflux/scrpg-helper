package scrpgHelper.chargen.archetypes

object RobotCyborg:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val abilities = List(
          AbilityTemplate(
            "Adaptive Programming",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Defend),
            List(
              "Boost yourself using",
              PowerChoice(AbilityChoice.noDupes),
              ", and Defend with your Min die."
            )
          ),
          AbilityTemplate(
            "Living Arsenal",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              "with a bonus equal to the number of bonuses you currently have."
            )
          ),
          AbilityTemplate(
            "Metal Skin",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Reduce the amount of physical damage taken by 1 while you are in the Green zone, 2 while in the Yellow zone, and 3 while in the Red zone.",
            )
          ),
          AbilityTemplate(
            "Self-Improvement",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using",
              PowerChoice(AbilityChoice.noDupes),
              ". That bonus is persistent and exclusive."
            )
          ),
          AbilityTemplate(
            "Something for Everyone",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Mid die to Attack one extra target for each bonus you have. Apply a different bonus to each Attack."
            )
          ),
        )

  val robotCyborg = Archetype(
    "Robot/Cyborg",
    10,
    _ => true,
    _ => true,
    1,
    List(
      Power.signatureWeapon,
      Power.signatureVehicle,
    ) ++ Power.athleticPowers ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.selfControlPowers ++ Power.technologicalPowers,
    Quality.informationQualities ++ Quality.mentalQualities,
    List(AbilityPool(2, abilities),
         AbilityPool(1, abilities.map(_.copy(status = Status.Yellow)))),
    PrincipleCategory.Expertise
  ).withExtraPowers(Power.technologicalPowers.map(p => (p, d(10))))
    .withExtraHealthCategories(List(PowerCategory.Athletic, QualityCategory.Mental))
end RobotCyborg
