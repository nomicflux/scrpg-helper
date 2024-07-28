package scrpgHelper.chargen.powers

object Cosmos:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers =
    List(Power.cosmic, Power.intuition, Power.signatureVehicle, Power.signatureWeapon) ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.selfControlPowers ++ Power.technologicalPowers

  val cosmos: PowerSource = PowerSource(
    "Cosmos",
    16,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Cosmic Ray Absorption",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(Action.Recover),
            List(
              "If you would take damage from",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
              ", instead reduce that damage to 0 and Recover that amount of Health."
            ),
          ),
          AbilityTemplate(
            "Encourage",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Boost),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Boost all nearby heroes taking Attack or Overcome actions using your Min die until your next turn.",
            ),
          ),
          AbilityTemplate(
            "Mass Effect",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost or Hinder using",
              PowerChoice(AbilityChoice.noDupes),
              " and apply that mod to multiple close targets."
            ),
          ),
        ),
      ),
    ),
    powers,
    List(d(10), d(8), d(8)),
  ).withUpgrades((qp, d) => qp match
                   case _: Power => Set(6,8,10).contains(d.n)
                   case _: Quality => false)
    .withDowngrades((qp, d) => qp match
                      case _: Power => Set(8,10,12).contains(d.n)
                      case _: Quality => false)
end Cosmos
