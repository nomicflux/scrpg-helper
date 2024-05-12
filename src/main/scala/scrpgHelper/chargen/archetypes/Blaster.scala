package scrpgHelper.chargen.archetypes

object Blaster:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val blaster = Archetype(
    "Blaster",
    5,
    signaturePowerCategory(PowerCategory.Energy),
    _ => true,
    1,
    List(
      Power.signatureWeapon,
    ) ++ Power.energyPowers ++ Power.mobilityPowers ++ Power.technologicalPowers,
    Quality.mentalQualities ++ Quality.physicalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Exploit Vulnerability",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". If you Attacked or Hindered that target in your previous turn, use your Max die in this Attack."
            )
          ),
          AbilityTemplate(
            "Disabling Blast",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Hinder using your Min die."
            )
          ),
          AbilityTemplate(
            "Danger Zone",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Min die against each."
            )
          ),
          AbilityTemplate(
            "Precise Hit",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Ignore all penalties on this Attack, ignore any Defend actions, and it cannot be affected by Reactions."
            )
          ),
        )
      ),
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Energy Immunity",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(Action.Recover),
            List(
              "If you would take damage from",
              EnergyChoice(),
              ", instead reduce that damage to 0 and Recover that amount of Health."
            )
          ),
          AbilityTemplate(
            "Heedless Blast",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Mid die against each target. Take irreducible damage equal to your Mid die."
            )
          ),
          AbilityTemplate(
            "Imbue with Element",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. If you choose another hero to go next, Boost that hero using your Mid die."
            )
          ),
        ),
      )
    ),
    PrincipleCategory.Esoteric
  )
end Blaster
