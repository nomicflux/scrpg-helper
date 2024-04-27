package scrpgHelper.chargen.archetypes

object PhysicalPowerhouse:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers = List(
    Power.densityControl,
    Power.leaping,
    Power.signatureWeapon,
    Power.sizeChanging
  ) ++ Power.athleticPowers

  val qualities = Quality.physicalQualities ++ Quality.socialQualities

  val abilities = List(
          AbilityTemplate(
            "Damage Resistant",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Reduce any physical or energy damage you take by 1 while you are in the Green zone, 2 while in the Yellow zone, and 3 while in the Red zone."
            )
          ),
          AbilityTemplate(
            "Frontline Fighting",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". The target of that Attack must take an Attack action against you as its next turn, if possible."
            )
          ),
          AbilityTemplate(
            "Galvanize",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". Apply that bonus to all hero Attack and Overcome actions until the start of your next turn."
            )
          ),
          AbilityTemplate(
            "Power Strike",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              "and use your Max die."
            )
          ),
          AbilityTemplate(
            "Strength in Victory",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Recover),
            List(
              "When you eliminate a minion with an Attack using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ", Recover Health equal to you Min die."
            )
          ),
        )

  val physicalPowerhouse = Archetype(
    "Physical Powerhouse",
    3,
    signaturePower(Power.strength),
    _ => true,
    1,
    powers,
    qualities,
    List(
      AbilityPool(
        2,
        abilities
      ),
      AbilityPool(
        1,
        abilities.map(_.copy(status = Status.Yellow))
      )
    ),
    PrincipleCategory.Expertise
  )
end PhysicalPowerhouse
