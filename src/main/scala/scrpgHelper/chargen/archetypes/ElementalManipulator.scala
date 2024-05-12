package scrpgHelper.chargen.archetypes

object ElementalManipulator:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val elementalManipulator = Archetype(
    "Elemental Manipulator",
    9,
    signaturePowerCategory(PowerCategory.Energy),
    _ => true,
    1,
    List(
      Power.absorption,
      Power.flight,
      Power.leaping,
      Power.swimming,
      Power.signatureVehicle,
      Power.signatureWeapon,
      Power.transmutation
    ) ++ Power.energyPowers,
    List(Quality.magicalLore, Quality.science) ++ Quality.mentalQualities ++ Quality.physicalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Backlash",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. Take damage equal to your Min die."
            )
          ),
          AbilityTemplate(
            "Energy Conversion",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Defend, Action.Boost),
            List(
              "Defend using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. Boost using your Min die."
            )
          ),
          AbilityTemplate(
            "External Combustion",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack up to two targets using",
              PowerChoice(AbilityChoice.noDupes),
              ". Also take an amount of damage equal to your Min die."
            )
          ),
          AbilityTemplate(
            "Focused Apparatus",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Hinder, Action.Attack),
            List(
              "Hinder using",
              PowerChoice(AbilityChoice.noDupes),
              ". Attack using your Min die. If you are in the Red zone, you may apply the penalty to any number of nearby targets."
            )
          )

        )
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Damage Spike",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
              ". Use your Max + Min dice. Take damage equal to your Mid die."
            )
          ),
          AbilityTemplate(
            "Energy Alignment",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(Action.Recover),
            List(
              "If you would take damage from",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
              ", reduce that damage to 0 and Recover that amount of Health instead."
            )
          ),
          AbilityTemplate(
            "Energy Redirection",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you take damage from",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
              ", you may also inflict that much damage on another target."
            )
          ),
          AbilityTemplate(
            "Live Dangerously",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Attack multiple targets using",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
              ". Take damage equal to your Max die."
            )
          )

        )
      )
    ),
    PrincipleCategory.Esoteric
  )
end ElementalManipulator
