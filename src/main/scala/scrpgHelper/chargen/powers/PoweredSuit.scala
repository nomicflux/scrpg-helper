package scrpgHelper.chargen.powers

object PoweredSuit:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powerSuit: Power = Power("Power Suit", PowerCategory.Hallmark)

  val poweredSuit: PowerSource = PowerSource(
    "Powered Suit",
    8,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Energy Converter",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            List(
              "When you take damage from ",
              EnergyChoice(),
              ", treat the amount of damage you take as a Boost action for yourself."
            ),
          ),
          AbilityTemplate(
            "Explosive Attack",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack up to three target using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Apply your Max die to one, your Mid die to another, and your Min die to the third. If you roll doubles, take a minor twist or take irreducible damage equal to that die.",
            ),
          ),
          AbilityTemplate(
            "Onboard Upgrade",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              PowerChoice((ps, ctx) => AbilityChoice.onePower(powerSuit)(ps, ctx) && AbilityChoice.noDupes(ps, ctx)),
              ". Use your Min+Mid dice. That bonus is persistent and exclusive.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Damage Reduction",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Reduce ",
              EnergyChoice.includePhysical(),
              " damage you take by 1 while you are in the Green zone, 2 while in the Yellow zone, and 3 while in the Red zone.",
            ),
          ),
          AbilityTemplate(
            "Diagnostic Subroutine",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever your status changes due to a change in your current Health, you may remove a penalty on yourself."
            ),
          )
        ),
      )
    ),
    List(powerSuit),
    List(powerSuit, Power.awareness, Power.cold, Power.elasticity, Power.fire, Power.lightningCalculator, Power.nuclear, Power.partDetachment, Power.signatureVehicle, Power.signatureWeapon) ++ Power.athleticPowers ++ Power.mobilityPowers,
    List(d(10), d(6), d(6))
  )
end PoweredSuit
