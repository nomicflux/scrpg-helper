package scrpgHelper.chargen.archetypes

object Gadgeteer:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val gadgeteer = Archetype(
    "Gadgeteer",
    17,
    signaturePowerCategory(PowerCategory.Intellectual),
    _ => true,
    1,
    List(
      Power.signatureVehicle,
      Power.signatureWeapon
    ) ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.technologicalPowers,
    Quality.informationQualities ++ Quality.mentalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Analyze Probabilites",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After rolling your dice pool, you may take 1 irreducible damage to reroll your dice pool.",
            )
          ),
          AbilityTemplate(
            "Analyze Weakness",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Hinder),
            List(
              "Hinder using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Max die, or use your Mid die and make it persistent and exclusive."
            )
          ),
          AbilityTemplate(
            "Equip",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Make one bonus for one ally using your Mid die and another for another ally using your Min die."
            )
          ),
          AbilityTemplate(
            "Helpful Invention",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Max die, or use your Mid die and make it persistent and exclusive."
            )
          ),
        )
      ),
      AbilityPool(
        1,
        List(
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
            "Snap Decision",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Attack),
            List(
              "Boost yourself using",
              PowerChoice(),
              ". Use your Max + Min dice. Then Attack using your Mid die with that bonus."
            )
          ),
          AbilityTemplate(
            "Turn the Tables",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(),
            List(
              "Change any bonus into a penalty of equal size or vice versa."
            )
          ),
        )
      )
    ),
    PrincipleCategory.Identity
  )
end Gadgeteer
