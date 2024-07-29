package scrpgHelper.chargen.powers

object Training:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val training: PowerSource = PowerSource(
    "Training",
    2,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Always Be Prepared",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Attack),
            List(
              "Boost yourself using",
              PowerChoice(AbilityChoice.noDupes),
              ". That bonus is persistent and exclusive. Then, Attack using your Min die. You may use the bonus you just created on that Attack."
            ),
          ),
          AbilityTemplate(
            "Reactive Field",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When you are attacked by a nearby enemy, the attacker also takes an equal amount of damage.",
            ),
          ),
          AbilityTemplate(
            "Flowing Fight",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Mid die to Attack one extra target for each bonus you have. Apply a different bonus to each Attack.",
            ),
          ),
        ),
      ),
    ),
    List(Power.gadgets, Power.signatureVehicle, Power.signatureWeapon) ++ Power.athleticPowers ++ Power.intellectualPowers,
    List(d(10), d(8), d(8)),
  ).withExtraArchetypeQuality()
end Training
