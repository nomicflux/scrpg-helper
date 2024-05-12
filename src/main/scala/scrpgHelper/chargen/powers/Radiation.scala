package scrpgHelper.chargen.powers

object Radiation:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val radiation: PowerSource = PowerSource(
    "Radiation",
    9,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Radioactive Recharge",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Defend),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Then, either remove a penalty on yourself or Recover using your Min die."
            ),
          ),
          AbilityTemplate(
            "Unstable Reaction",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After rolling during your turn, you may take 1 irreducible damage to reroll your entire dice pool."
            ),
          ),
          AbilityTemplate(
            "Wither",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Hinder that target using your Max die.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Charged Up",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you roll a 1 on one or more dice, you may reroll those dice. You must accept the result of the reroll."
            ),
          ),
          AbilityTemplate(
            "Dangerous Lash",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using using ",
              PowerChoice(),
              ", applying your Min die to each. If you roll doubles, also attack an ally using your Mid die."
            ),
          ),
          AbilityTemplate(
            "Radioactive Aura",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Attack),
            List(
              "When a new target enters the scene close to you, you may Attack it by rolling your single ",
              PowerChoice(),
              " die."
            ),
          )

        ),
      )
    ),
    List(Power.nuclear, Power.signatureVehicle, Power.signatureWeapon) ++ Power.athleticPowers ++ Power.selfControlPowers ++ Power.technologicalPowers,
    List(d(10), d(8), d(6))
  )
end Radiation
