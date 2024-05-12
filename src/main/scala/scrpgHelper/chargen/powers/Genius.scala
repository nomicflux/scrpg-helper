package scrpgHelper.chargen.powers

object Genius:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val genius: PowerSource = PowerSource(
    "Genius",
    15,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "A Plan For Everything",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Boost, Action.Defend),
            List(
              "When you are attacked, first roll your single ",
              PowerChoice(AbilityChoice.noDupes),
              " die. Defend yourself with that roll. Then, Boost yourself using that roll."
            ),
          ),
          AbilityTemplate(
            "Expanded Mind",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Attack),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. That bonus is persistent and exclusive. Then Attack using your Min die.",
            ),
          ),
          AbilityTemplate(
            "Overwhelming Vision",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Recover),
            List(
              "Attack using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Then, if the target of the Attack survived, also Attack that target with your Max die. Otherwise, Recover an amount of Health equal to your Min die.",
            ),
          ),
        ),
      ),
    ),
    List(Power.inventions, Power.robotics, Power.signatureVehicle, Power.signatureWeapon) ++ Power.intellectualPowers,
    List(d(10), d(6), d(6)),
    Some((d(10), Quality.informationQualities ++ Quality.mentalQualities)),
    None
  )
end Genius
