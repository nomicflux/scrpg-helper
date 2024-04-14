package scrpgHelper.chargen.archetypes

object WildCard:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val wildCard = Archetype(
    "Wild Card",
    15,
    _ => true,
    _ => true,
    1,
    List(
      Power.signatureVehicle,
      Power.signatureWeapon,
    ) ++ Power.athleticPowers ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.selfControlPowers,
    Quality.physicalQualities ++ Quality.socialQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Gimmick",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Boost, Action.Hinder),
            List(
              "Boost or Hinder using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your MAx die. If you roll doubles, you may also Attack using your Mid die."
            )
          ),
          AbilityTemplate(
            "Multitask",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder, Action.Defend, Action.Attack, Action.Overcome),
            List(
              "Take any two different basic actions using",
              PowerQualityChoice(AbilityChoice.noDupes(_)),
              ", each using your Min die."
            )
          ),
          AbilityTemplate(
            "Surprise Results",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After rolling your dice pool for the turn, you may take 1 irreducible damage to reroll your entire pool."
            )
          ),
          AbilityTemplate(
            "Unknown Results",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder, Action.Defend, Action.Attack, Action.Overcome),
            List(
              "Take any basic action using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Then roll a d6. On 1, Boost with your Min die. On 2, Hinder with your Min die. On 3, Defend with your Min die. On 4, lose Health equal to your Min die. On 5, your basic action uses your Max die. On 6, your basic action uses your Min die."
            )
          ),
        )
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Break the 4th",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "You may uncheck a checkoud off collection on your hero sheet.",
            )
          ),
          AbilityTemplate(
            "Danger!",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using",
              PowerChoice(),
              ". If you roll doubles, one nearby ally is also hit with the Attack."
            )
          ),
          AbilityTemplate(
            "Expect the Unexpected",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "Apply a bonus after rolling your action, instead of before.",
            )
          ),
          AbilityTemplate(
            "Imitation",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(),
            List(
              "Use a Green action ability of a nearby ally (using the same size power/quality die they would use)."
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
    PrincipleCategory.Ideals
  )
end WildCard
