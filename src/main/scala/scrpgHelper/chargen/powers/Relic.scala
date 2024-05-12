package scrpgHelper.chargen.powers

object Relic:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val relic: PowerSource = PowerSource(
    "Relic",
    7,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Harvest Life Force",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Recover),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Min die. Take damage equal to your Mid die, and one nearby ally Recovers Health equal to you Max die."
            ),
          ),
          AbilityTemplate(
            "Magical Shield",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When another hero in the Yellow or Red zone would take damage, you may Defend them by rolling your single",
              PowerChoice(AbilityChoice.noDupes),
              " die.",
            ),
          ),
          AbilityTemplate(
            "Momentary Power",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ". That bonus is persistent and exclusive.",
            ),
          ),
          AbilityTemplate(
            "Relic Drain",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Hinder, Action.Recover),
            List(
              "Hinder using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Also Recover Health equal to your Min die.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Draw Power",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              PowerChoice(),
              ". That bonus is persistent and exclusive.",
            ),
          ),
          AbilityTemplate(
            "Punishment",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you Attack an enemy that has inflicted a penalty on you, treat that penalty as if it were a bonus for the purpose of that Attack.",
            ),
          )
        ),
      )
    ),
    List(Power.awareness, Power.intuition, Power.signatureVehicle, Power.signatureWeapon) ++ Power.energyPowers ++ Power.materialPowers ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(10), d(10), d(6))
  )
end Relic
