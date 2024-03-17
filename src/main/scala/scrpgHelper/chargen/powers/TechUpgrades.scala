package scrpgHelper.chargen.powers

object TechUpgrades:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val techUpgrades: PowerSource = PowerSource(
    "Tech Upgrades",
    10,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Energy Burst",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ", using your Min die afainst each."
            ),
          ),
          AbilityTemplate(
            "Recharge",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Recover),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Then, either remove a penalty on yourself or Recover using your Min die.",
            ),
          ),
          AbilityTemplate(
            "Techno-Absorb",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(Action.Recover),
            List(
              "When you would take damage from ",
              EnergyChoice(),
              ", you may Recover that amount of Health instead.",
            ),
          ),
          AbilityTemplate(
            "Tactical Analysis",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            List(
              "When Attacked, treat the amount of damage you take as a Boost action for yourself."
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Indiscriminate Fabrication",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              PowerChoice(),
              ", assigning your Min, Mid, and Max dice to 3 different bonuses, one of which must be given to an enemy.",
            ),
          ),
          AbilityTemplate(
            "Organi-Hack",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack a target using using ",
              PowerChoice(),
              ". Hinder that target with your Min die."
            ),
          )
        ),
      )
    ),
    List(Power.signatureVehicle, Power.signatureWeapon) ++ Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.technologicalPowers,
    List(d(10), d(8), d(8))
  )
end TechUpgrades
