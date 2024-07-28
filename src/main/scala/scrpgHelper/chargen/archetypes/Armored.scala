package scrpgHelper.chargen.archetypes

object Armored:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers = List(
    Power.signatureVehicle,
    Power.signatureWeapon,
  ) ++ Power.athleticPowers ++ Power.intellectualPowers ++ Power.materialPowers ++ Power.mobilityPowers ++ Power.technologicalPowers

  val qualities = Quality.physicalQualities ++ Quality.socialQualities

  val armored = Archetype(
    "Armored",
    7,
    _ => true,
    _ => true,
    1,
    powers,
    qualities,
    List(
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Armored",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Reduce any physical or energy damage you take by 1 while you are in the Green zone, 2 while in the Yellow zone, and 3 while in the Red zone."
            )
          ),
        )
      ),
      AbilityPool(
        3,
        List(
          AbilityTemplate(
            "Deflect",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When you would be dealt damage, you may deal damage to a nearby target equal to the amount reduced by your Armored ability.",
            )
          ),
          AbilityTemplate(
            "Dual Offense",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.nDistinct(2)),
              ". Attack a second target with your Min die.",
            )
          ),
          AbilityTemplate(
            "Living Bulwark",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.nDistinct(2)),
              ". Defend another target with your Min die."
            )
          ),
          AbilityTemplate(
            "Repair",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Recover),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.nDistinct(2)),
              ". Recover Health equal to your Min die."
            )
          ),
          AbilityTemplate(
            "Unstoppable Charge",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerQualityChoice(AbilityChoice.nDistinct(2)),
              ". Ignore all penalties on this Attack, ignore any Defend actions, and it cannot be affected by Reactions."
            )
          ),
       )
      ),
    ),
    PrincipleCategory.Expertise
  ).withExtraHealthCategories(List(PowerCategory.Material, PowerCategory.Technological))
end Armored
