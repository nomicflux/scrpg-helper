package scrpgHelper.chargen.powers

object Mystical:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val mystical: PowerSource = PowerSource(
    "Mystical",
    5,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Modification Wave",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost or Hinder using ",
              PowerChoice(AbilityChoice.noDupes),
              ", and apply that mod to multiple nearby targets."
            ),
          ),
          AbilityTemplate(
            "Mystic Redirection",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When another hero in the Yellow or Red zone would take damage, you may redirect it to yourself and Defend against it by rolling your single ",
              PowerChoice(AbilityChoice.noDupes),
              " die.",
            ),
          ),
          AbilityTemplate(
            "Sever Link",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Overcome, Action.Boost),
            List(
              "Overcome an environmental challenge using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. Either remove any penalty in the scene or Boost equal to you Mid die.",
            ),
          ),
        ),
      ),
    ),
    List(Power.awareness, Power.flight, Power.presence, Power.signatureWeapon, Power.teleportation) ++ Power.energyPowers ++ Power.materialPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(10), d(8), d(8)),
    Some((d(10), Quality.informationQualities)),
    None
  )
end Mystical
