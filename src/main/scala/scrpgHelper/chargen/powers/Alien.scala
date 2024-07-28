package scrpgHelper.chargen.powers

object Alien:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers =
    List(Power.signatureVehicle, Power.signatureWeapon) ++ Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.technologicalPowers

  val alien: PowerSource = PowerSource(
    "Alien",
    14,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Alien Boost",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost all nearby allies using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max+Mid dice. Hinder yourself with your Min die."
            ),
          ),
          AbilityTemplate(
            "Empower and Repair",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder, Action.Defend, Action.Attack, Action.Recover),
            List(
              "Boost, Hinder, Defend, or Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". You and all nearby heroes in the Yellow or Red zone Recover Health equal to your Min die.",
            ),
          ),
          AbilityTemplate(
            "Halt",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When you are Attacked at close range, Defend yourself by rolling your single",
              PowerChoice(AbilityChoice.noDupes),
              "die."
            ),
          ),
        ),
      ),
    ),
    powers,
    List(d(8), d(8), d(8)),
  ).withUpgrades((qp, d) => d.n == 6)
    .withConditionalExtraPowers(ds => if ds.filter(_.n == 6).isEmpty then Some((d(6), powers)) else None)
end Alien
