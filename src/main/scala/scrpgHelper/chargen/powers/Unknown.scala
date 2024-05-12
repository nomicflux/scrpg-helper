package scrpgHelper.chargen.powers

object Unknown:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val unknown: PowerSource = PowerSource(
    "Unknown",
    18,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Brainstorm",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Boost),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Hit one target using your Min die, another target with your Mid die, and Boost using your Max die."
            ),
          ),
          AbilityTemplate(
            "Strange Enhancement",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost all nearby allies using ",
              PowerChoice(AbilityChoice.noDupes),
              " using your Max + Mid dice. Hinder yourself with your Min die.",
            ),
          ),
          AbilityTemplate(
            "Volatile Creations",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(),
            List(
              "When one of your bonuses, penalties, or other creation of your powers is destroyed, deala target damage equal to the roll of your ",
              PowerChoice(AbilityChoice.noDupes),
              " die.",
            ),
          ),
        ),
      ),
    ),
    Power.energyPowers ++ Power.intellectualPowers ++ Power.materialPowers ++ Power.selfControlPowers ++ Power.technologicalPowers,
    List(d(10), d(8), d(6)),
    Some((d(8), Quality.socialQualities)),
    None
  )
end Unknown
