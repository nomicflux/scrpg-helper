package scrpgHelper.chargen.powers

object Extradimensional:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val extradimensional: PowerSource = PowerSource(
    "Extradimensional",
    17,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Absorb Essence",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            List(
              "When you defeat a minion, roll that minion's die and Boost yourself using that roll."
            ),
          ),
          AbilityTemplate(
            "Aura of Pain",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Then, take irreducible damage equal to the number of targets hit.",
            ),
          ),
          AbilityTemplate(
            "Bizarre Strike",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. Hinder that target with your Mid die. Hinder yourself with your Min die.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Attune",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              PowerChoice(),
              ". That bonus is persistent and exclusive. Damage dealt using that bonus is all ",
              EnergyChoice(),
              ".",
            ),
          ),
          AbilityTemplate(
            "Extrasensory Awareness",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When you would take damage that would change your zone, Defend against that damage by rolling your single ",
              QualityChoice(),
              " die."
            ),
          )
        ),
      )
    ),
    List(Power.cosmic, Power.duplication, Power.infernal, Power.intangibility, Power.invisibility, Power.radiant, Power.signatureVehicle, Power.signatureWeapon, Power.transmutation, Power.teleportation) ++ Power.intellectualPowers ++ Power.psychicPowers,
    List(d(12), d(6))
  )
end Extradimensional
