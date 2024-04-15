package scrpgHelper.chargen.archetypes

object CloseQuartersCombatant:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers = List(
    Power.signatureWeapon,
  ) ++ Power.athleticPowers ++ Power.mobilityPowers ++ Power.technologicalPowers

  val qualities = Quality.physicalQualities ++ Quality.socialQualities

  val abilities = List(
          AbilityTemplate(
            "Defensive Strike",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Defend, Action.Attack),
            List(
              "Defend using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". Attack using your Min die."
            )
          ),
          AbilityTemplate(
            "Dual Strike",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack one target using using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". Attack a second target using your Min die."
            )
          ),
          AbilityTemplate(
            "Flexible Stance",
            Status.Green,
            AbilityCategory.Action,
            _ => List(),
            List(
              "Take any two basic actions using using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ", each using your Min die."
            )
          ),
          AbilityTemplate(
            "Offensive Strike",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". Use your Max die."
            )
          ),
          AbilityTemplate(
            "Precise Strike",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". Ignore all penalties on this Attack, ignore any Defend actions, and it cannot be affected by Reactions."
            )
          ),
          AbilityTemplate(
            "Throw Minion",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack a minion using",
              PowerQualityChoice(pqs => AbilityChoice.intersection(powers ++ qualities)(pqs) && AbilityChoice.noDupes(pqs)),
              ". Whatever that minion rolls as defense Attacks another target of your choice."
            )
          ),
        )

  val closeQuartersCombatant = Archetype(
    "Close Quarters Combatant",
    6,
    _ => true,
    signatureQuality(Quality.closeCombat),
    1,
    powers,
    qualities,
    List(
      AbilityPool(
        3,
        abilities
      ),
      AbilityPool(
        1,
        abilities.map(_.copy(status = Status.Yellow))
      )
    ),
    PrincipleCategory.Responsibility
  )
end CloseQuartersCombatant
