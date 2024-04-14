package scrpgHelper.chargen.archetypes

object Marksman:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val marksman = Archetype(
    "Marksman",
    4,
    signaturePower(Power.signatureWeapon),
    _ => true,
    1,
    List(
      Power.signatureVehicle,
      Power.swinging,
    ) ++ Power.athleticPowers ++ Power.intellectualPowers ++ Power.technologicalPowers,
    Quality.informationQualities ++ Quality.mentalQualities ++ Quality.physicalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Dual Wielder",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack two different targets using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ", one target using your Mid die and the other your Min die."
            )
          ),
          AbilityTemplate(
            "Load",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost using",
              PowerChoice(AbilityChoice.noDupes(_)),
              "to create one bonus using your Max die and another using your Mid die."
            )
          ),
          AbilityTemplate(
            "Precise Shot",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ". Ignore all penalties on this Attack, ignore any Defend actions, and it cannot be affected by Reactions."
            )
          ),
          AbilityTemplate(
            "Sniper Aim",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ". Use your Max + Min dice. This bonus can only be used against one chosen target, and is persistent & exclusive against that target until it leaves the scene."
            )
          ),
          AbilityTemplate(
            "Spin & Shoot",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ". Defend using your Min die."
            )
          ),
        )
      ),
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Called Shot",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Boost),
            List(
              "Attack using",
              QualityChoice(AbilityChoice.noDupes(_)),
              ". Boost another hero using your Max die."
            )
          ),
          AbilityTemplate(
            "Exploding Ammo",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Overcome),
            List(
              "Attack or Overcome using",
              QualityChoice(AbilityChoice.noDupes(_)),
              "on an environmental target, using your Max + Min dice. If you roll doubles, take a minor twist."
            )
          ),
          AbilityTemplate(
            "Hair Trigger Reflexes",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Attack),
            List(
              "When a new target enters close range, Attack that target by rolling your single",
              QualityChoice(AbilityChoice.noDupes(_)),
              "die."
            )
          ),
          AbilityTemplate(
            "Ricochet",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              QualityChoice(AbilityChoice.noDupes(_)),
              ". Use your Max die. If you roll doubles, use Max + Min instead."
            )
          )
        ),
      )
    ),
    PrincipleCategory.Responsibility
  )
end Marksman
