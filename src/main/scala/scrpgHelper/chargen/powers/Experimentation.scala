package scrpgHelper.chargen.powers

object Experimentation:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val experimentation: PowerSource = PowerSource(
    "Experimentation",
    4,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Personal Upgrade",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. That bonus is persistent and exclusive."
            ),
          ),
          AbilityTemplate(
            "Misdirection",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When a nearby hero in the Yellow or Red zone would take damage, Defend against that damage by rolling your single ",
              PowerChoice(AbilityChoice.noDupes),
              "die, then redirect any remaining damage to a nearby minion of your choice.",
            ),
          ),
          AbilityTemplate(
            "Throw Minion",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack a minion using ",
              PowerChoice(AbilityChoice.noDupes),
              ". The result of the minion's save Attacks another target of your choice.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Overpower",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you are Boosted, increase that bonus by +1. Then, if that bonus is +5 or higher, take damage equal to that bonus and remove it.",
            ),
          ),
          AbilityTemplate(
            "Unflagging",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "At the start of your turn, remove a penalty on yourself.",
            ),
          )
        ),
      )
    ),
    List(Power.signatureWeapon) ++ Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.selfControlPowers,
    List(d(8), d(8), d(8))
  )
end Experimentation
